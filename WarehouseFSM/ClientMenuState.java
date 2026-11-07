/** Menu and operations available to the active client. */
public final class ClientMenuState implements State {
    public static final int ID = 1;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "ClientMenuState";
    }

    @Override
    public void run(Context context) {
        context.showScreen("ClientMenuState: " + context.getActiveClient().getName()
                        + " (C" + context.getActiveClientId() + ")",
                context.button("Show client details", context::showClientDetails),
                context.button("Show products", context::showProducts),
                context.button("Show transactions", context::showTransactions),
                context.button("Add item to wishlist", context::addToWishlist),
                context.button("Display wishlist", context::showWishlist),
                context.button("Place order", context::placeOrder),
                context.button("Show invoices", context::showInvoices),
                context.button("Remove item from wishlist", context::removeFromWishlist),
                context.button("Logout", context::logout));
    }
}
