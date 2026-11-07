import java.util.*;
import java.io.*;
import java.math.BigDecimal;

/**
 * Controls the warehouse system.
 * StoreSystem manages clients, products,
 * and client wishlists.
 */
public class StoreSystem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Save file holding the serialized warehouse; reserved in .gitignore. */
    private static final String SAVE_FILE = "WarehouseData";

    private static StoreSystem storeSystem;

    private List<Client> clients;
    private List<Product> products;

    private int nextClientID;
    private int nextProductID;

    /**
     * Creates the StoreSystem.
     */
    private StoreSystem() {

        clients = new LinkedList<Client>();
        products = new LinkedList<Product>();

        nextClientID = 1;
        nextProductID = 1;
    }

    /**
     * Returns the single StoreSystem instance.
     *
     * @return StoreSystem instance
     */
    public static StoreSystem instance() {

        if (storeSystem == null) {

            storeSystem =
                    new StoreSystem();
        }

        return storeSystem;
    }

    /**
     * Creates and adds a new client.
     *
     * @param name client name
     * @param address client address
     * @return newly created Client
     */
    public Client addClient(String name,
                            String address) {

        Client client =
                new Client(
                        nextClientID,
                        name,
                        address);

        clients.add(client);
        generateClientID();

        return client;
    }

    /**
     * Generates a unique client ID.
     *
     * @return next client ID
     */
    public int generateClientID() {
        return nextClientID++;
    }

    /**
     * Creates and adds a new product.
     *
     * @param name product name
     * @param quantityInStock quantity in stock
     * @param salePrice unit price
     * @return newly created Product
     */
    public Product addProduct(
            String name,
            int quantityInStock,
            BigDecimal salePrice) {

        Product product =
                new Product(
                        nextProductID,
                        name,
                        quantityInStock,
                        salePrice);

        products.add(product);
        generateProductID();

        return product;
    }

    /**
     * Generates a unique product ID.
     *
     * @return next product ID
     */
    public int generateProductID() {
        return nextProductID++;
    }

    /**
     * Searches for a client.
     *
     * @param clientID client ID
     * @return Client if found, otherwise null
     */
    public Client findClient(int clientID) {

        for (Client client : clients) {

            if (client.getClientID()
                    == clientID) {

                return client;
            }
        }

        return null;
    }

    /** Applies a payment to the specified account and returns its new balance. */
    public BigDecimal receivePayment(int clientID, BigDecimal amount) {
        Client client = requireClient(clientID);
        client.credit(amount);
        return client.getBalance();
    }

    public List<Transaction> getTransactions(int clientID) {
        return requireClient(clientID).getTransactions();
    }

    private Client requireClient(int clientID) {
        Client client = findClient(clientID);
        if (client == null) {
            throw new IllegalArgumentException("Client not found.");
        }
        return client;
    }

    private Product requireProduct(int productID) {
        Product product = findProduct(productID);
        if (product == null) {
            throw new IllegalArgumentException("Product not found.");
        }
        return product;
    }

    /**
     * Searches for a product.
     *
     * @param productID product ID
     * @return Product if found, otherwise null
     */
    public Product findProduct(
            int productID) {

        for (Product product : products) {

            if (product.getProductID()
                    == productID) {

                return product;
            }
        }

        return null;
    }

    /**
     * Adds a product to a client's wishlist.
     *
     * @param clientID client ID
     * @param productID product ID
     * @param quantity desired quantity
     * @return true if successful, otherwise false
     */
    public boolean addToWishlist(
            int clientID,
            int productID,
            int quantity) {

        Client client =
                findClient(clientID);

        Product product =
                findProduct(productID);

        if (client == null
                || product == null
                || quantity <= 0) {

            return false;
        }

        return client.addWishlistItem(
                product,
                quantity);
    }

    /** Updates the quantity for an existing wishlist product. */
    public boolean updateWishlistQuantity(
            int clientID,
            int productID,
            int quantity) {

        Client client = findClient(clientID);
        Product product = findProduct(productID);

        if (client == null || product == null || quantity <= 0) {
            return false;
        }

        return client.updateWishlistItemQuantity(productID, quantity);
    }

    /** Removes a product from a client's wishlist. */
    public boolean removeFromWishlist(int clientID, int productID) {
        Client client = findClient(clientID);
        if (client == null) {
            return false;
        }
        return client.removeWishlistItem(productID);
    }

    /** Fulfills available stock and queues each shortage in product FIFO order. */
    public List<OrderFulfillment> fillOrder(int clientID) {
        Client client = requireClient(clientID);
        List<WishlistItem> wishlist = client.getWishlistItems();
        List<OrderFulfillment> results = new ArrayList<OrderFulfillment>();
        List<Invoice.LineItem> invoiceLines = new ArrayList<Invoice.LineItem>();

        for (WishlistItem item : wishlist) {
            Product product = item.getProduct();
            int requestedQuantity = item.getQuantity();
            int suppliedQuantity = product.fulfillFromStock(requestedQuantity);
            int waitlistedQuantity = requestedQuantity - suppliedQuantity;

            if (waitlistedQuantity > 0) {
                product.addWaitlistEntry(client, waitlistedQuantity);
            }
            if (suppliedQuantity > 0) {
                invoiceLines.add(new Invoice.LineItem(product, suppliedQuantity));
            }

            results.add(new OrderFulfillment(product, requestedQuantity,
                    suppliedQuantity, waitlistedQuantity));
        }

        if (!invoiceLines.isEmpty()) {
            client.chargeInvoice("Order charge", new Invoice(invoiceLines));
        }
        client.clearWishlist();
        return Collections.unmodifiableList(results);
    }

    /**
     * Processes a received shipment for a product. Waitlisted requests are
     * filled first in FIFO order; each fill is invoiced and automatically
     * charged to its client, partially filled requests stay queued, and
     * leftover units are added to stock.
     *
     * @param productID product that arrived
     * @param receivedQuantity units received
     * @return read-only list of waitlist fulfillments, in FIFO order
     */
    public List<ShipmentFulfillment> receiveShipment(int productID, int receivedQuantity) {
        Product product = requireProduct(productID);
        if (receivedQuantity <= 0) {
            throw new IllegalArgumentException("Received quantity must be positive.");
        }

        List<ShipmentFulfillment> fulfillments = new ArrayList<ShipmentFulfillment>();
        int remainingShipment = receivedQuantity;

        for (WaitlistEntry entry : product.getWaitlistEntries()) {
            if (remainingShipment == 0) {
                break;
            }
            int filledQuantity = Math.min(entry.getRemainingQuantity(), remainingShipment);
            entry.fulfill(filledQuantity);
            remainingShipment -= filledQuantity;
            if (entry.getRemainingQuantity() == 0) {
                product.removeWaitlistEntry(entry);
            }

            Invoice invoice = new Invoice(Collections.singletonList(
                    new Invoice.LineItem(product, filledQuantity)));
            entry.getClient().chargeInvoice("Shipment fulfillment charge", invoice);
            fulfillments.add(new ShipmentFulfillment(entry.getClient(), product,
                    filledQuantity, entry.getRemainingQuantity(), invoice));
        }

        if (remainingShipment > 0) {
            product.addStock(remainingShipment);
        }
        return Collections.unmodifiableList(fulfillments);
    }

    /** Returns the client's dated invoices. */
    public List<Invoice> getInvoices(int clientID) {
        return requireClient(clientID).getInvoices();
    }

    /** Returns a product's FIFO waitlist, or null when the product does not exist. */
    public List<WaitlistEntry> getProductWaitlist(int productID) {
        Product product = findProduct(productID);
        return product == null ? null : product.getWaitlistEntries();
    }

    /**
     * Returns an iterator for all clients.
     *
     * @return client Iterator
     */
    public Iterator<Client> getClients() {
        return getAllClients().iterator();
    }

    /**
     * Returns an iterator for all products.
     *
     * @return product Iterator
     */
    public Iterator<Product> getProducts() {
        return getAllProducts().iterator();
    }

    /**
     * Returns all clients.
     *
     * @return client list
     */
    public List<Client> getAllClients() {
        return Collections.unmodifiableList(new ArrayList<Client>(clients));
    }

    /**
     * Returns all products.
     *
     * @return product list
     */
    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(new ArrayList<Product>(products));
    }

    /** Returns the clients that currently owe money (positive balance). */
    public List<Client> getClientsWithOutstandingBalance() {
        List<Client> owing = new ArrayList<Client>();
        for (Client client : clients) {
            if (client.getBalance().signum() > 0) {
                owing.add(client);
            }
        }
        return Collections.unmodifiableList(owing);
    }

    /**
     * Saves the warehouse, including all data and ID counters.
     *
     * @return true when the save succeeds
     */
    public static boolean save() {
        try {
            ObjectOutputStream output = new ObjectOutputStream(
                    new FileOutputStream(SAVE_FILE));
            output.writeObject(instance());
            output.close();
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    /**
     * Loads the saved warehouse, replacing the in-memory instance so data
     * and ID counters continue from the saved state.
     *
     * @return restored StoreSystem, or null when no save could be read
     */
    public static StoreSystem retrieve() {
        try {
            ObjectInputStream input = new ObjectInputStream(
                    new FileInputStream(SAVE_FILE));
            storeSystem = (StoreSystem) input.readObject();
            input.close();
            return storeSystem;
        } catch (IOException exception) {
            return null;
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    /**
     * Returns the wishlist for a client.
     *
     * @param clientID client ID
     * @return wishlist or null
     */
    public List<WishlistItem>
            getWishlistItems(int clientID) {

        Client client =
                findClient(clientID);

        if (client == null) {
            return null;
        }

        return client.getWishlistItems();
    }
}
