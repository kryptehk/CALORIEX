package ui.screens;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import logics.model.UserProfile;
import logics.user_info.UserDataBase;
import logics.validator.Validator;
import ui.components.*;
import ui.LoginFrame;

/**
  Settings screen — lets the user edit profile fields and reset the app.
 */
public class SettingsPanel {

    private final UserProfile    userProfile;
    private final DashboardPanel dashboardPanel;

    public SettingsPanel(UserProfile userProfile, DashboardPanel dashboardPanel) {
        this.userProfile    = userProfile;
        this.dashboardPanel = dashboardPanel;
    }

    public JPanel build(JFrame appFrame) {
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
