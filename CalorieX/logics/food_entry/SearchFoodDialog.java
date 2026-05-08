package logics.food_entry;

import logics.controller.FoodController;
import logics.model.Food;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.*;

/**
 * Dialog for searching the food library and logging a selected item to a meal.

 */
public class SearchFoodDialog extends JDialog {
    //holds the list of foods
    private final DefaultListModel<Food> foodListModel = new DefaultListModel<>();
    //displays the data
    private final JList<Food>foodResultsList = new JList<>(foodListModel);

    /**
       @param parentFrame - where the dialog is attached to
       @param mealName - Breakfast etc.
       @param dateString - which day
       @param onFoodAdded -callback function after adding food
       consumer<> lets the ui update this dialog like refresh dashboard
    */

    public SearchFoodDialog(JFrame parentFrame, String mealName, String dateString, Consumer<Food> onFoodAdded) {
        super(parentFrame, "Search Food Library — " + mealName, true);
        setSize(480, 420);
        setLocationRelativeTo(parentFrame);

        JPanel rootPanel = new JPanel(new BorderLayout(0, 12));
        rootPanel.setBackground(Color.BLACK);
        rootPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

      
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

        rootPanel.add(resultsScrollPane,     BorderLayout.CENTER);
        rootPanel.add(logSelectedFoodButton, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private void showAllLibraryResults() {
        foodListModel.clear();
        FoodController.getFoodLibrary().forEach(foodListModel::addElement);
    }

  // Cell renderer showing name + macro summary 
private static class FoodLibraryListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list, Object cellValue,
            int rowIndex, boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, cellValue, rowIndex, isSelected, cellHasFocus);

        if (cellValue instanceof Food) { // check if the list is actually food object
            Food foodItem = (Food) cellValue;

            // simple readable format
            setText(
                foodItem.getName() + " - " +
                foodItem.getCalories() + " kcal " +
                "P:" + (int)foodItem.getProtein() + "g " +
                "C:" + (int)foodItem.getCarbs() + "g " +
                "F:" + (int)foodItem.getFat() + "g"
            );
        }

        setBackground(isSelected ? Color.DARK_GRAY : new Color(20, 20, 20));
        setForeground(Color.WHITE);
        setBorder(new EmptyBorder(6, 10, 6, 10));

        return this;
    }
    }
}