package logics.edit_food;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import logics.controller.FoodController;
import logics.model.Food;

/**
  Dialog that lists all foods logged in a specific meal, with per-item edit and delete.
  Clicking any food row opens ManualEntryDialog in edit mode.
 */
public class EditMealDialog extends JDialog {

    private final String   mealName;
    private final String   dateString;
    private final Runnable onMealChanged;
    private final JPanel   foodRowsPanel;

    public EditMealDialog(JFrame parentFrame, String mealName, String dateString,
                          Runnable onMealChanged) {
        super(parentFrame, "Edit Meal — " + mealName, true);
        this.mealName      = mealName;
        this.dateString    = dateString;
        this.onMealChanged = onMealChanged;

        setSize(500, 460);
        setLocationRelativeTo(parentFrame);

        JPanel rootPanel = new JPanel(new BorderLayout(0, 12));
        rootPanel.setBackground(Color.BLACK);
        rootPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel sectionTitle = new JLabel(mealName + " — Food Entries");
        sectionTitle.setForeground(Color.WHITE);
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        rootPanel.add(sectionTitle, BorderLayout.NORTH);

        foodRowsPanel = new JPanel();
        foodRowsPanel.setLayout(new BoxLayout(foodRowsPanel, BoxLayout.Y_AXIS));
        foodRowsPanel.setBackground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(foodRowsPanel);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        rootPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel editHintLabel = new JLabel("Click any row to edit or delete that entry.");
        editHintLabel.setForeground(Color.GRAY);
        editHintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rootPanel.add(editHintLabel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        refreshFoodRowList(parentFrame);
    }

    private void refreshFoodRowList(JFrame parentFrame) {
        foodRowsPanel.removeAll();

        java.util.List<Food> loggedFoods = FoodController.getMealsForDate(dateString)
                                            .getOrDefault(mealName, List.of());

        if (loggedFoods.isEmpty()) {
            JLabel emptyStateLabel = new JLabel("No foods logged in this meal yet.");
            emptyStateLabel.setForeground(Color.GRAY);
            emptyStateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            emptyStateLabel.setBorder(new EmptyBorder(12, 8, 12, 8));
            foodRowsPanel.add(emptyStateLabel);
        } else {
            for (int foodIndex = 0; foodIndex < loggedFoods.size(); foodIndex++) {
                final int    capturedIndex = foodIndex;
                final Food   foodEntry     = loggedFoods.get(foodIndex);
                JPanel       foodRow       = buildFoodRowPanel(foodEntry);

                foodRow.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent clickEvent) {
                        EditSingleFoodDialog.open(
                            parentFrame, foodEntry, mealName, dateString, capturedIndex,
                            updatedFood -> {
                                onMealChanged.run();
                                refreshFoodRowList(parentFrame);
                            });
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) { foodRow.setBackground(new Color(40, 40, 40)); }
                    @Override
                    public void mouseExited(MouseEvent e)  { foodRow.setBackground(new Color(20, 20, 20)); }
                });

                foodRowsPanel.add(foodRow);
                foodRowsPanel.add(Box.createVerticalStrut(4));
            }
        }

        foodRowsPanel.revalidate();
        foodRowsPanel.repaint();
    }

    private JPanel buildFoodRowPanel(Food foodEntry) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(new Color(20, 20, 20));
        rowPanel.setBorder(new EmptyBorder(10, 12, 10, 12));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        rowPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel foodNameLabel = new JLabel(foodEntry.getName());
        foodNameLabel.setForeground(Color.WHITE);
        foodNameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel macroSummaryLabel = new JLabel(String.format(
            "%d kcal  |  P: %.0fg  C: %.0fg  F: %.0fg",
            foodEntry.getCalories(), foodEntry.getProtein(),
            foodEntry.getCarbs(), foodEntry.getFat()
        ));
        macroSummaryLabel.setForeground(Color.LIGHT_GRAY);
        macroSummaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.add(foodNameLabel);
        textStack.add(macroSummaryLabel);

        JLabel editIconLabel = new JLabel("✎ edit");
        editIconLabel.setForeground(Color.GRAY);
        editIconLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        rowPanel.add(textStack,    BorderLayout.CENTER);
        rowPanel.add(editIconLabel, BorderLayout.EAST);
        return rowPanel;
    }
}
