/** Initial state for selecting a warehouse role. */
public final class OpeningState implements State {
    public static final int ID = 0;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "OpeningState";
    }

    @Override
    public void run(Context context) {
        context.showScreen("OpeningState: choose how to log in",
                context.button("Login as Client", context::loginAsClient),
                context.button("Login as Clerk", context::loginAsClerk),
                context.button("Login as Manager", context::loginAsManager),
                context.button("Exit", context::exit));
    }
}
