package ui.components;

import java.awt.*;
import javax.swing.*;

// CalorieRing — circular progress ring
public class CalorieRing extends JPanel {
    private double caloriesConsumedToday = 0;
    private double dailyCalorieGoal      = 2000;

    public CalorieRing() {
        setOpaque(false);
        setPreferredSize(new Dimension(180, 180));
    }

    public void updateCalories(double caloriesConsumedToday, double dailyCalorieGoal) {
        this.caloriesConsumedToday = caloriesConsumedToday;
        this.dailyCalorieGoal      = dailyCalorieGoal;
        repaint();
    }

    // for the progress ring
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth   = getWidth();
        int panelHeight  = getHeight();
        int ringDiameter = Math.min(panelWidth, panelHeight) - 20; // 20 - margin space
        int ringOriginX  = (panelWidth  - ringDiameter) / 2; // perfectly centered ring
        int ringOriginY  = (panelHeight - ringDiameter) / 2; // perfectly centered ring, regardless of panel size
        int strokeWidth  = 14;

        boolean isOverGoal      = caloriesConsumedToday > dailyCalorieGoal && dailyCalorieGoal > 0;
        double  progressPercent = dailyCalorieGoal > 0
            ? Math.min(caloriesConsumedToday / dailyCalorieGoal, 1.0) : 0;
        int sweepAngleDegrees = (int) (360 * progressPercent);

        //Background track ring
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.GRAY);
        g2.drawOval(ringOriginX + strokeWidth / 2, ringOriginY + strokeWidth / 2,
                    ringDiameter - strokeWidth, ringDiameter - strokeWidth);

        //Filled progress arc
        g2.setColor(isOverGoal ? Color.LIGHT_GRAY : Color.WHITE);
        g2.drawArc(ringOriginX + strokeWidth / 2, ringOriginY + strokeWidth / 2,
                   ringDiameter - strokeWidth, ringDiameter - strokeWidth,
                   90, -sweepAngleDegrees);

        // Center label — shows remaining or overage calories
        String primaryCalorieLabel = isOverGoal
                                   ? "+" + (int)(caloriesConsumedToday - dailyCalorieGoal)
                                   : String.valueOf((int)(dailyCalorieGoal - caloriesConsumedToday));
        String calorieStatusLabel  = isOverGoal ? "OVER GOAL" : "REMAINING";
        Color  calorieLabelColor   = isOverGoal ? Color.LIGHT_GRAY : Color.WHITE;

        g2.setFont(Theme.bold(24));
        FontMetrics largeMetrics = g2.getFontMetrics();
        g2.setColor(calorieLabelColor);
        g2.drawString(primaryCalorieLabel,
                      panelWidth  / 2 - largeMetrics.stringWidth(primaryCalorieLabel) / 2,
                      panelHeight / 2 + 6);

        g2.setFont(Theme.plain(10));
        FontMetrics smallMetrics = g2.getFontMetrics();
        g2.setColor(isOverGoal ? Color.LIGHT_GRAY : Color.GRAY);
        g2.drawString(calorieStatusLabel,
                      panelWidth  / 2 - smallMetrics.stringWidth(calorieStatusLabel) / 2,
                      panelHeight / 2 + 22);

        g2.dispose();
    }
}
