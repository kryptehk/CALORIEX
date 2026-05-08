package logics.food_entry;

import java.awt.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;
import logics.controller.FoodController;
import logics.model.Food;
import logics.validator.Validator;

/**
 * Dialog for adding a new food entry manually, or editing an existing one.
 *
 *  opened from AddFoodDialog with no existing food.
 * The user fills in all fields from scratch.
 *
 * opened from EditSingleFoodDialog with a pre-populated food.
 * A "Delete Entry" button is also shown in this mode.
 */
public class ManualEntryDialog extends JDialog {

    // Add mode constructor 
    public ManualEntryDialog(JFrame parentFrame, String mealName, String dateString, Consumer<Food> onFoodAdded) {
        this(parentFrame, mealName, dateString, null, -1, onFoodAdded, null);
    }

    //Edit mode for library and dashboard edit
    public ManualEntryDialog(JFrame parentFrame, String mealName, String dateString,
    Food existingFood, int foodIndexInMeal, Consumer<Food> onFoodSaved, Runnable onFoodDeleted) {
        super(parentFrame, existingFood == null ? "Add Food Manually — " + mealName : "Edit Food — " + mealName, true);

        boolean isEditMode = existingFood != null; //check if there editing or adding 

        setSize(480, isEditMode ? 680 : 640); // if the user is editing, the window is slightly taller
        setLocationRelativeTo(parentFrame);

        // Root layout
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.BLACK);
        rootPanel.setBorder(new EmptyBorder(18, 26, 18, 26));

        JLabel dialogTitle = new JLabel(isEditMode ? "Edit Food Entry" : "Manual Food Entry");
        dialogTitle.setForeground(Color.WHITE);
        dialogTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        dialogTitle.setBorder(new EmptyBorder(0, 0, 14, 0));
        rootPanel.add(dialogTitle, BorderLayout.NORTH);

        //Pre-populate fields in edit mode 
        //Fix for: "Chicken (200 x 2)(200 x 3)(200 x 4)"
        String prefillName = isEditMode ? parseCleanName(existingFood.getName()) : "";
        String prefillServingGrams = isEditMode ? parseServingFromName(existingFood.getName()) : "100";
        String prefillQuantity = isEditMode ? parseQtyFromName(existingFood.getName()) : "1";

        double parsedQty; 
        try {
            parsedQty = Double.parseDouble(prefillQuantity.trim()); 
        } catch (NumberFormatException ex) { 
            parsedQty = 1; 
        }
        if (parsedQty <= 0) parsedQty = 1;
        int prefillCalories = isEditMode ? (int) Math.round(existingFood.getCalories() / parsedQty) : 0;
        //BUG FIXED
        // For macros, we reverse-calculate the per-serving values by dividing the totals by the quantity.
        double prefillProtein = isEditMode ? existingFood.getProtein() / parsedQty : 0;
        double prefillCarbs = isEditMode ? existingFood.getCarbs() / parsedQty : 0;
        double prefillFat = isEditMode ? existingFood.getFat() / parsedQty : 0;
        double prefillFibre = isEditMode ? existingFood.getFibre() / parsedQty : 0;
        double prefillSodium = isEditMode ? existingFood.getSodium() / parsedQty : 0;
        double prefillSugar = isEditMode ? existingFood.getSugar() / parsedQty : 0;

        //Form fields 
        JTextField foodNameField = buildTextField(isEditMode ? prefillName : "");
        JTextField servingSizeField = buildTextField(isEditMode ? prefillServingGrams : "100");
        JTextField quantityField = buildTextField(isEditMode ? prefillQuantity: "1");
        JTextField caloriesField = buildTextField(isEditMode ? String.valueOf(prefillCalories): "");
        JTextField proteinField = buildTextField(isEditMode ? formatDecimal(prefillProtein) : "");
        JTextField carbsField = buildTextField(isEditMode ? formatDecimal(prefillCarbs) : "");
        JTextField fatField = buildTextField(isEditMode ? formatDecimal(prefillFat): "");
        JTextField fibreField = buildTextField(isEditMode ? formatDecimal(prefillFibre): "");
        JTextField sodiumField  = buildTextField(isEditMode ? formatDecimal(prefillSodium): "");
        JTextField sugarField = buildTextField(isEditMode ? formatDecimal(prefillSugar): "");

        String[]     fieldLabels = {
            "Food Name", "Serving Size (g)", "Quantity",
            "Calories (kcal per serving)", "Protein (g)", "Carbs (g)",
            "Fat (g)", "Fibre (g)", "Sodium (mg)", "Sugar (g)"
        };
        JTextField[] inputFields = {
            foodNameField, servingSizeField, quantityField,
            caloriesField, proteinField, carbsField,
            fatField, fibreField, sodiumField, sugarField
        };

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.insets = new Insets(5, 5, 5, 5);
        layoutConstraints.fill   = GridBagConstraints.HORIZONTAL;

        for (int rowIndex = 0; rowIndex < fieldLabels.length; rowIndex++) {
            layoutConstraints.gridx = 0; layoutConstraints.gridy = rowIndex; layoutConstraints.weightx = 0.4;
            JLabel fieldLabel = new JLabel(fieldLabels[rowIndex]);
            fieldLabel.setForeground(Color.LIGHT_GRAY);
            fieldLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            formPanel.add(fieldLabel, layoutConstraints);

            layoutConstraints.gridx = 1; layoutConstraints.weightx = 0.6;
            inputFields[rowIndex].setPreferredSize(new Dimension(200, 30));
            formPanel.add(inputFields[rowIndex], layoutConstraints);
        }
        rootPanel.add(formPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel buttonPanel = new JPanel(new GridLayout(isEditMode ? 2 : 1, 1, 0, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        String saveButtonLabel = isEditMode ? "Save Changes" : "Add to " + mealName;
        JButton saveButton     = buildActionButton(saveButtonLabel, Color.WHITE, Color.BLACK);

        saveButton.addActionListener(e -> {

            // Validate required fields first
            String[] validationErrors = {
                Validator.validateName(foodNameField.getText()),
                Validator.validateCalories(caloriesField.getText()),
                Validator.validateMacroGrams(proteinField.getText(), "Protein"),
                Validator.validateMacroGrams(carbsField.getText(),"Carbs"),
                Validator.validateMacroGrams(fatField.getText(),"Fat")
            };
            for (String errorMessage : validationErrors) {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(this, errorMessage, "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            double servingGrams = parseDoubleOrZero(servingSizeField.getText());
            if (servingGrams <= 0) servingGrams = 100;
            double quantityCount = parseDoubleOrZero(quantityField.getText());
            if (quantityCount <= 0) quantityCount = 1;

            // Multiply per-serving values by quantity to get the total for this log entry
            int    totalCalories = (int) Math.round(Integer.parseInt(caloriesField.getText().trim()) * quantityCount);
            double totalProtein  = Double.parseDouble(proteinField.getText().trim()) * quantityCount;
            double totalCarbs = Double.parseDouble(carbsField.getText().trim()) * quantityCount;
            double totalFat = Double.parseDouble(fatField.getText().trim()) * quantityCount;
            double totalFibre = parseDoubleOrZero(fibreField.getText())  * quantityCount;
            double totalSodium = parseDoubleOrZero(sodiumField.getText()) * quantityCount;
            double totalSugar = parseDoubleOrZero(sugarField.getText())  * quantityCount;

            // Label encodes the serving info so it can be reverse-parsed later during edits
            String servingDisplay = formatDecimal(servingGrams) + "g x " + formatDecimal(quantityCount);
            String foodLabel = foodNameField.getText().trim() + " (" + servingDisplay + ")";

            Food savedFood = new Food(foodLabel, totalCalories, totalProtein, totalCarbs, totalFat,
            totalFibre, totalSodium, totalSugar);

            if (isEditMode) {
                // Only update the daily log when an actual date is present (not a library edit)
                if (!dateString.isEmpty()) FoodController.updateFoodInLog(dateString, mealName, foodIndexInMeal, savedFood);
                onFoodSaved.accept(savedFood);
            } else {
                FoodController.addFoodToLibrary(savedFood);
                // Only log to the daily meal if an actual date was provided (not a library-only add)
                if (!dateString.isEmpty()) FoodController.logFoodForMeal(dateString, mealName, savedFood);
                onFoodSaved.accept(savedFood);
            }
            dispose();
        });

        buttonPanel.add(saveButton);

        if (isEditMode) {
            JButton deleteButton = buildActionButton("Delete Entry", new Color(60, 20, 20), Color.WHITE);
            deleteButton.addActionListener(e -> {
                int userConfirmation = JOptionPane.showConfirmDialog(this,
                    "Delete \"" + existingFood.getName() + "\" from " + mealName + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (userConfirmation == JOptionPane.YES_OPTION) {
                    FoodController.removeFoodFromLog(dateString, mealName, foodIndexInMeal);
                    if (onFoodDeleted != null) onFoodDeleted.run();
                    dispose();
                }
            });
            buttonPanel.add(deleteButton);
        }

        rootPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    //helpers

    private JTextField buildTextField(String initialValue) {
        JTextField textField = new JTextField(initialValue);
        textField.setBackground(Color.BLACK);
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return textField;
    }

    private JButton buildActionButton(String buttonLabel, Color backgroundColor, Color foregroundColor) {
        JButton button = new JButton(buttonLabel);
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(0, 38));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private double parseDoubleOrZero(String inputText) {
        try { return Double.parseDouble(inputText.trim()); 
        } catch (NumberFormatException e) { return 0; }
    }

    /* Formats a decimal to 1 place, but omits ".0" for whole numbers. */
    private String formatDecimal(double value) {
        return value == (int) value ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    //FIXED for bugs during presentation

      // Pulls the serving grams out of a name encoded like "Chicken (200g x 2)" → "200"
    private String parseServingFromName(String foodName) {
        int openParen = foodName.lastIndexOf("(");
        int gIndex    = foodName.lastIndexOf("g x");
        if (openParen == -1 || gIndex == -1) return "100";
        return foodName.substring(openParen + 1, gIndex).trim();
    }
 
    // Pulls the quantity out of a name encoded like "Chicken (200g x 2)" → "2"
    private String parseQtyFromName(String foodName) {
        int gxIndex    = foodName.lastIndexOf("g x");
        int closeParen = foodName.lastIndexOf(")");
        if (gxIndex == -1 || closeParen == -1) return "1";
        return foodName.substring(gxIndex + 3, closeParen).trim();
    }
    // Strips the serving suffix out of "Chicken (200g x 2)" → "Chicken"
    private String parseCleanName(String foodName) {
    int openParen = foodName.lastIndexOf("(");
    int gIndex    = foodName.lastIndexOf("g x");
    if (openParen == -1 || gIndex == -1) return foodName;
    return foodName.substring(0, openParen).trim();
}

}