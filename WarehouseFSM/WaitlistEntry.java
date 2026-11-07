import java.io.Serializable;

/** A client's unfilled quantity for one product's FIFO waitlist. */
public final class WaitlistEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Client client;
    private int remainingQuantity;

    public WaitlistEntry(Client client, int quantity) {
        if (client == null || quantity <= 0) {
            throw new IllegalArgumentException("A client and positive quantity are required.");
        }
        this.client = client;
        this.remainingQuantity = quantity;
    }

    public Client getClient() {
        return client;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    /** Reduces the queued quantity when a future shipment fulfills part of it. */
    public void fulfill(int quantity) {
        if (quantity <= 0 || quantity > remainingQuantity) {
            throw new IllegalArgumentException("Fulfilled quantity is outside the queued amount.");
        }
        remainingQuantity -= quantity;
    }

    @Override
    public String toString() {
        return "Client " + client.getClientID() + " (" + client.getName()
                + "): waiting for " + remainingQuantity;
    }
}