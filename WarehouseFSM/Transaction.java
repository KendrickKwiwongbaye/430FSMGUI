import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Immutable account entry: charges are positive and payments are negative. */
public final class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private final String description;
    private final BigDecimal amount;

    public Transaction(String description, BigDecimal amount) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction description is required.");
        }
        if (amount == null || amount.signum() == 0) {
            throw new IllegalArgumentException("Transaction amount must be nonzero.");
        }
        BigDecimal magnitude = Money.positive(amount.abs());
        this.amount = amount.signum() < 0 ? magnitude.negate() : magnitude;
        this.description = description.trim();
        this.date = LocalDate.now();
    }

    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }

    @Override
    public String toString() {
        return date + " | " + description + " | "
                + (amount.signum() < 0 ? "Credit: $" : "Debit: $")
                + amount.abs().toPlainString();
    }
}
