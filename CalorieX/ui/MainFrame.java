package ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;
import logics.controller.FoodController;
import logics.model.UserProfile;
import logics.user_info.UserDataBase;
import logics.validator.Validator;
import ui.components.*;
import ui.screens.*;

/**
  App shell that hosts all three main screens behind a CardLayout
 
    Dashboard— daily nutrition summary
    Calendar— food log history by month
    Settings — profile editor and reset

  A bottom navigation bar lets the user switch between screens.
 */
public class MainFrame {


    private static final String CARD_CALENDAR  = "calendar"; //left 
    private static final String CARD_DASHBOARD = "dashboard"; //middle
    private static final String CARD_SETTINGS  = "settings"; //right

    private JFrame appFrame;
    private JPanel cardContainer;
    private final List<JButton> navBarButtons  = new ArrayList<>(); //dynamic array
    private String activeCardName = CARD_DASHBOARD;

    // Constructor 
    public MainFrame(UserProfile userProfile, boolean isReturningUser) {
        buildAndShowAppFrame(userProfile); // creates the app (dashboard and stuff)
        if (isReturningUser) showWelcomeBackDialog(userProfile.getName()); // checks if returning user, pops up "Welcome back, y/n!"
    }

    // App frame setup
    private void buildAndShowAppFrame(UserProfile userProfile) {
        appFrame = new JFrame("CaloriX | Dashboard");
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setSize(1280, 760);
        appFrame.setLocationRelativeTo(null);
        appFrame.setLayout(new BorderLayout());
        appFrame.getContentPane().setBackground(Color.BLACK);
        
        // cardContainer {DashBoard,Calendar,Settings}
        cardContainer = new JPanel(new CardLayout());
        cardContainer.setBackground(Color.BLACK);
            
        DashboardPanel dashboardPanel = new DashboardPanel(userProfile);
        
        // if the user click on a specific date  "154"
        JPanel calendarCard = buildCalendarPanel(selectedDate -> {
            dashboardPanel.loadDate(selectedDate); // tells the dashboard to fetch the data for that day
            switchToCard(CARD_DASHBOARD);// when clicked, it switches to dashboard
            appFrame.setTitle("CaloriX | " + selectedDate);
        });

        JPanel settingsCard = buildSettingsPanel(userProfile, dashboardPanel);
        // Put all screens inside the container, but only show one at a time
        cardContainer.add(dashboardPanel.build(), CARD_DASHBOARD);
        cardContainer.add(calendarCard,CARD_CALENDAR);
        cardContainer.add(settingsCard, CARD_SETTINGS);

        appFrame.add(cardContainer,BorderLayout.CENTER); // with progress ring, macro cards, meal cards
        appFrame.add(buildBottomNavBar(), BorderLayout.SOUTH); // calendar, dashboard, settings

        refreshNavBarHighlight(); // highlight the active tab in the nav bar
        appFrame.setVisible(true);
    }

    // Bottom navigation bar 
    private JPanel buildBottomNavBar() {
        JPanel navBar = new JPanel(new GridLayout(1, 3));
        navBar.setBackground(Theme.BACKGROUND_DARK_CARD);
        navBar.setBorder(new MatteBorder(1, 0, 0, 0, Color.WHITE));
        navBar.setPreferredSize(new Dimension(1280, 64));
        navBar.add(buildNavBarButton("Calendar",CARD_CALENDAR));
        navBar.add(buildNavBarButton("Dashboard",CARD_DASHBOARD));
        navBar.add(buildNavBarButton("Settings", CARD_SETTINGS));
        return navBar;
    }

    //pass to BuildBottomNavBar ABOVE
    private JButton buildNavBarButton(String buttonLabel, String targetCardName) {
        JButton navButton = new JButton(buttonLabel);
        navButton.putClientProperty("targetCard", targetCardName); // hides - ENCAPSULATION
        navButton.setBackground(Theme.BACKGROUND_DARK_CARD);
        navButton.setForeground(Color.LIGHT_GRAY);
        navButton.setFont(Theme.bold(15));
        navButton.setFocusPainted(false); // removes the annoying outline box of the text
        navButton.setOpaque(true);
        navButton.setContentAreaFilled(true);
        navButton.setBorderPainted(false);
        navButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        navButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { // active 
                if (!activeCardName.equals(targetCardName))
                     navButton.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) { // not active
                if (!activeCardName.equals(targetCardName))
                     navButton.setForeground(Color.LIGHT_GRAY);
            }
        });

        navButton.addActionListener(e -> { // this changes the center (the one with- basta tay adda ta center)
            switchToCard(targetCardName);
            String capitalizedCardName = Character.toUpperCase(targetCardName.charAt(0)) + targetCardName.substring(1); // capitalizes (from "dashboard" to "Dashboard")
            appFrame.setTitle("CalorieX | " + capitalizedCardName);
        });

        navBarButtons.add(navButton); // put each navButton (c, d, s) into the list so we can loop through them later to change their colors when active/inactive
        return navButton;
    }

    // what actually changes screen
    private void switchToCard(String targetCardName) {
        activeCardName = targetCardName; // this is now the active page
        ((CardLayout) cardContainer.getLayout()).show(cardContainer, targetCardName); // switches screen
        refreshNavBarHighlight(); //updates
    }

    /* this is the LOOP mentioned line 120: navBarButtons.add(navButton) that loops through all 
     navButtons and changes their colors based on whether they are active or not */
    private void refreshNavBarHighlight() {
        Color inactiveBackground = Theme.BACKGROUND_DARK_CARD;
        Color activeBackground   = new Color(50, 50, 50);
        for (JButton navButton : navBarButtons) {
            String buttonTarget = (String) navButton.getClientProperty("targetCard");
            boolean isActiveTab = buttonTarget.equals(activeCardName);
            navButton.setBackground(isActiveTab ? activeBackground   : inactiveBackground);
            navButton.setForeground(isActiveTab ? Color.WHITE        : Color.LIGHT_GRAY);
        }
    }

    //This shows if profile.dat exist in data
    private void showWelcomeBackDialog(String userName) {
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.foreground", Color.BLACK);
        JLabel welcomeLabel = new JLabel("Welcome back, " + userName + "!");
        welcomeLabel.setFont(Theme.bold(18));
        JOptionPane.showMessageDialog(appFrame, welcomeLabel, "CalorieX", JOptionPane.PLAIN_MESSAGE);
    }

    // CALENDAR PANEL
    private YearMonth calendarDisplayMonth = YearMonth.now();
    private JPanel    calendarGridContainer;

    /* when user clicks a date, send me the selected date as a String 
            - user clicks the date
            - calendar reports it (04/01/2023 for example)
            - MainFrame recieves it (selectedDate) */
    private JPanel buildCalendarPanel(Consumer<String> onDateSelected) { 
        JPanel rootPanel = new JPanel(new BorderLayout(0, 12));
        rootPanel.setBackground(Color.BLACK);
        rootPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel calendarTitleLabel = new JLabel("Food Log Calendar");
        calendarTitleLabel.setFont(Theme.bold(20));
        calendarTitleLabel.setForeground(Color.WHITE);
        rootPanel.add(calendarTitleLabel, BorderLayout.NORTH);

        calendarGridContainer = new JPanel(new BorderLayout());
        calendarGridContainer.setOpaque(false);
        refreshCalendarGrid(onDateSelected);
        rootPanel.add(calendarGridContainer, BorderLayout.CENTER);
        return rootPanel;
    }

    // refreshes calendar when i press prev/next month or change year
    private void refreshCalendarGrid(Consumer<String> onDateSelected) {
        calendarGridContainer.removeAll();
        calendarGridContainer.add(buildMonthGrid(calendarDisplayMonth, onDateSelected), BorderLayout.CENTER);
        calendarGridContainer.revalidate();
        calendarGridContainer.repaint();
    }

    private JPanel buildMonthGrid(YearMonth yearMonth, Consumer<String> onDateSelected) {
        JPanel outerPanel = new JPanel(new BorderLayout(0, 12));
        outerPanel.setOpaque(false);

        // Month / year navigation row
        JPanel monthNavRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4)); // this creates (◄   May 2026   ►)
        monthNavRow.setOpaque(false);

        JButton prevMonthButton = new DarkButton("◄", Theme.BACKGROUND_DARK_CARD);
        JButton nextMonthButton = new DarkButton("►", Theme.BACKGROUND_DARK_CARD);

        String capitalizedMonthName = capitalizeFirstLetter(yearMonth.getMonth().name());
        JButton monthYearPickerButton = new JButton(capitalizedMonthName + "  " + yearMonth.getYear() + "  ▾");
        monthYearPickerButton.setFont(Theme.bold(16));
        monthYearPickerButton.setForeground(Color.WHITE);
        monthYearPickerButton.setBackground(Theme.BACKGROUND_DARK_CARD);
        monthYearPickerButton.setFocusPainted(false);
        monthYearPickerButton.setBorderPainted(false);
        monthYearPickerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        monthYearPickerButton.addActionListener(e ->
            showMonthYearPickerDialog(monthYearPickerButton, onDateSelected)); // opens a dialog to jump/change month/year

        monthNavRow.add(prevMonthButton);
        monthNavRow.add(monthYearPickerButton);
        monthNavRow.add(nextMonthButton);
        outerPanel.add(monthNavRow, BorderLayout.NORTH);

        // Day-of-week header row
        JPanel calendarDayGrid = new JPanel(new GridLayout(0, 7, 6, 6)); // this creates (Sun Mon Tue Wed Thu Fri Sat)
        calendarDayGrid.setOpaque(false);
        for (String dayAbbreviation : new String[]{ "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" }) {
            JLabel dayHeaderLabel = new JLabel(dayAbbreviation, SwingConstants.CENTER);
            dayHeaderLabel.setFont(Theme.bold(11));
            dayHeaderLabel.setForeground(Color.LIGHT_GRAY);
            calendarDayGrid.add(dayHeaderLabel);
        }

        // Offset empty cells for the first day of the month (leaving others blank)
        int startDayOfWeekOffset = yearMonth.atDay(1).getDayOfWeek().getValue() % 7;
        for (int offsetIndex = 0; offsetIndex < startDayOfWeekOffset; offsetIndex++) { // loop for empty space
            calendarDayGrid.add(new JLabel());
        }

        Set<String> datesWithLoggedFood = FoodController.getAllLoggedDates();

        for (int dayNumber = 1; dayNumber <= yearMonth.lengthOfMonth(); dayNumber++) {
            final String dayDateString  = yearMonth.atDay(dayNumber).toString();
            boolean hasLoggedFood = datesWithLoggedFood.contains(dayDateString);
            boolean isToday       = dayDateString.equals(LocalDate.now().toString());

            DarkRoundPanel dayCell = new DarkRoundPanel(10,
                isToday ? new Color(50, 50, 50) : Theme.BACKGROUND_DARK_CARD,
                isToday ? Color.WHITE            : Color.GRAY);
            dayCell.setLayout(new GridBagLayout());
            dayCell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dayCell.setPreferredSize(new Dimension(0, 52));

            JLabel dayNumberLabel = new JLabel(String.valueOf(dayNumber), SwingConstants.CENTER);
            dayNumberLabel.setFont(isToday ? Theme.bold(13) : Theme.plain(13));
            dayNumberLabel.setForeground(isToday ? Color.WHITE : Color.LIGHT_GRAY);

            if (hasLoggedFood) {
                // Show a dot indicator below the day number
                JLabel loggedFoodDotIndicator = new JLabel("●");
                loggedFoodDotIndicator.setForeground(Color.WHITE);
                loggedFoodDotIndicator.setFont(Theme.plain(8));

                JPanel dayNumberStack = new JPanel();
                dayNumberStack.setOpaque(false);
                dayNumberStack.setLayout(new BoxLayout(dayNumberStack, BoxLayout.Y_AXIS));
                dayNumberLabel.setAlignmentX(0.5f);
                loggedFoodDotIndicator.setAlignmentX(0.5f);
                dayNumberStack.add(dayNumberLabel);
                dayNumberStack.add(loggedFoodDotIndicator);
                dayCell.add(dayNumberStack);
            } else {
                dayCell.add(dayNumberLabel);
            }

            dayCell.addMouseListener(new MouseAdapter() { // When you click a day: Calendar → sends date → MainFrame
                @Override
                public void mouseClicked(MouseEvent e) { onDateSelected.accept(dayDateString); }
            });
            calendarDayGrid.add(dayCell); // puts each day into calendar
        }

        outerPanel.add(calendarDayGrid, BorderLayout.CENTER);

        prevMonthButton.addActionListener(e -> { // rebuilds calendar when you click ◄ or ►
            calendarDisplayMonth = calendarDisplayMonth.minusMonths(1);
            refreshCalendarGrid(onDateSelected);
        });
        nextMonthButton.addActionListener(e -> { // rebuilds calendar when you click ◄ or ►
            calendarDisplayMonth = calendarDisplayMonth.plusMonths(1);
            refreshCalendarGrid(onDateSelected);
        });

        return outerPanel;
    }


    /** It opens a popup where you choose a month and year, then updates your calendar. 
     * Open popup -> choose month/year -> click "Go" -> calendarDisplayMonth is updated -> 
     * calendar grid is rebuilt with the new month/year and shown on the calendar screen.
    */
    private void showMonthYearPickerDialog(Component anchorComponent, Consumer<String> onDateSelected) {
        JDialog pickerDialog = new JDialog(appFrame, "Go to Month / Year", true);
        pickerDialog.setSize(360, 240);
        pickerDialog.setLocationRelativeTo(appFrame);

        JPanel dialogContent = new JPanel(new GridBagLayout());
        dialogContent.setBackground(Theme.BACKGROUND_DARK_CARD);
        dialogContent.setBorder(new EmptyBorder(20, 28, 20, 28));

        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.insets = new Insets(8, 8, 8, 8);
        layoutConstraints.fill   = GridBagConstraints.HORIZONTAL;

        String[] monthNames = {
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        };
        DarkComboBox monthDropdown = new DarkComboBox(monthNames);
        monthDropdown.setSelectedIndex(calendarDisplayMonth.getMonthValue() - 1);
        monthDropdown.setPreferredSize(new Dimension(180, 34));

        int currentYear = calendarDisplayMonth.getYear();
        SpinnerNumberModel yearSpinnerModel = new SpinnerNumberModel(currentYear, currentYear - 50, currentYear + 50, 1);
        JSpinner yearSpinner = new JSpinner(yearSpinnerModel);
        JSpinner.NumberEditor yearNumberEditor = new JSpinner.NumberEditor(yearSpinner, "#");
        yearNumberEditor.getTextField().setBackground(Color.BLACK);
        yearNumberEditor.getTextField().setForeground(Color.WHITE);
        yearNumberEditor.getTextField().setFont(Theme.plain(13));
        yearSpinner.setEditor(yearNumberEditor);
        yearSpinner.setPreferredSize(new Dimension(180, 34));

        layoutConstraints.gridx = 0; layoutConstraints.gridy = 0; layoutConstraints.weightx = 0.35;
        JLabel monthPickerLabel = new JLabel("Month:");
        monthPickerLabel.setForeground(Color.LIGHT_GRAY);
        monthPickerLabel.setFont(Theme.plain(13));
        dialogContent.add(monthPickerLabel, layoutConstraints);
        layoutConstraints.gridx = 1; layoutConstraints.weightx = 0.65;
        dialogContent.add(monthDropdown, layoutConstraints);

        layoutConstraints.gridx = 0; layoutConstraints.gridy = 1; layoutConstraints.weightx = 0.35;
        JLabel yearPickerLabel = new JLabel("Year:");
        yearPickerLabel.setForeground(Color.LIGHT_GRAY);
        yearPickerLabel.setFont(Theme.plain(13));
        dialogContent.add(yearPickerLabel, layoutConstraints);
        layoutConstraints.gridx = 1; layoutConstraints.weightx = 0.65;
        dialogContent.add(yearSpinner, layoutConstraints);

        layoutConstraints.gridx = 0; layoutConstraints.gridy = 2;
        layoutConstraints.gridwidth = 2;
        layoutConstraints.insets = new Insets(18, 8, 4, 8);
        DarkButton goToMonthButton = new DarkButton("Go", Color.WHITE);
        goToMonthButton.setPreferredSize(new Dimension(0, 38));
        dialogContent.add(goToMonthButton, layoutConstraints);

        goToMonthButton.addActionListener(e -> {
            calendarDisplayMonth = YearMonth.of(
                (Integer) yearSpinner.getValue(),
                monthDropdown.getSelectedIndex() + 1);
            pickerDialog.dispose();
            refreshCalendarGrid(onDateSelected);
        });

        pickerDialog.setContentPane(dialogContent);
        pickerDialog.setVisible(true);
    }

    // It converts a word into “Capitalized form” (first letter uppercase, rest lowercase).
    private String capitalizeFirstLetter(String text) {
        return text.isEmpty() ? text : text.charAt(0) + text.substring(1).toLowerCase();
    }

    // SETTINGS PANEL
    private JPanel buildSettingsPanel(UserProfile userProfile, DashboardPanel dashboardPanel) {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.BLACK);
        rootPanel.setBorder(new EmptyBorder(24, 40, 24, 40));

        JLabel settingsTitleLabel = new JLabel("Profile Settings");
        settingsTitleLabel.setFont(Theme.bold(22));
        settingsTitleLabel.setForeground(Color.WHITE);
        settingsTitleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        rootPanel.add(settingsTitleLabel, BorderLayout.NORTH);

        DarkRoundPanel settingsCard = new DarkRoundPanel(18);
        settingsCard.setLayout(new GridBagLayout());
        settingsCard.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.insets  = new Insets(10, 10, 10, 10);
        layoutConstraints.fill    = GridBagConstraints.HORIZONTAL;
        layoutConstraints.anchor  = GridBagConstraints.WEST;

        DarkTextField ageEditField    = new DarkTextField(); 
        ageEditField.setText(String.valueOf(userProfile.getAge()));
        DarkTextField weightEditField = new DarkTextField(); 
        weightEditField.setText(String.valueOf(userProfile.getWeight()));
        DarkTextField heightEditField = new DarkTextField(); 
        heightEditField.setText(String.valueOf(userProfile.getHeight()));

        DarkComboBox goalDropdown = new DarkComboBox(new String[]{ "Lose Weight", "Maintain Weight", "Gain Weight" });
        goalDropdown.setSelectedItem(userProfile.getGoal());

        DarkComboBox genderDropdown = new DarkComboBox(new String[]{ "Male", "Female" });
        genderDropdown.setSelectedItem(userProfile.getGender());

        DarkComboBox activityDropdown = new DarkComboBox(new String[]{
            "Sedentary | No training",
            "Lightly Active | 1-2x / week",
            "Moderately Active | 3-5x / week",
            "Very Active | 6-7x / week",
            "Extra Active | Athlete / 2x daily"
        });
        activityDropdown.setSelectedItem(userProfile.getActivityLevel());

        Object[][] settingsRows = {
            { "Age",            ageEditField    },
            { "Weight (kg)",    weightEditField },
            { "Height (cm)",    heightEditField },
            { "Goal",           goalDropdown    },
            { "Gender",         genderDropdown  },
            { "Activity Level", activityDropdown },
        };

        for (int rowIndex = 0; rowIndex < settingsRows.length; rowIndex++) {
            layoutConstraints.gridx = 0; layoutConstraints.gridy = rowIndex; layoutConstraints.weightx = 0.3;
            JLabel settingFieldLabel = new JLabel((String) settingsRows[rowIndex][0]);
            settingFieldLabel.setFont(Theme.plain(14));
            settingFieldLabel.setForeground(Color.LIGHT_GRAY);
            settingsCard.add(settingFieldLabel, layoutConstraints);

            layoutConstraints.gridx = 1; layoutConstraints.weightx = 0.7;
            ((Component) settingsRows[rowIndex][1]).setPreferredSize(new Dimension(280, 36));
            settingsCard.add((Component) settingsRows[rowIndex][1], layoutConstraints);
        }

        // Save changes button
        layoutConstraints.gridx = 1; layoutConstraints.gridy = settingsRows.length;
        layoutConstraints.weightx = 0; layoutConstraints.anchor = GridBagConstraints.EAST;
        DarkButton saveProfileButton = new DarkButton("  Save Changes  ", Color.WHITE);
        saveProfileButton.setForeground(Color.BLACK);
        saveProfileButton.setPreferredSize(new Dimension(180, 40));
        settingsCard.add(saveProfileButton, layoutConstraints);

        saveProfileButton.addActionListener(e -> {
            String ageError    = Validator.validateAge(ageEditField.getText());
            String weightError = Validator.validateWeight(weightEditField.getText());
            String heightError = Validator.validateHeight(heightEditField.getText());
            for (String validationError : new String[]{ ageError, weightError, heightError }) {
                if (validationError != null) {
                    JOptionPane.showMessageDialog(appFrame, validationError, "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            userProfile.setAge(Integer.parseInt(ageEditField.getText().trim()));
            userProfile.setWeight(Float.parseFloat(weightEditField.getText().trim()));
            userProfile.setHeight(Float.parseFloat(heightEditField.getText().trim()));
            userProfile.setGoal((String) goalDropdown.getSelectedItem());
            userProfile.setGender((String) genderDropdown.getSelectedItem());
            userProfile.setActivityLevel((String) activityDropdown.getSelectedItem());

            UserDataBase.save(userProfile);
            dashboardPanel.onProfileUpdated();
            JOptionPane.showMessageDialog(appFrame, "Profile saved!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        });

        // Reset / delete data button
        layoutConstraints.gridx = 1; layoutConstraints.gridy = settingsRows.length + 1;
        layoutConstraints.insets = new Insets(24, 10, 10, 10);
        DarkButton resetAppButton = new DarkButton("  Reset App & Delete Profile  ", Color.LIGHT_GRAY);
        resetAppButton.setForeground(Color.BLACK);
        resetAppButton.setPreferredSize(new Dimension(280, 40));
        settingsCard.add(resetAppButton, layoutConstraints);

        resetAppButton.addActionListener(e -> {
            String[] resetChoiceOptions = { "Delete Profile Only", "Delete Everything", "Cancel" };
            int userResetChoice = JOptionPane.showOptionDialog(appFrame,
                "<html><b>Reset the app?</b><br><br>"
                + "<b>Delete Profile Only</b> — removes your profile; food log is kept.<br>"
                + "<b>Delete Everything</b> — removes profile AND all food logs.<br></html>",
                "Reset App", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, resetChoiceOptions, resetChoiceOptions[2]);

            if (userResetChoice == 2 || userResetChoice == JOptionPane.CLOSED_OPTION) return;

            new File("data/profile.dat").delete();
            if (userResetChoice == 1) new File("data/foodlog.dat").delete();

            appFrame.dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().start());
        });

        JPanel centeredCardWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centeredCardWrapper.setOpaque(false);
        centeredCardWrapper.add(settingsCard);
        rootPanel.add(centeredCardWrapper, BorderLayout.CENTER);
        return rootPanel;
    }
}
