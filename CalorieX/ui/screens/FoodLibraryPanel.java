package ui.screens;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import logics.controller.FoodController;
import logics.food_entry.ManualEntryDialog;
import logics.model.Food;
import ui.components.*;

/**
  Food Library screen — view, add, edit, and delete entries in the reusable food library.
  Add/Edit recycles ManualEntryDialog from the meal entry flow.
  Delete removes the selected item from the library immediately.
 */
public class FoodLibraryPanel {

    private JPanel foodRowsContainer;
    private JFrame parentFrame;

    public JPanel build(JFrame appFrame) {
        this.parentFrame = appFrame;

        JPanel rootPanel = new JPanel(new BorderLayout(0, 12));
        rootPanel.setBackground(Color.BLACK);
        rootPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        rootPanel.add(buildTopBar(),   BorderLayout.NORTH);
        rootPanel.add(buildListArea(), BorderLayout.CENTER);

        // Refresh every time the panel is shown — picks up foods added from meal cards
        rootPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) { refreshFoodRows(); }
        });

        return rootPanel;
    }

    // Top bar with screen title and "+ Add Food" button
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.BLACK);
        topBar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel screenTitleLabel = new JLabel("Food Library");
        screenTitleLabel.setFont(Theme.bold(22));
        screenTitleLabel.setForeground(Color.WHITE);

        JButton addFoodButton = new JButton("+ Add Food");
        addFoodButton.setBackground(Color.WHITE);
        addFoodButton.setForeground(Color.BLACK);
        addFoodButton.setFont(Theme.bold(13));
        addFoodButton.setFocusPainted(false);
        addFoodButton.setBorderPainted(false);
        addFoodButton.setOpaque(true);
        addFoodButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addFoodButton.setPreferredSize(new Dimension(130, 36));

        addFoodButton.addActionListener(e ->
            // Reuse ManualEntryDialog in add mode — "library" as the meal slot name, no date needed
            new ManualEntryDialog(parentFrame, "Library", "", addedFood -> {
                refreshFoodRows();
            }).setVisible(true));

        topBar.add(screenTitleLabel, BorderLayout.WEST);
        topBar.add(addFoodButton,    BorderLayout.EAST);
        return topBar;
    }

    // Scrollable list area that holds all food rows
    private JScrollPane buildListArea() {
        foodRowsContainer = new JPanel();
        foodRowsContainer.setLayout(new BoxLayout(foodRowsContainer, BoxLayout.Y_AXIS));
        foodRowsContainer.setBackground(Color.BLACK);

        refreshFoodRows();

        JScrollPane scrollPane = new JScrollPane(foodRowsContainer,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        return scrollPane;
    }

    // Rebuilds the food rows from the current library state
    void refreshFoodRows() {
        if (foodRowsContainer == null) return;
        foodRowsContainer.removeAll();

        List<Food> libraryFoods = FoodController.getFoodLibrary();

        if (libraryFoods.isEmpty()) {
            JLabel emptyStateLabel = new JLabel("Your food library is empty. Add something!");
            emptyStateLabel.setForeground(Color.GRAY);
            emptyStateLabel.setFont(Theme.plain(13));
            emptyStateLabel.setBorder(new EmptyBorder(16, 12, 12, 12));
            foodRowsContainer.add(emptyStateLabel);
        } else {
            for (int foodIndex = 0; foodIndex < libraryFoods.size(); foodIndex++) {
                final int  capturedIndex = foodIndex;
                final Food foodEntry     = libraryFoods.get(foodIndex);
                foodRowsContainer.add(buildFoodRow(foodEntry, capturedIndex));
                foodRowsContainer.add(Box.createVerticalStrut(4));
            }
        }

        foodRowsContainer.revalidate();
        foodRowsContainer.repaint();
    }

    // Single row: food name + macro summary + edit button + delete button
    private JPanel buildFoodRow(Food foodEntry, int foodIndex) {
        JPanel rowPanel = new JPanel(new BorderLayout(6, 0));
        rowPanel.setBackground(new Color(20, 20, 20));
        rowPanel.setBorder(new EmptyBorder(10, 14, 10, 14));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel foodNameLabel = new JLabel(foodEntry.getName());
        foodNameLabel.setForeground(Color.WHITE);
        foodNameLabel.setFont(Theme.bold(13));

        JLabel macroSummaryLabel = new JLabel(String.format(
            "<html><span style='color:#aaa'>%d kcal &nbsp; P:%.0fg &nbsp; C:%.0fg &nbsp; F:%.0fg</span></html>",
            foodEntry.getCalories(), foodEntry.getProtein(),
            foodEntry.getCarbs(),    foodEntry.getFat()));
        macroSummaryLabel.setFont(Theme.plain(11));

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.add(foodNameLabel);
        textStack.add(macroSummaryLabel);

        JButton editButton = new JButton("✎ edit");
        editButton.setBackground(new Color(20, 20, 20));
        editButton.setForeground(Color.LIGHT_GRAY);
        editButton.setFont(Theme.plain(11));
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setOpaque(true);
        editButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Open ManualEntryDialog in edit mode; on save replace in place, on delete remove it
        editButton.addActionListener(e ->
            new ManualEntryDialog(parentFrame, "Library", "", foodEntry, foodIndex,
                updatedFood -> {
                    FoodController.updateFoodInLibrary(foodIndex, updatedFood);
                    refreshFoodRows();
                },
                () -> {
                    FoodController.removeFoodFromLibrary(foodIndex);
                    refreshFoodRows();
                }).setVisible(true));

        JButton deleteButton = new JButton("✕");
        deleteButton.setBackground(new Color(20, 20, 20));
        deleteButton.setForeground(Color.GRAY);
        deleteButton.setFont(Theme.plain(12));
        deleteButton.setFocusPainted(false);
        deleteButton.setBorderPainted(false);
        deleteButton.setOpaque(true);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        deleteButton.addActionListener(e -> {
            int userConfirmation = JOptionPane.showConfirmDialog(parentFrame,
                "Remove \"" + foodEntry.getName() + "\" from the library?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (userConfirmation == JOptionPane.YES_OPTION) {
                FoodController.removeFoodFromLibrary(foodIndex);
                refreshFoodRows();
            }
        });

        JPanel actionButtonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionButtonRow.setOpaque(false);
        actionButtonRow.add(editButton);
        actionButtonRow.add(deleteButton);

        rowPanel.add(textStack,       BorderLayout.CENTER);
        rowPanel.add(actionButtonRow, BorderLayout.EAST);
        return rowPanel;
    }
}