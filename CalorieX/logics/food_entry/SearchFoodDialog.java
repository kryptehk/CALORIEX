package logics.food_entry;

import logics.controller.FoodController;
import logics.model.Food;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.*;

/**
 * Dialog for searching the food library and logging a selected item to a meal.
 * The list filters in real time as the user types in the search field.
 */
public class SearchFoodDialog extends JDialog {

    private final DefaultListModel<Food> foodListModel = new DefaultListModel<>();
    private final JList<Food>            foodResultsList = new JList<>(foodListModel);

    public SearchFoodDialog(JFrame parentFrame, String mealName, String dateString,
                            Consumer<Food> onFoodAdded) {
        super(parentFrame, "Search Food Library — " + mealName, true);
        setSize(480, 420);
        setLocationRelativeTo(parentFrame);

        JPanel rootPanel = new JPanel(new BorderLayout(0, 12));
        rootPanel.setBackground(Color.BLACK);
        rootPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField searchInputField = new JTextField();
        searchInputField.setBackground(Color.BLACK);
        searchInputField.setForeground(Color.WHITE);
        searchInputField.setCaretColor(Color.WHITE);
        searchInputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchInputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        searchInputField.setToolTipText("Type to search food library...");

        foodResultsList.setBackground(new Color(20, 20, 20));
        foodResultsList.setForeground(Color.WHITE);
        foodResultsList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        foodResultsList.setSelectionBackground(Color.DARK_GRAY);
        foodResultsList.setSelectionForeground(Color.WHITE);
        foodResultsList.setCellRenderer(new FoodLibraryListCellRenderer());

        JScrollPane resultsScrollPane = new JScrollPane(foodResultsList);
        resultsScrollPane.setBackground(Color.BLACK);
        resultsScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        resultsScrollPane.getViewport().setBackground(new Color(20, 20, 20));

        JButton logSelectedFoodButton = new JButton("Log Selected Food");
        logSelectedFoodButton.setBackground(Color.WHITE);
        logSelectedFoodButton.setForeground(Color.BLACK);
        logSelectedFoodButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logSelectedFoodButton.setFocusPainted(false);
        logSelectedFoodButton.setBorderPainted(false);
        logSelectedFoodButton.setOpaque(true);
        logSelectedFoodButton.setPreferredSize(new Dimension(0, 40));
        logSelectedFoodButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        showAllLibraryResults();

        searchInputField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterResultsByQuery(); }
            public void removeUpdate(DocumentEvent e)  { filterResultsByQuery(); }
            public void changedUpdate(DocumentEvent e) { filterResultsByQuery(); }

            private void filterResultsByQuery() {
                String searchQuery = searchInputField.getText().trim().toLowerCase();
                if (searchQuery.isEmpty()) {
                    showAllLibraryResults();
                } else {
                    List<Food> matchingFoods = FoodController.getFoodLibrary().stream()
                        .filter(food -> food.getName().toLowerCase().contains(searchQuery))
                        .collect(Collectors.toList());
                    foodListModel.clear();
                    matchingFoods.forEach(foodListModel::addElement);
                }
            }
        });

        logSelectedFoodButton.addActionListener(e -> {
            Food selectedFood = foodResultsList.getSelectedValue();
            if (selectedFood == null) {
                JOptionPane.showMessageDialog(this, "Please select a food from the list.");
                return;
            }
            FoodController.logFoodForMeal(dateString, mealName, selectedFood);
            onFoodAdded.accept(selectedFood);
            dispose();
        });

        // Double-click logs the food immediately (same as clicking the button)
        foodResultsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent clickEvent) {
                if (clickEvent.getClickCount() == 2) logSelectedFoodButton.doClick();
            }
        });

        rootPanel.add(searchInputField,      BorderLayout.NORTH);
        rootPanel.add(resultsScrollPane,     BorderLayout.CENTER);
        rootPanel.add(logSelectedFoodButton, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private void showAllLibraryResults() {
        foodListModel.clear();
        FoodController.getFoodLibrary().forEach(foodListModel::addElement);
    }

    // ── Cell renderer showing name + macro summary ────────────────────────────

    private static class FoodLibraryListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object cellValue,
                int rowIndex, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, cellValue, rowIndex, isSelected, cellHasFocus);
            if (cellValue instanceof Food) {
                Food foodItem = (Food) cellValue;
                setText(String.format(
                    "<html><b>%s</b>&nbsp;<span style='color:#aaa'>%d kcal&nbsp;P:%.0fg&nbsp;C:%.0fg&nbsp;F:%.0fg</span></html>",
                    foodItem.getName(), foodItem.getCalories(),
                    foodItem.getProtein(), foodItem.getCarbs(), foodItem.getFat()));
            }
            setBackground(isSelected ? Color.DARK_GRAY : new Color(20, 20, 20));
            setForeground(Color.WHITE);
            setBorder(new EmptyBorder(6, 10, 6, 10));
            return this;
        }
    }
}
