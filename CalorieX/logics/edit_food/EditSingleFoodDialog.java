package logics.edit_food;

import logics.food_entry.ManualEntryDialog;
import logics.model.Food;

import javax.swing.*;
import java.util.function.*;

/**
 * Opens the shared ManualEntryDialog in edit mode, pre-populated with the
 * existing food's values so the user can adjust serving size, quantity, or
 * any macro — and either save or delete the entry.
 *
 * This is a utility class — use the static {@code open()} method.
 */
public class EditSingleFoodDialog {

    /**
     * Opens the edit dialog for a specific logged food entry.
     *
     * @param parentFrame  = the parent window for the modal dialog
     * @param existingFood = the food entry to edit
     * @param mealName    = the meal this food belongs to
     * @param dateString   =  ISO date string of the log entry
     * @param foodIndexInMeal = position of the food in the meal list
     * @param onFoodUpdated = called with the updated Food after a save (null signals deletion)
     */
    public static void open(JFrame parentFrame, Food existingFood, //who open this and 
        String mealName, String dateString,  int foodIndexInMeal, Consumer<Food> onFoodUpdated) {

        new ManualEntryDialog(parentFrame, mealName, dateString,existingFood, foodIndexInMeal, onFoodUpdated,
            () -> onFoodUpdated.accept(null)  // null tells the caller the entry was deleted
        ).setVisible(true);
    }
}
