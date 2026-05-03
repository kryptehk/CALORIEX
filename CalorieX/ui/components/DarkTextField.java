package ui.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

// DarkTextField — styled single-line text input for the dark theme
public class DarkTextField extends JTextField {
    public DarkTextField() {
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setFont(Theme.plain(13));
        setBorder(new CompoundBorder(
            new LineBorder(Color.WHITE, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
    }
}
