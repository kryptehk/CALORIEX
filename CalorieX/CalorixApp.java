import javax.swing.SwingUtilities;
import ui.LoginFrame;

/**
    Profile exists? YES -> skip setup -> open MainFrame
    Profile exists? NO  -> show SetupFormPanel -> on submit -> open MainFrame
 */
public class CalorixApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().start());
    }
}
