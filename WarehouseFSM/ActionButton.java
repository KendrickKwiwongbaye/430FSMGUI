import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 * A button that carries its own action and listens to itself, the same idea as
 * ClerkButton in the instructor's sample code. Because every button knows what
 * it does, no code ever checks which button was clicked.
 */
public class ActionButton extends JButton implements ActionListener {
    private static final long serialVersionUID = 1L;

    private final transient Runnable action;

    public ActionButton(String label, Runnable action) {
        super(label);
        this.action = action;
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        action.run();
    }
}
