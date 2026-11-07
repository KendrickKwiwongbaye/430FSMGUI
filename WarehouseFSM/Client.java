import java.util.*;
import java.io.*;
import java.math.BigDecimal;

/**
 * Represents a client in the warehouse system.
 */
public class Client implements Serializable {

    private static final long serialVersionUID = 2L;

    private int clientID;
    private String name;
    private String address;
    private BigDecimal balance;
    private List<WishlistItem> wishlistItems;
    private List<Transaction> transactions;
    private List<Invoice> invoices;

    /**
     * Creates a Client.
     *
     * @param clientID the unique client ID
     * @param name the client's name
     * @param address the client's address
     */
    public Client(int clientID, String name, String address) {

        if (clientID <= 0 || name == null || name.trim().isEmpty()
                || address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "A positive client ID, name and address are required.");
        }

        this.clientID = clientID;
        this.name = name.trim();
        this.address = address.trim();
        this.balance = new BigDecimal("0.00");

        wishlistItems = new LinkedList<WishlistItem>();
        transactions = new LinkedList<Transaction>();
        invoices = new LinkedList<Invoice>();
    }

    /**
     * Returns the client ID.
     *
     * @return client ID
     */
    public int getClientID() {
        return clientID;
    }

    /**
     * Returns the client's name.
     *
     * @return client name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the client's address.
     *
     * @return client address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the client's credit/debit balance.
     *
     * @return client balance
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /** Records a payment; overpayments leave a negative balance (client credit). */
    public void credit(BigDecimal amount) {
        recordTransaction("Payment received", Money.positive(amount).negate());
    }

    /** Records a charge for future order and shipment fulfillment. */
    public void debit(BigDecimal amount) {
        recordTransaction("Charge recorded", Money.positive(amount));
    }

    private void recordTransaction(String description, BigDecimal amount) {
        Transaction transaction = new Transaction(description, amount);
        transactions.add(transaction);
        balance = balance.add(transaction.getAmount());
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(new ArrayList<Transaction>(transactions));
    }

    public void addInvoice(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("An invoice is required.");
        }
        invoices.add(invoice);
    }

    /** Records a fulfillment invoice and automatically charges its total. */
    public void chargeInvoice(String description, Invoice invoice) {
        addInvoice(invoice);
        recordTransaction(description, invoice.getTotal());
    }

    public List<Invoice> getInvoices() {
        return Collections.unmodifiableList(new ArrayList<Invoice>(invoices));
    }

    /** Shows a negative balance as an absolute credit amount for the clerk. */
    public String getBalanceDetails() {
        if (balance.signum() < 0) {
            return "Credit: $" + balance.abs().toPlainString();
        }
        return "Balance: $" + balance.toPlainString();
    }

    /**
     * Searches the client's wishlist for a product.
     *
     * @param productID the product ID
     * @return WishlistItem if found, otherwise null
     */
    public WishlistItem findWishlistItem(int productID) {

        for (WishlistItem item : wishlistItems) {

            if (item.getProduct().getProductID() == productID) {
                return item;
            }
        }

        return null;
    }

    /**
     * Adds a product to the client's wishlist.
     * If the product already exists, the quantity is updated.
     *
     * @param product the product
     * @param quantity the desired quantity
     * @return true when the operation is completed
     */
    public boolean addWishlistItem(Product product, int quantity) {

        if (product == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "A product and positive wishlist quantity are required.");
        }

        WishlistItem item =
                findWishlistItem(product.getProductID());

        if (item == null) {

            wishlistItems.add(
                    new WishlistItem(product, quantity));

        } else {

            item.setQuantity(quantity);
        }

        return true;
    }

    /** Updates the quantity of an existing wishlist item. */
    public boolean updateWishlistItemQuantity(int productID, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        WishlistItem item = findWishlistItem(productID);
        if (item == null) {
            return false;
        }
        item.setQuantity(quantity);
        return true;
    }

    /** Removes a product from the wishlist; false when it is not listed. */
    public boolean removeWishlistItem(int productID) {
        WishlistItem item = findWishlistItem(productID);
        if (item == null) {
            return false;
        }
        return wishlistItems.remove(item);
    }

    /**
     * Returns all wishlist items.
     *
     * @return list of WishlistItems
     */
    public List<WishlistItem> getWishlistItems() {
        return Collections.unmodifiableList(
                new ArrayList<WishlistItem>(wishlistItems));
    }

    /** Removes the wishlist entries that have been submitted for fulfillment. */
    public void clearWishlist() {
        wishlistItems.clear();
    }

    /**
     * Returns the client's information.
     *
     * @return client details
     */
    public String getDetails() {

        return "Client ID: " + clientID
                + ", Name: " + name
                + ", Address: " + address
                + ", " + getBalanceDetails();
    }

    /**
     * Returns the client's information.
     *
     * @return client information
     */
    public String toString() {
        return getDetails();
    }
}
