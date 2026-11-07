import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Starts the graphical warehouse finite state machine. */
public final class UserInterface {
    private UserInterface() {
    }

    public static void main(String[] args) {
        // Keeps window frames and dialogs fully drawn when run on a remote display.
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(UserInterface::launch);
    }

    private static void launch() {
        new Context(selectWarehouse()).start();
    }

    private static StoreSystem selectWarehouse() {
        int answer = JOptionPane.showConfirmDialog(null,
                "Look for saved warehouse data and use it?", "Warehouse",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            StoreSystem saved = StoreSystem.retrieve();
            if (saved != null) {
                JOptionPane.showMessageDialog(null,
                        "The warehouse has been retrieved from WarehouseData.");
                return saved;
            }
            JOptionPane.showMessageDialog(null,
                    "No saved data found; starting a new warehouse.");
        }
        return StoreSystem.instance();
    }
}
