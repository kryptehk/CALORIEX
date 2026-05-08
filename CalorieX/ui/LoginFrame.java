package ui;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import logics.model.UserProfile;
import logics.user_info.UserDataBase;
import logics.validator.Validator;
import ui.components.*;

/**
 The first screen the user sees on app launch.
  If a saved profile is found on disk, the user goes straight to MainFrame.
 Otherwise, a setup form is shown to collect the user's profile details.
 */
//I remove the java2d background class and other java2d components
//Change GlowingTitleLabel to AppTitleLabel
public class LoginFrame {

    
    //App title label pixel 
    private class AppTitleLabel extends JLabel {
        AppTitleLabel(String labelText, Font labelFont) {
            super(labelText);
            setFont(labelFont);
            setForeground(Color.WHITE);
        }

      
    }

    // Custom font for the app title, with a fallback to a default font if loading fails.
    private Font loadUpheavalFont(float desiredSize) {
        try {
            InputStream fontStream = getClass().getResourceAsStream("/ui/fonts/upheavtt.ttf");
            if (fontStream == null) return new Font("SansSerif", Font.BOLD, (int) desiredSize);
            return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(desiredSize);
        } catch (FontFormatException | IOException fontLoadException) {
            return new Font("SansSerif", Font.BOLD, (int) desiredSize);
        }
    }

    // Entry point 

    public void start() {
        UserProfile savedProfile = UserDataBase.load();
        if (savedProfile != null) {
            SwingUtilities.invokeLater(() -> new MainFrame(savedProfile, true));
            return;
        }
        SwingUtilities.invokeLater(this::showSetupForm);
    }

    // Shows when there is no "profile.dat" in data
    private void showSetupForm() {
        JFrame setupFrame = new JFrame("CaloriX - Setup");
        setupFrame.setSize(1280, 720);
        setupFrame.setLocationRelativeTo(null);
        setupFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel rootPanel = new JPanel();
        rootPanel.setBackground(Color.BLACK);
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBorder(new EmptyBorder(40, 60, 40, 60));
        rootPanel.add(buildHeadingPanel(),  BorderLayout.NORTH);
        rootPanel.add(buildFormCard(setupFrame), BorderLayout.CENTER);

        setupFrame.add(rootPanel);
        setupFrame.setVisible(true);
    }

    //called in showSetupForm
    private JPanel buildHeadingPanel() {
        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        AppTitleLabel AppTitleLabel = new AppTitleLabel("CALORIX", loadUpheavalFont(80f));
        AppTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headingPanel.add(AppTitleLabel);
        headingPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("Calorie and Macro Tracker");
        subtitleLabel.setFont(new Font("Satoshi", Font.PLAIN, 22));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headingPanel.add(Box.createVerticalStrut(3));
        headingPanel.add(subtitleLabel);
        headingPanel.add(Box.createVerticalStrut(28));

        return headingPanel;
    }


    //the box below the headers
    private DarkRoundPanel buildFormCard(JFrame parentFrame) {
        DarkRoundPanel formCard = new DarkRoundPanel(20);
        formCard.setLayout(new GridBagLayout());
        formCard.setBackground(Color.BLACK);
        formCard.setOpaque(true);
        formCard.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        //
        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.insets = new Insets(8, 8, 8, 8); //spacing
        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;

        Font inputFont = new Font("Satoshi", Font.PLAIN, 15);

        //input fields
        DarkTextField nameInputField = new DarkTextField();
        DarkTextField ageInputField = new DarkTextField();
        DarkTextField weightInputField = new DarkTextField();
        DarkTextField heightInputField = new DarkTextField();

        //for appearance: black textField, foreground (text) white, caret (the blinking cursor) white, border white
        for (JTextField inputField : new JTextField[]{ nameInputField, ageInputField, weightInputField, heightInputField }) {
            inputField.setBackground(Color.BLACK);
            inputField.setForeground(Color.WHITE);
            inputField.setCaretColor(Color.WHITE);
            inputField.setOpaque(true);
            inputField.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            inputField.setFont(inputFont);
        }

        // dropdown
        DarkComboBox sexDropdown   = new DarkComboBox(new String[]{ "Male", "Female" });
        DarkComboBox activityDropdown = new DarkComboBox(new String[]{
            "Sedentary | No training",
            "Lightly Active | 1-2x / week",
            "Moderately Active | 3-5x / week",
            "Very Active | 6-7x / week",
            "Extra Active | Athlete / 2x daily"
        });
        DarkComboBox goalDropdown = new DarkComboBox(new String[]{
            "Lose Weight", "Maintain Weight", "Gain Weight"
        });

        //same as this one
        
        //2d array (first - label, second - textField/input component)
        Object[][] formRows = {
            { "Name",           nameInputField   },
            { "Age",            ageInputField    },
            { "Weight (kg)",    weightInputField },
            { "Height (cm)",    heightInputField },
            { "Sex",         sexDropdown         },
            { "Activity Level", activityDropdown },
            { "Your Goal",      goalDropdown     },
        };

        for (int rowIndex = 0; rowIndex < formRows.length; rowIndex++) {
            layoutConstraints.gridx = 0; 
            layoutConstraints.gridy = rowIndex; //places new component below the previous one
            layoutConstraints.weightx = 0.4; //takes 40% of the horizontal space
            JLabel rowLabel = new JLabel((String) formRows[rowIndex][0]);
            rowLabel.setFont(new Font("Satoshi", Font.PLAIN, 18));
            rowLabel.setForeground(Color.WHITE);
            formCard.add(rowLabel, layoutConstraints);

            layoutConstraints.gridx = 1; 
            layoutConstraints.weightx = 0.6;
            Component inputComponent = (Component) formRows[rowIndex][1]; //gets input
            inputComponent.setPreferredSize(new Dimension(240, 40)); //input sizing
            formCard.add(inputComponent, layoutConstraints);
        }

        // get started button
        layoutConstraints.gridx     = 0; //sets horizontal position to the left column
        layoutConstraints.gridy     = formRows.length; //so that the button is beneath the form
        layoutConstraints.gridwidth = 2; //to stretch/merge both columns
        layoutConstraints.insets    = new Insets(24, 8, 4, 8); //horizontal padding
        layoutConstraints.ipady     = 10; //add 10 pixels of height

        DarkButton getStartedButton = new DarkButton("Get Started ", Color.WHITE);
        getStartedButton.setForeground(Color.BLACK);
        getStartedButton.setFont(new Font("Satoshi", Font.BOLD, 22));
        formCard.add(getStartedButton, layoutConstraints);  

        //Validator
        getStartedButton.addActionListener(e -> {
            String validationError = Validator.validateProfileForm(
                nameInputField.getText(),
                ageInputField.getText(),
                weightInputField.getText(),
                heightInputField.getText()
            );
            if (validationError != null) {
                JOptionPane.showMessageDialog(parentFrame, validationError, "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //.trim its like getchar() in c "remove spaces"
            // get inputs here (some to be parsed), then create UserProfile and save to disk, then open MainFrame
            String userName = nameInputField.getText().trim();
            int    ageYears = Integer.parseInt(ageInputField.getText().trim());
            float  weightKg = Float.parseFloat(weightInputField.getText().trim());
            float  heightCm = Float.parseFloat(heightInputField.getText().trim());
            String selectedSex = (String) sexDropdown.getSelectedItem();
            String selectedActivityLevel = (String) activityDropdown.getSelectedItem();
            String selectedGoal= (String) goalDropdown.getSelectedItem();

            
            UserProfile newProfile = new UserProfile(
                userName, ageYears, heightCm, weightKg,
                selectedGoal, selectedSex, selectedActivityLevel);

            UserDataBase.save(newProfile);
            parentFrame.dispose();
            new MainFrame(newProfile, false);
        });

        return formCard;
    }
}
