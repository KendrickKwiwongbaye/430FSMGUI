import java.io.Serializable;

/** Immutable result of filling one waitlisted request from a received shipment. */
public final class ShipmentFulfillment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Client client;
    private final Product product;
    private final int filledQuantity;
    private final int remainingQuantity;
    private final Invoice invoice;

    public ShipmentFulfillment(Client client, Product product, int filledQuantity,
                               int remainingQuantity, Invoice invoice) {
        if (client == null || product == null || invoice == null
                || filledQuantity <= 0 || remainingQuantity < 0) {
            throw new IllegalArgumentException("Invalid shipment fulfillment result.");
        }
        this.client = client;
        this.product = product;
        this.filledQuantity = filledQuantity;
        this.remainingQuantity = remainingQuantity;
        this.invoice = invoice;
    }

    public Client getClient() {
        return client;
    }

    public Product getProduct() {
        return product;
    }

    public int getFilledQuantity() {
        return filledQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    @Override
    public String toString() {
        String details = "Client " + client.getClientID() + " (" + client.getName()
                + "): filled " + filledQuantity + " of " + product.getName()
                + ", charged $" + invoice.getTotal().toPlainString();
        if (remainingQuantity > 0) {
            details += ", still waiting for " + remainingQuantity;
        }
        return details;
    }
}
