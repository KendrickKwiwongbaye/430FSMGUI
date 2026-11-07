import java.io.Serializable;

/** Immutable result for one product line submitted from a wishlist. */
public final class OrderFulfillment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Product product;
    private final int requestedQuantity;
    private final int suppliedQuantity;
    private final int waitlistedQuantity;

    public OrderFulfillment(Product product, int requestedQuantity,
                            int suppliedQuantity, int waitlistedQuantity) {
        if (product == null || requestedQuantity <= 0 || suppliedQuantity < 0
                || waitlistedQuantity < 0
                || suppliedQuantity + waitlistedQuantity != requestedQuantity) {
            throw new IllegalArgumentException("Invalid order fulfillment result.");
        }
        this.product = product;
        this.requestedQuantity = requestedQuantity;
        this.suppliedQuantity = suppliedQuantity;
        this.waitlistedQuantity = waitlistedQuantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getSuppliedQuantity() {
        return suppliedQuantity;
    }

    public int getWaitlistedQuantity() {
        return waitlistedQuantity;
    }

    @Override
    public String toString() {
        return product.getName() + ": requested " + requestedQuantity
                + ", supplied " + suppliedQuantity
                + ", waitlisted " + waitlistedQuantity;
    }
}