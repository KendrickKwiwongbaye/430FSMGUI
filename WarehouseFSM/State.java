/** A state in the warehouse user-interface finite state machine. */
public interface State {
    int getId();

    String getName();

    /** Shows this state's title and buttons in the window, then returns. */
    void run(Context context);
}
