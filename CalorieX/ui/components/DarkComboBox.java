package ui.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

// DarkComboBox — styled dropdown selector for the gender, activity, & goal (login)
public class DarkComboBox extends JComboBox<String> {
    public DarkComboBox(String[] options) {
        super(options);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setFont(Theme.plain(13));
        setBorder(new LineBorder(Color.WHITE, 1, true));
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object cellValue,
                    int rowIndex, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, cellValue, rowIndex, isSelected, cellHasFocus);
                setBackground(isSelected ? Color.GRAY : Color.BLACK);
                setForeground(Color.WHITE);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }
}
