/** Menu and operations available to a warehouse clerk. */
public final class ClerkMenuState implements State {
    public static final int ID = 2;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "ClerkMenuState";
    }

    @Override
    public void run(Context context) {
        context.showScreen("ClerkMenuState: clerk menu",
                context.button("Add a client", context::addClient),
                context.button("Show products", context::showProducts),
                context.button("Show all clients", context::showClients),
                context.button("Show clients with outstanding balances",
                        context::showOutstandingBalances),
                context.button("Record a client payment", context::receivePayment),
                context.button("Become a client", context::becomeClient),
                context.button("Save warehouse data", context::saveWarehouse),
                context.button("Logout", context::logout));
    }
}
