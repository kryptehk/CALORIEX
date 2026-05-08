package ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import logics.model.UserProfile;
import ui.components.*;
import ui.screens.*;

/**
  App shell that hosts all four main screens behind a CardLayout
 
    Dashboard     — daily nutrition summary
    Calendar      — food log history by month
    Settings      — profile editor and reset
    Food Library  — reusable food library (add / delete)

  A bottom navigation bar lets the user switch between screens.
 */
public class MainFrame {

    private static final String CARD_CALENDAR     = "calendar";  //left
    private static final String CARD_DASHBOARD    = "dashboard"; //middle
    private static final String CARD_SETTINGS     = "settings";  //right
    private static final String CARD_FOOD_LIBRARY = "library";   //extra

    private JFrame       appFrame;
    private JPanel       cardContainer;
    private final List<JButton> navBarButtons = new ArrayList<>(); //dynamic array
    private String       activeCardName       = CARD_DASHBOARD;

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

        // cardContainer {Dashboard, Calendar, Settings, FoodLibrary}
        cardContainer = new JPanel(new CardLayout());
        cardContainer.setBackground(Color.BLACK);

        DashboardPanel   dashboardPanel   = new DashboardPanel(userProfile);
        FoodLibraryPanel foodLibraryPanel = new FoodLibraryPanel();

        // if the user click on a specific date  "154"
        CalendarPanel calendarPanel = new CalendarPanel();
        JPanel calendarCard = calendarPanel.build(selectedDate -> {
            dashboardPanel.loadDate(selectedDate); // tells the dashboard to fetch the data for that day
            switchToCard(CARD_DASHBOARD);          // when clicked, it switches to dashboard
            appFrame.setTitle("CaloriX | " + selectedDate);
        });

        SettingsPanel settingsPanel = new SettingsPanel(userProfile, dashboardPanel);

        // Put all screens inside the container, but only show one at a time
        cardContainer.add(dashboardPanel.build(),            CARD_DASHBOARD);
        cardContainer.add(calendarCard,                      CARD_CALENDAR);
        cardContainer.add(settingsPanel.build(appFrame),     CARD_SETTINGS);
        cardContainer.add(foodLibraryPanel.build(appFrame),  CARD_FOOD_LIBRARY);

        appFrame.add(cardContainer,     BorderLayout.CENTER); // with progress ring, macro cards, meal cards
        appFrame.add(buildBottomNavBar(), BorderLayout.SOUTH); // calendar, dashboard, settings, library

        refreshNavBarHighlight(); // highlight the active tab in the nav bar
        appFrame.setVisible(true);
    }

    // Bottom navigation bar
    private JPanel buildBottomNavBar() {
        JPanel navBar = new JPanel(new GridLayout(1, 4));
        navBar.setBackground(Theme.BACKGROUND_DARK_CARD);
        navBar.setBorder(new MatteBorder(1, 0, 0, 0, Color.WHITE));
        navBar.setPreferredSize(new Dimension(1280, 64));
        navBar.add(buildNavBarButton("Calendar",     CARD_CALENDAR));
        navBar.add(buildNavBarButton("Dashboard",    CARD_DASHBOARD));
        navBar.add(buildNavBarButton("Settings",     CARD_SETTINGS));
        navBar.add(buildNavBarButton("Food Library", CARD_FOOD_LIBRARY));
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
            String  buttonTarget = (String) navButton.getClientProperty("targetCard");
            boolean isActiveTab  = buttonTarget.equals(activeCardName);
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
}
