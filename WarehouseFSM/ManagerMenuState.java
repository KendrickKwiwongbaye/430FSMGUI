/** Menu and operations available to a warehouse manager. */
public final class ManagerMenuState implements State {
    public static final int ID = 3;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "ManagerMenuState";
    }

    @Override
    public void run(Context context) {
        context.showScreen("ManagerMenuState: manager menu",
                context.button("Add a product", context::addProduct),
                context.button("Display a product waitlist", context::showProductWaitlist),
                context.button("Receive a shipment", context::receiveShipment),
                context.button("Become a clerk", context::becomeClerk),
                context.button("Save warehouse data", context::saveWarehouse),
                context.button("Logout", context::logout));
    }
}
