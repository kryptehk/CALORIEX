package logics.food_entry;

import java.awt.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;
import logics.model.Food;

/**
 * Dialog that lets the user choose how to add a food to a meal:
 *   - Search from the reusable food library
 *   - Enter nutritional values manually
 */
public class AddFoodDialog extends JDialog {


    /**
     * Constructs a new dialog for adding food to a meal.
     * @param parentFrame = main application window, used as the owner for this dialog
     * @param mealName = the meal slot (e.g., "Breakfast") that the new food will be added to
     * @param dateString = the date (in ISO format, e.g. "2025-06-01") that the meal belongs to
     * @param onFoodAdded = A callback (Consumer) that receives the selected/created Food object.
     */
    public AddFoodDialog(JFrame parentFrame, String mealName, String dateString,
                         Consumer<Food> onFoodAdded) {
        super(parentFrame, "Add Food to " + mealName, true);
        setSize(400, 220);
        setLocationRelativeTo(parentFrame);
        setBackground(Color.BLACK);

        JPanel addFoodDialogPanel = new JPanel(new BorderLayout(0, 16));
        addFoodDialogPanel.setBackground(Color.BLACK);
        addFoodDialogPanel.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel promptLabel = new JLabel("How would you like to add food?");
        promptLabel.setForeground(Color.WHITE);
        promptLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        promptLabel.setHorizontalAlignment(SwingConstants.CENTER);
        addFoodDialogPanel.add(promptLabel, BorderLayout.NORTH);

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 16, 0));
        buttonRow.setOpaque(false);

        JButton searchLibraryButton = buildStyledButton("Search Library");
        JButton enterManuallyButton = buildStyledButton("Enter Manually");
        //dispose is clean up current dialog 
        searchLibraryButton.addActionListener(e -> {
            dispose();
            new SearchFoodDialog(parentFrame, mealName, dateString, onFoodAdded).setVisible(true);
        });

        enterManuallyButton.addActionListener(e -> {
            dispose();
            new ManualEntryDialog(parentFrame, mealName, dateString, onFoodAdded).setVisible(true);
        });

        buttonRow.add(searchLibraryButton);
        buttonRow.add(enterManuallyButton);
        addFoodDialogPanel.add(buttonRow, BorderLayout.CENTER);

        setContentPane(addFoodDialogPanel);
    }

    private JButton buildStyledButton(String buttonLabel) {
        JButton button = new JButton(buttonLabel);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(0, 42));
        return button;
    }
}
