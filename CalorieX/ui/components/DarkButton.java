package ui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// DarkButton — styled button
public class DarkButton extends JButton {
    private final Color hoverBackground = Color.LIGHT_GRAY;

    public DarkButton(String buttonLabel, Color normalBackground) {
        super(buttonLabel);
        setBackground(normalBackground);
        setForeground(Color.BLACK);
        setFont(Theme.bold(13));
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { setBackground(hoverBackground); }
            public void mouseExited(MouseEvent e)  { setBackground(normalBackground); }
        });
    }

    public DarkButton(String buttonLabel) { this(buttonLabel, Color.WHITE); }
}
