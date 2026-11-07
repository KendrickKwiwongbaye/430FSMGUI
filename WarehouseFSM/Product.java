import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Represents a product in the warehouse system.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 2L;

    private int productID;
    private String name;
    private int quantityInStock;
    private BigDecimal salePrice;
    private List<WaitlistEntry> waitlistEntries;

    /**
     * Creates a Product.
     *
     * @param productID the unique product ID
     * @param name the product name
     * @param quantityInStock quantity in stock
     * @param salePrice unit sale price
     */
    public Product(int productID,
                   String name,
                   int quantityInStock,
                   BigDecimal salePrice) {

        if (productID <= 0 || name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("A positive product ID and name are required.");
        }
        if (quantityInStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        this.productID = productID;
        this.name = name.trim();
        this.quantityInStock = quantityInStock;
        this.salePrice = Money.positive(salePrice);
        this.waitlistEntries = new LinkedList<WaitlistEntry>();
    }

    /**
     * Returns the product ID.
     *
     * @return product ID
     */
    public int getProductID() {
        return productID;
    }

    /**
     * Returns the product name.
     *
     * @return product name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the quantity in stock.
     *
     * @return quantity in stock
     */
    public int getQuantityInStock() {
        return quantityInStock;
    }

    /**
     * Returns the unit sale price.
     *
     * @return sale price
     */
    public BigDecimal getSalePrice() {
        return salePrice;
    }

    /** Supplies as many requested units as are currently in stock. */
    public int fulfillFromStock(int requestedQuantity) {
        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException("Requested quantity must be positive.");
        }
        int suppliedQuantity = Math.min(requestedQuantity, quantityInStock);
        quantityInStock -= suppliedQuantity;
        return suppliedQuantity;
    }

    /** Adds received units that remain after waitlist processing to stock. */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Added stock must be positive.");
        }
        quantityInStock += quantity;
    }

    /** Appends an unfilled request to this product's FIFO waitlist. */
    public void addWaitlistEntry(Client client, int quantity) {
        waitlistEntries.add(new WaitlistEntry(client, quantity));
    }

    /** Removes a waitlist entry once a shipment fully fulfills it. */
    public boolean removeWaitlistEntry(WaitlistEntry entry) {
        return waitlistEntries.remove(entry);
    }

    /** Returns the total quantity clients are waiting for. */
    public int getTotalWaitlistedQuantity() {
        int total = 0;
        for (WaitlistEntry entry : waitlistEntries) {
            total += entry.getRemainingQuantity();
        }
        return total;
    }

    /** Returns a read-only snapshot of this product's FIFO waitlist. */
    public List<WaitlistEntry> getWaitlistEntries() {
        return Collections.unmodifiableList(
                new ArrayList<WaitlistEntry>(waitlistEntries));
    }

    /**
     * Returns the product information.
     *
     * @return product details
     */
    public String getDetails() {

        return "Product ID: " + productID
                + ", Name: " + name
                + ", Quantity In Stock: "
                + quantityInStock
                + ", Sale Price: $"
                + salePrice.toPlainString();
    }

    /**
     * Returns the product information.
     *
     * @return product information
     */
    public String toString() {
        return getDetails();
    }
}
