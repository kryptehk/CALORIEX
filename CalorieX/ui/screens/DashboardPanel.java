package ui.screens;

import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import logics.controller.FoodController;
import logics.edit_food.EditMealDialog;
import logics.food_entry.AddFoodDialog;
import logics.model.Food;
import logics.model.UserProfile;
import ui.components.*;

/**
  
  Layout:
    ┌── Top bar ──────────────────────────────┐
    │  "Daily Tracker"            date label  │
    ├── Stats section ──────────────────────  │
    │  CalorieRing  |  6 macro tiles          │
    ├── Meal section ──────────────────────── │
    │  Breakfast | Lunch | Dinner | Snacks    │
    └─────────────────────────────────────────┘
 */
public class DashboardPanel {

    private final UserProfile userProfile;
    private String viewingDateString = LocalDate.now().toString();

    // Running totals for the currently viewed date
    private double totalCaloriesToday, totalProteinToday, totalCarbsToday,
                   totalFatToday, totalFibreToday, totalSodiumToday, totalSugarToday;

    //Inner class: to tell other programmers that these are used for dashboard only
    private StatsSection statsSection;
    private MealSection mealSection;

    private JLabel currentDateLabel;
    private JFrame parentFrame;

    public DashboardPanel(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    //use to assemble
    public JPanel build() {
        /*
        creates two section
         */
        statsSection = new StatsSection(this);
        mealSection  = new MealSection(this);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.BLACK);
        /*
        Panel for "Daily Tracker" and "date string"
         */
        rootPanel.add(buildTopBar(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setBackground(Color.BLACK);
        /*this is is just padding so the text doesn't touch the edge */
        centerPanel.setBorder(new EmptyBorder(16, 24, 8, 24)); 
        /*calorie ring and other macro */
        centerPanel.add(statsSection.build(), BorderLayout.NORTH);
        /*Breakfast/dinner etc. */
        centerPanel.add(mealSection.build(),  BorderLayout.CENTER);

        rootPanel.add(centerPanel, BorderLayout.CENTER);

        /*
        when this method runs, you are creating The "Paper"(JPanel), but it's not in a folder yet(JFrame)
        AncestorListener waits for the jpanel to be added to a window
        */
        rootPanel.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent e) {
                parentFrame = (JFrame) SwingUtilities.getWindowAncestor(rootPanel);
            }
            public void ancestorRemoved(AncestorEvent e) {}
            public void ancestorMoved(AncestorEvent e)   {}
        });

        return rootPanel;
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.BLACK);
        topBar.setBorder(new EmptyBorder(16, 24, 8, 24));

        JLabel screenTitle = new JLabel("Daily Tracker");
        screenTitle.setFont(Theme.bold(22));
        screenTitle.setForeground(Color.WHITE);

        currentDateLabel = new JLabel(viewingDateString);
        currentDateLabel.setFont(Theme.bold(13));
        currentDateLabel.setForeground(Color.LIGHT_GRAY);

        topBar.add(screenTitle,      BorderLayout.WEST);
        topBar.add(currentDateLabel, BorderLayout.EAST);
        return topBar;
    }

    // used in mainframe
    public void loadDate(String newDateString) {
        viewingDateString = newDateString;
        currentDateLabel.setText(newDateString);
        recalcDailyTotals();
        mealSection.rebuildAll();
        statsSection.refresh();
    }

    // Called by the Settings panel after the profile is saved — updates targets without restarting.
    public void onProfileUpdated() {
        statsSection.refreshGoalLabels();
        statsSection.refresh();
    }

    //initialize values
    void clearDailyTotals() {
        totalCaloriesToday = totalProteinToday = totalCarbsToday =
        totalFatToday = totalFibreToday = totalSodiumToday = totalSugarToday = 0;
    }

    private void recalcDailyTotals() {
        
        clearDailyTotals(); 
        FoodController.getMealsForDate(viewingDateString)
            .values()
            .forEach(foodList -> foodList.forEach(this::addFoodToTotals));
    }
    
    //FOOD object
    void addFoodToTotals(Food loggedFood) {
        totalCaloriesToday += loggedFood.getCalories();
        totalProteinToday  += loggedFood.getProtein();
        totalCarbsToday    += loggedFood.getCarbs();
        totalFatToday      += loggedFood.getFat();
        totalFibreToday    += loggedFood.getFibre();
        totalSodiumToday   += loggedFood.getSodium();
        totalSugarToday    += loggedFood.getSugar();
    }

    void subtractFoodFromTotals(Food removedFood) {
        totalCaloriesToday -= removedFood.getCalories();
        totalProteinToday  -= removedFood.getProtein();
        totalCarbsToday    -= removedFood.getCarbs();
        totalFatToday      -= removedFood.getFat();
        totalFibreToday    -= removedFood.getFibre();
        totalSodiumToday   -= removedFood.getSodium();
        totalSugarToday    -= removedFood.getSugar();
    }

    

    // Getters
    double getTotalCalories() { return totalCaloriesToday; }
    double getTotalProtein()  { return totalProteinToday; }
    double getTotalCarbs()    { return totalCarbsToday; }
    double getTotalFat()      { return totalFatToday; }
    double getTotalFibre()    { return totalFibreToday; }
    double getTotalSodium()   { return totalSodiumToday; }
    double getTotalSugar()    { return totalSugarToday; }
    UserProfile getProfile()  { return userProfile; }
    String getViewingDate()   { return viewingDateString; }
    JFrame getParentFrame()   { return parentFrame; }


    // STATS SECTION — calorie ring + 6 macro tiles
    static class StatsSection {
        private final DashboardPanel dashboard;
        private CalorieRing calorieRingWidget;

        // Value labels (show consumed amounts)
        private JLabel proteinConsumedLabel, carbsConsumedLabel, fatConsumedLabel;
        private JLabel fibreConsumedLabel,   sodiumConsumedLabel, sugarConsumedLabel;

        // Goal labels (show daily targets)
        private JLabel proteinGoalLabel,  carbsGoalLabel,  fatGoalLabel;
        private JLabel fibreGoalLabel,    sodiumGoalLabel, sugarGoalLabel;

        StatsSection(DashboardPanel dashboard) { this.dashboard = dashboard; }

        /*The method creates one large container (statsRow) and splits it into two distinct zones using borderlayout */
        JPanel build() { //
            JPanel statsRow = new JPanel(new BorderLayout(24, 0));
            statsRow.setBackground(Color.BLACK);
            statsRow.setPreferredSize(new Dimension(1232, 220));

            // Calorie ring card 
            JPanel ringCard = new JPanel(new GridBagLayout()); //GridBagLayout allows to perfectly center the ring widget in side the card
            ringCard.setBackground(Theme.BACKGROUND_DARK_CARD);
            ringCard.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            ringCard.setPreferredSize(new Dimension(210, 210)); //forces the card to remain this size
            calorieRingWidget = new CalorieRing();
            calorieRingWidget.updateCalories(
                dashboard.getTotalCalories(),
                dashboard.getProfile().getDailyCalorieTarget());
            ringCard.add(calorieRingWidget);

            // Macro grid tiles
            JPanel macroTileGrid = new JPanel(new GridLayout(2, 3, 12, 12));
            macroTileGrid.setBackground(Color.BLACK);

            UserProfile profile = dashboard.getProfile();
            /*2d array is used so that it move as one */
            String[][] tileDefs = {
                {"PROTEIN", formatValue(dashboard.getTotalProtein()), formatValue(profile.getDailyProteinTargetGrams()),      "g"},
                {"CARBS", formatValue(dashboard.getTotalCarbs()), formatValue(profile.getDailyCarbohydrateTargetGrams()), "g"},
                {"FAT", formatValue(dashboard.getTotalFat()), formatValue(profile.getDailyFatTargetGrams()),           "g"},
                {"FIBRE", formatValue(dashboard.getTotalFibre()), formatValue(profile.getDailyFibreTargetGrams()),         "g"},
                {"SODIUM", formatValue(dashboard.getTotalSodium()), formatValue(profile.getDailySodiumTargetMilligrams()),  "mg"},
                {"SUGAR", formatValue(dashboard.getTotalSugar()), formatValue(profile.getDailySugarTargetGrams()),         "g"},
            };

            JLabel[][] labelPairs = new JLabel[tileDefs.length][2];
            for (int tileIndex = 0; tileIndex < tileDefs.length; tileIndex++) {
                labelPairs[tileIndex] = buildMacroTile(
                    macroTileGrid,
                    tileDefs[tileIndex][0],  // nutrient name
                    tileDefs[tileIndex][1],  // consumed amount
                    tileDefs[tileIndex][2],  // goal amount
                    tileDefs[tileIndex][3]); // unit
            }

            proteinConsumedLabel = labelPairs[0][0]; proteinGoalLabel = labelPairs[0][1];
            carbsConsumedLabel   = labelPairs[1][0]; carbsGoalLabel   = labelPairs[1][1];
            fatConsumedLabel     = labelPairs[2][0]; fatGoalLabel     = labelPairs[2][1];
            fibreConsumedLabel   = labelPairs[3][0]; fibreGoalLabel   = labelPairs[3][1];
            sodiumConsumedLabel  = labelPairs[4][0]; sodiumGoalLabel  = labelPairs[4][1];
            sugarConsumedLabel   = labelPairs[5][0]; sugarGoalLabel   = labelPairs[5][1];

            statsRow.add(ringCard,      BorderLayout.WEST);
            statsRow.add(macroTileGrid, BorderLayout.CENTER);
            return statsRow;
        }

        // Builds a single macro tile and returns [valueLabel, goalLabel].
        private JLabel[] buildMacroTile(JPanel parent, String nutrientName,
                                         String consumedAmount, String goalAmount, String unit) {
            JPanel tilePanel = new JPanel();
            tilePanel.setLayout(new BoxLayout(tilePanel, BoxLayout.Y_AXIS));
            tilePanel.setBackground(Theme.BACKGROUND_MACRO_TILE);
            tilePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel nutrientNameLabel = new JLabel(nutrientName);
            nutrientNameLabel.setFont(Theme.plain(11));
            nutrientNameLabel.setForeground(Color.LIGHT_GRAY);

            JLabel consumedValueLabel = new JLabel(consumedAmount + unit);
            consumedValueLabel.setFont(Theme.bold(26));
            consumedValueLabel.setForeground(Color.WHITE);

            JLabel dailyGoalLabel = new JLabel("/ " + goalAmount + unit + " goal");
            dailyGoalLabel.setFont(Theme.plain(10));
            dailyGoalLabel.setForeground(Color.GRAY);

            tilePanel.add(nutrientNameLabel);
            tilePanel.add(consumedValueLabel);
            tilePanel.add(dailyGoalLabel);
            parent.add(tilePanel);

            return new JLabel[]{ consumedValueLabel, dailyGoalLabel };
        }

        // Updates all consumed-amount labels and the calorie ring.
        void refresh() {
            calorieRingWidget.updateCalories(
                dashboard.getTotalCalories(),
                dashboard.getProfile().getDailyCalorieTarget());

            proteinConsumedLabel.setText(formatValue(dashboard.getTotalProtein()) + "g");
            carbsConsumedLabel.setText(formatValue(dashboard.getTotalCarbs())   + "g");
            fatConsumedLabel.setText(formatValue(dashboard.getTotalFat())     + "g");
            fibreConsumedLabel.setText(formatValue(dashboard.getTotalFibre())   + "g");
            sodiumConsumedLabel.setText(formatValue(dashboard.getTotalSodium())  + "mg");
            sugarConsumedLabel.setText(formatValue(dashboard.getTotalSugar())   + "g");
        }

        // Updates the goal labels after the user saves profile changes in Settings.
        void refreshGoalLabels() {
            UserProfile profile = dashboard.getProfile();
            proteinGoalLabel.setText("/ " + formatValue(profile.getDailyProteinTargetGrams())      + "g goal");
            carbsGoalLabel.setText("/ " + formatValue(profile.getDailyCarbohydrateTargetGrams()) + "g goal");
            fatGoalLabel.setText("/ " + formatValue(profile.getDailyFatTargetGrams())           + "g goal");
            fibreGoalLabel.setText("/ " + formatValue(profile.getDailyFibreTargetGrams())         + "g goal");
            sodiumGoalLabel.setText("/ " + formatValue(profile.getDailySodiumTargetMilligrams())   + "mg goal");
            sugarGoalLabel.setText("/ " + formatValue(profile.getDailySugarTargetGrams())         + "g goal");
        }

        // Formats a double as a whole number or 1-decimal string.
        static String formatValue(double rawValue) {
            return rawValue == (int) rawValue
                ? String.valueOf((int) rawValue)
                : String.format("%.1f", rawValue);
        }
    }


    // MEAL SECTION — four meal cards: Breakfast, Lunch, Dinner, Snacks
    static class MealSection {
        private static final String[] MEAL_NAMES = { "Breakfast", "Lunch", "Dinner", "Snacks" };
        /*reference to the main dashboard to access global data like dates, calories*/
        private final DashboardPanel dashboard; 
        //A dictionary that stores the "List Panel" for each meal name. This allows you to update only one meal card at a time.
        private final Map<String, JPanel> mealContentPanelMap = new LinkedHashMap<>(); //
        //A dictionary that stores the "Calories Label" for each meal, allowing you to change the text dynamically when food is added/removed
        private final Map<String, JLabel> mealCalorieLabelMap = new LinkedHashMap<>();

        MealSection(DashboardPanel dashboard) {
             this.dashboard = dashboard; 
            }

        JPanel build() {
            JPanel mealGrid = new JPanel(new GridLayout(2, 2, 14, 14));
            mealGrid.setBackground(Color.BLACK);
            //loops through array and creates ui card for each one
            for (String mealName : MEAL_NAMES) mealGrid.add(buildMealCard(mealName));
            return mealGrid;
        }

        private JPanel buildMealCard(String mealName) {
            JPanel cardPanel = new JPanel(new BorderLayout());
            cardPanel.setBackground(Theme.BACKGROUND_MEAL_CARD);
            cardPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

            JLabel mealTitleLabel = new JLabel(mealName);
            mealTitleLabel.setFont(Theme.bold(17));
            mealTitleLabel.setForeground(Color.WHITE);

            JLabel mealCalorieSummary = new JLabel("0 kcal");
            mealCalorieSummary.setFont(Theme.plain(11));
            mealCalorieSummary.setForeground(Color.LIGHT_GRAY);
            mealCalorieLabelMap.put(mealName, mealCalorieSummary);

            JPanel mealTitleStack = new JPanel();
            mealTitleStack.setOpaque(false);
            mealTitleStack.setLayout(new BoxLayout(mealTitleStack, BoxLayout.Y_AXIS));
            mealTitleStack.add(mealTitleLabel);
            mealTitleStack.add(mealCalorieSummary);

            JButton editMealButton = buildCardButton("Edit", Theme.BACKGROUND_MEAL_CARD, Color.LIGHT_GRAY);
            JButton addFoodButton  = buildCardButton("+",    Color.WHITE,                Color.BLACK);
            addFoodButton.setFont(Theme.bold(20));

            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttonRow.setOpaque(false);
            buttonRow.add(editMealButton);
            buttonRow.add(addFoodButton);

            JPanel cardHeader = new JPanel(new BorderLayout());
            cardHeader.setOpaque(false);
            cardHeader.add(mealTitleStack, BorderLayout.WEST);
            cardHeader.add(buttonRow,      BorderLayout.EAST);

            JPanel foodListContent = new JPanel();
            foodListContent.setOpaque(false);
            foodListContent.setLayout(new BoxLayout(foodListContent, BoxLayout.Y_AXIS));
            mealContentPanelMap.put(mealName, foodListContent);

            JScrollPane foodScrollPane = new JScrollPane(foodListContent,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            foodScrollPane.setOpaque(false);
            foodScrollPane.getViewport().setOpaque(false);
            foodScrollPane.setBorder(null);

            cardPanel.add(cardHeader,     BorderLayout.NORTH);
            cardPanel.add(foodScrollPane, BorderLayout.CENTER);
            
                // +Button
            addFoodButton.addActionListener(e ->
                new AddFoodDialog(dashboard.getParentFrame(), mealName, dashboard.getViewingDate(),
                    addedFood -> {
                        dashboard.addFoodToTotals(addedFood);
                        dashboard.statsSection.refresh();
                        rebuildMealCard(mealName);
                    }).setVisible(true));

            editMealButton.addActionListener(e ->
                new EditMealDialog(dashboard.getParentFrame(), mealName, dashboard.getViewingDate(),
                    () -> {
                        dashboard.recalcDailyTotals();
                        dashboard.statsSection.refresh();
                        rebuildMealCard(mealName);
                    }).setVisible(true));

            rebuildMealCard(mealName);
            return cardPanel;
        }

        void rebuildMealCard(String mealName) {
            JPanel contentPanel = mealContentPanelMap.get(mealName);
            if (contentPanel == null) return;
            contentPanel.removeAll();

            java.util.List<Food> loggedFoods = FoodController.getMealsForDate(dashboard.getViewingDate())
                                                              .getOrDefault(mealName, List.of());

            if (loggedFoods.isEmpty()) {
                JLabel emptyStateLabel = new JLabel("No food logged yet");
                emptyStateLabel.setForeground(Color.GRAY);
                emptyStateLabel.setFont(Theme.plain(12));
                emptyStateLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
                contentPanel.add(emptyStateLabel);
            } else {
                for (int foodIndex = 0; foodIndex < loggedFoods.size(); foodIndex++) {
                    final int  capturedIndex = foodIndex;
                    final Food foodEntry     = loggedFoods.get(foodIndex);
                    contentPanel.add(buildFoodItemRow(foodEntry, mealName, capturedIndex));
                    contentPanel.add(Box.createVerticalStrut(3));
                }
            }

            int totalMealCalories = loggedFoods.stream().mapToInt(Food::getCalories).sum();
            mealCalorieLabelMap.get(mealName).setText(totalMealCalories + " kcal");
            contentPanel.revalidate();
            contentPanel.repaint();
        }

        void rebuildAll() { 
            for (String mealName : MEAL_NAMES) rebuildMealCard(mealName); 
        }

        private JPanel buildFoodItemRow(Food foodEntry, String mealName, int foodIndex) {
            JPanel rowPanel = new JPanel(new BorderLayout(6, 0));
            rowPanel.setOpaque(false);
            rowPanel.setBorder(new EmptyBorder(4, 0, 4, 0));
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

            JLabel foodNameLabel = new JLabel(foodEntry.getName());
            foodNameLabel.setForeground(Color.WHITE);
            foodNameLabel.setFont(Theme.bold(12));

            JLabel macroDetailLabel = new JLabel(String.format(
                "<html><span style='color:#aaa'>%d kcal &nbsp; P:%.0fg &nbsp; C:%.0fg &nbsp; F:%.0fg &nbsp; Na:%.0fmg &nbsp; Sugar:%.0fg</span></html>",
                foodEntry.getCalories(), foodEntry.getProtein(), foodEntry.getCarbs(),
                foodEntry.getFat(), foodEntry.getSodium(), foodEntry.getSugar()));
            macroDetailLabel.setFont(Theme.plain(10));

            JPanel textStack = new JPanel();
            textStack.setOpaque(false);
            textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
            textStack.add(foodNameLabel);
            textStack.add(macroDetailLabel);

            JButton removeFoodButton = new JButton("✕");
            removeFoodButton.setBackground(new Color(25, 25, 25));
            removeFoodButton.setForeground(Color.GRAY);
            removeFoodButton.setFont(Theme.plain(10));
            removeFoodButton.setFocusPainted(false);
            removeFoodButton.setBorderPainted(false);
            removeFoodButton.setOpaque(true);
            removeFoodButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            removeFoodButton.addActionListener(e -> {
                FoodController.removeFoodFromLog(dashboard.getViewingDate(), mealName, foodIndex);
                dashboard.subtractFoodFromTotals(foodEntry);
                dashboard.statsSection.refresh();
                rebuildMealCard(mealName);
            });

            rowPanel.add(textStack,        BorderLayout.CENTER);
            rowPanel.add(removeFoodButton, BorderLayout.EAST);
            return rowPanel;
        }

        private JButton buildCardButton(String buttonLabel, Color backgroundColor, Color foregroundColor) {
            JButton button = new JButton(buttonLabel);
            button.setBackground(backgroundColor);
            button.setForeground(foregroundColor);
            button.setFont(Theme.bold(13));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }
    }
}
