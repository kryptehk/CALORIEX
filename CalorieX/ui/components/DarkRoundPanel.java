package ui.components;

import java.awt.*;
import javax.swing.*;

// DarkRoundPanel — rounded-corner card panel used throughout the ui
public class DarkRoundPanel extends JPanel {
    private final int   cornerRadius;
    private final Color panelFillColor;
    private final Color panelStrokeColor;

    public DarkRoundPanel(int cornerRadius) {
        this(cornerRadius, new Color(20, 20, 20), Color.WHITE);
    }

    public DarkRoundPanel(int cornerRadius, Color panelFillColor, Color panelStrokeColor) {
        this.cornerRadius     = cornerRadius;
        this.panelFillColor   = panelFillColor;
        this.panelStrokeColor = panelStrokeColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(panelFillColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        g2.setColor(panelStrokeColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2.dispose();
        super.paintComponent(g);
    }
}
