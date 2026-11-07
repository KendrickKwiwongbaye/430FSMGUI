import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Immutable, dated record of products supplied to a client. */
public final class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private final List<LineItem> lineItems;
    private final BigDecimal total;

    public Invoice(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("An invoice must contain supplied items.");
        }
        this.date = LocalDate.now();
        this.lineItems = Collections.unmodifiableList(
                new ArrayList<LineItem>(lineItems));
        BigDecimal invoiceTotal = new BigDecimal("0.00");
        for (LineItem lineItem : this.lineItems) {
            if (lineItem == null) {
                throw new IllegalArgumentException("Invoice line items cannot be null.");
            }
            invoiceTotal = invoiceTotal.add(lineItem.getLineTotal());
        }
        this.total = invoiceTotal;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public BigDecimal getTotal() {
        return total;
    }

    @Override
    public String toString() {
        StringBuilder details = new StringBuilder("Invoice dated ").append(date);
        for (LineItem lineItem : lineItems) {
            details.append(System.lineSeparator()).append(lineItem);
        }
        return details.append(System.lineSeparator())
                .append("Total: $").append(total.toPlainString()).toString();
    }

    /** Immutable product and price snapshot for one supplied quantity. */
    public static final class LineItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int productID;
        private final String productName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal lineTotal;

        public LineItem(Product product, int quantity) {
            if (product == null || quantity <= 0) {
                throw new IllegalArgumentException("A product and positive supplied quantity are required.");
            }
            this.productID = product.getProductID();
            this.productName = product.getName();
            this.quantity = quantity;
            this.unitPrice = Money.positive(product.getSalePrice());
            this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }

        public int getProductID() {
            return productID;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        @Override
        public String toString() {
            return productName + " (P" + productID + ") x " + quantity
                    + " @ $" + unitPrice.toPlainString()
                    + " = $" + lineTotal.toPlainString();
        }
    }
}