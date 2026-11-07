import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Owns the warehouse FSM: the transition matrix, the current state, the role
 * history, and the window that every state draws its buttons in.
 */
public final class Context {
    public static final int EXIT = 0;
    public static final int LOGIN_CLIENT = 1;
    public static final int LOGIN_CLERK = 2;
    public static final int LOGIN_MANAGER = 3;
    public static final int LOGOUT = 4;
    public static final int BECOME_CLIENT = 5;
    public static final int BECOME_CLERK = 6;

    private static final int RETURN = -1;
    private static final int TERMINATED = -2;

    private static final int[][] TRANSITIONS = {
        // exit, client login, clerk login, manager login, logout, become client, become clerk
        {TERMINATED, ClientMenuState.ID, ClerkMenuState.ID, ManagerMenuState.ID,
            OpeningState.ID, OpeningState.ID, OpeningState.ID},
        {ClientMenuState.ID, ClientMenuState.ID, ClientMenuState.ID, ClientMenuState.ID,
            RETURN, ClientMenuState.ID, ClientMenuState.ID},
        {ClerkMenuState.ID, ClerkMenuState.ID, ClerkMenuState.ID, ClerkMenuState.ID,
            RETURN, ClientMenuState.ID, ClerkMenuState.ID},
        {ManagerMenuState.ID, ManagerMenuState.ID, ManagerMenuState.ID, ManagerMenuState.ID,
            RETURN, ManagerMenuState.ID, ClerkMenuState.ID}
    };

    /** Asks the user for text and shows messages. Swing dialogs are used by default. */
    public interface Prompter {
        /** Returns the text entered, or null when the user cancelled. */
        String ask(String prompt);

        void tell(String message);
    }

    /** Thrown when the user cancels a dialog, to stop the operation in progress. */
    public static final class CancelledException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final class SwingPrompter implements Prompter {
        @Override
        public String ask(String prompt) {
            return JOptionPane.showInputDialog(frame, prompt);
        }

        @Override
        public void tell(String message) {
            JOptionPane.showMessageDialog(frame, message);
        }
    }

    private final StoreSystem store;
    private final State[] states = {
        new OpeningState(),
        new ClientMenuState(),
        new ClerkMenuState(),
        new ManagerMenuState()
    };
    private final Deque<State> previousStates = new ArrayDeque<State>();
    private State currentState = states[OpeningState.ID];
    private Integer activeClientId;

    private final JPanel root = new JPanel(new BorderLayout(8, 8));
    private final JLabel header = new JLabel(" ");
    private final JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 6, 6));
    private final JTextArea output = new JTextArea();
    private JFrame frame;
    private Prompter prompter = new SwingPrompter();

    public Context(StoreSystem store) {
        if (store == null) {
            throw new IllegalArgumentException("A warehouse is required.");
        }
        this.store = store;
        buildScreen();
    }

    private void buildScreen() {
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JPanel buttonColumn = new JPanel(new BorderLayout());
        buttonColumn.add(buttonPanel, BorderLayout.NORTH);
        buttonColumn.setPreferredSize(new Dimension(280, 400));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(header, BorderLayout.NORTH);
        root.add(buttonColumn, BorderLayout.WEST);
        root.add(new JScrollPane(output), BorderLayout.CENTER);
    }

    /** Opens the window and shows the first state. */
    public void start() {
        frame = new JFrame("Warehouse");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.setSize(900, 620);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        begin();
    }

    /** Shows the current state's screen. Used by start() and by scripted checks. */
    public void begin() {
        currentState.run(this);
    }

    public State getCurrentState() {
        return currentState;
    }

    public String getCurrentStateName() {
        return currentState == null ? "Exited" : currentState.getName();
    }

    public JPanel getRootPanel() {
        return root;
    }

    public String getOutputText() {
        return output.getText();
    }

    public void setPrompter(Prompter prompter) {
        this.prompter = prompter;
    }

    public int getActiveClientId() {
        if (activeClientId == null) {
            throw new IllegalStateException("No client is active.");
        }
        return activeClientId.intValue();
    }

    public Client getActiveClient() {
        return store.findClient(getActiveClientId());
    }

    /** Applies a transition through the state-by-event matrix, then shows the new state. */
    public void transition(int event) {
        if (currentState == null || event < EXIT || event > BECOME_CLERK) {
            throw new IllegalArgumentException("Unknown FSM transition.");
        }
        int targetId = TRANSITIONS[currentState.getId()][event];
        if (targetId == TERMINATED) {
            currentState = null;
            previousStates.clear();
            activeClientId = null;
            if (frame != null) {
                frame.dispose();
                System.exit(0);
            }
            return;
        }
        if (targetId == RETURN) {
            activeClientId = null;
            currentState = previousStates.isEmpty()
                    ? states[OpeningState.ID] : previousStates.pop();
        } else {
            State target = states[targetId];
            if (target != currentState) {
                previousStates.push(currentState);
                currentState = target;
            }
            if (currentState.getId() != ClientMenuState.ID) {
                activeClientId = null;
            }
        }
        currentState.run(this);
    }

    // ---- Window: screens, buttons, output and dialogs ----

    /** Replaces the title and the buttons on the screen. */
    public void showScreen(String title, ActionButton... buttons) {
        header.setText(title);
        buttonPanel.removeAll();
        for (ActionButton button : buttons) {
            buttonPanel.add(button);
        }
        buttonPanel.revalidate();
        buttonPanel.repaint();
        print("");
        print("--- " + title + " ---");
    }

    /** Makes a button whose click runs the operation and reports problems in the window. */
    public ActionButton button(String label, Runnable operation) {
        return new ActionButton(label, () -> {
            try {
                operation.run();
            } catch (CancelledException exception) {
                print("Cancelled.");
            } catch (IllegalArgumentException exception) {
                print("Error: " + exception.getMessage());
            } catch (IllegalStateException exception) {
                print("Error: " + exception.getMessage());
            }
        });
    }

    /** Adds a line to the output area. */
    public void print(Object message) {
        output.append(String.valueOf(message) + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    public String getToken(String prompt) {
        while (true) {
            String answer = prompter.ask(prompt);
            if (answer == null) {
                throw new CancelledException();
            }
            answer = answer.trim();
            if (!answer.isEmpty()) {
                return answer;
            }
            prompter.tell("Input cannot be empty.");
        }
    }

    private int getNumber(String prompt, int minimum, String problem) {
        while (true) {
            try {
                int value = Integer.parseInt(getToken(prompt));
                if (value >= minimum) {
                    return value;
                }
            } catch (NumberFormatException exception) {
                // Falls through to the message below.
            }
            prompter.tell(problem);
        }
    }

    private int getPositiveNumber(String prompt) {
        return getNumber(prompt, 1, "Enter a whole number greater than 0.");
    }

    private int getNonnegativeNumber(String prompt) {
        return getNumber(prompt, 0, "Enter a whole number, 0 or more.");
    }

    private BigDecimal getMoney(String prompt) {
        while (true) {
            try {
                return Money.positive(new BigDecimal(getToken(prompt)));
            } catch (IllegalArgumentException exception) {
                prompter.tell("Enter a positive amount in whole cents (for example 10.50).");
            }
        }
    }

    // ---- Operations that change state ----

    public boolean selectClient() {
        int id = getPositiveNumber("Enter client ID:");
        if (store.findClient(id) == null) {
            print("Client not found.");
            prompter.tell("Client not found.");
            return false;
        }
        activeClientId = Integer.valueOf(id);
        return true;
    }

    public void loginAsClient() {
        if (selectClient()) {
            transition(LOGIN_CLIENT);
        }
    }

    public void loginAsClerk() {
        transition(LOGIN_CLERK);
    }

    public void loginAsManager() {
        transition(LOGIN_MANAGER);
    }

    public void becomeClient() {
        if (selectClient()) {
            transition(BECOME_CLIENT);
        }
    }

    public void becomeClerk() {
        transition(BECOME_CLERK);
    }

    public void logout() {
        transition(LOGOUT);
    }

    public void exit() {
        transition(EXIT);
    }

    // ---- Operations that stay in the current state ----

    public void addClient() {
        Client client = store.addClient(getToken("Enter client name:"),
                getToken("Enter client address:"));
        print("Client added: " + client);
    }

    public void addProduct() {
        String name = getToken("Enter product name:");
        int quantity = getNonnegativeNumber("Enter quantity in stock:");
        BigDecimal price = getMoney("Enter unit price:");
        Product product = store.addProduct(name, quantity, price);
        print("Product added: " + product);
    }

    public void showClients() {
        print("All clients");
        List<Client> clients = store.getAllClients();
        if (clients.isEmpty()) {
            print("No clients found.");
        }
        for (Client client : clients) {
            print(client);
        }
    }

    public void showProducts() {
        print("All products");
        List<Product> products = store.getAllProducts();
        if (products.isEmpty()) {
            print("No products found.");
        }
        for (Product product : products) {
            print(product);
        }
    }

    public void showOutstandingBalances() {
        print("Clients with outstanding balances");
        List<Client> clients = store.getClientsWithOutstandingBalance();
        if (clients.isEmpty()) {
            print("No outstanding balances.");
        }
        for (Client client : clients) {
            print(client);
        }
    }

    public void showClientDetails() {
        print(getActiveClient().getDetails());
    }

    public void addToWishlist() {
        int clientId = getActiveClientId();
        showProducts();
        int productId = getPositiveNumber("Enter product ID:");
        int quantity = getPositiveNumber("Enter wishlist quantity:");
        if (store.addToWishlist(clientId, productId, quantity)) {
            print("Product added to wishlist.");
        } else {
            print("Could not add product to wishlist.");
        }
    }

    public void removeFromWishlist() {
        int clientId = getActiveClientId();
        int productId = getPositiveNumber("Enter product ID to remove:");
        if (store.removeFromWishlist(clientId, productId)) {
            print("Product removed from wishlist.");
        } else {
            print("Product was not in the wishlist.");
        }
    }

    public void showWishlist() {
        int clientId = getActiveClientId();
        print("Wishlist for Client " + clientId);
        List<WishlistItem> items = store.getWishlistItems(clientId);
        if (items == null) {
            print("Client not found.");
        } else if (items.isEmpty()) {
            print("Wishlist is empty.");
        } else {
            for (WishlistItem item : items) {
                print(item);
            }
        }
    }

    public void showTransactions() {
        List<Transaction> transactions = store.getTransactions(getActiveClientId());
        if (transactions.isEmpty()) {
            print("No transactions found.");
        } else {
            for (Transaction transaction : transactions) {
                print(transaction);
            }
        }
    }

    public void showInvoices() {
        List<Invoice> invoices = store.getInvoices(getActiveClientId());
        if (invoices.isEmpty()) {
            print("No invoices found.");
        } else {
            for (Invoice invoice : invoices) {
                print(invoice);
            }
        }
    }

    public void placeOrder() {
        int clientId = getActiveClientId();
        List<OrderFulfillment> results = store.fillOrder(clientId);
        if (results.isEmpty()) {
            print("Wishlist is empty; no order was submitted.");
            return;
        }
        boolean suppliedItems = false;
        for (OrderFulfillment result : results) {
            print(result);
            suppliedItems |= result.getSuppliedQuantity() > 0;
        }
        if (suppliedItems) {
            List<Invoice> invoices = store.getInvoices(clientId);
            print(invoices.get(invoices.size() - 1));
        } else {
            print("No invoice generated; no items were supplied.");
        }
        print("Order submitted; processed wishlist entries cleared.");
    }

    public void receivePayment() {
        int clientId = getPositiveNumber("Enter client ID:");
        Client client = store.findClient(clientId);
        if (client == null) {
            print("Client not found.");
            prompter.tell("Client not found.");
            return;
        }
        print(client.getBalanceDetails());
        store.receivePayment(clientId, getMoney("Enter payment amount:"));
        print("Payment recorded.");
        print(client.getBalanceDetails());
    }

    public void showProductWaitlist() {
        int productId = getPositiveNumber("Enter product ID:");
        Product product = store.findProduct(productId);
        if (product == null) {
            print("Product not found.");
            prompter.tell("Product not found.");
            return;
        }
        List<WaitlistEntry> waitlist = store.getProductWaitlist(productId);
        print("Waitlist for " + product.getName() + " (P" + productId + ")");
        if (waitlist.isEmpty()) {
            print("Waitlist is empty.");
        } else {
            for (WaitlistEntry entry : waitlist) {
                print(entry);
            }
            print("Total waitlisted quantity: " + product.getTotalWaitlistedQuantity());
        }
    }

    public void receiveShipment() {
        int productId = getPositiveNumber("Enter product ID:");
        if (store.findProduct(productId) == null) {
            print("Product not found.");
            prompter.tell("Product not found.");
            return;
        }
        int quantity = getPositiveNumber("Enter received quantity:");
        List<ShipmentFulfillment> results = store.receiveShipment(productId, quantity);
        if (results.isEmpty()) {
            print("No waitlisted requests; full shipment added to stock.");
        } else {
            print("Waitlist fulfillments:");
            for (ShipmentFulfillment result : results) {
                print(result);
                print(result.getInvoice());
            }
        }
        print(store.findProduct(productId));
    }

    public void saveWarehouse() {
        if (StoreSystem.save()) {
            print("Warehouse data saved.");
        } else {
            print("Could not save warehouse data.");
        }
    }
}
