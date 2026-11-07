import java.math.BigDecimal;
import java.math.RoundingMode;

/** Shared validation for prices, payments and charges, stored in whole cents. */
public final class Money {
    private Money() { }

    public static BigDecimal positive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Amount must be a whole number of cents.");
        }
    }
}
