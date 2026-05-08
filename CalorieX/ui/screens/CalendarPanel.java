package ui.screens;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;
import logics.controller.FoodController;
import ui.components.*;

/**
  Calendar screen — month grid view of the food log.
  Clicking a date fires onDateSelected with the ISO date string.
 */
public class CalendarPanel {

    private YearMonth displayedMonth = YearMonth.now();
    private JPanel    calendarGridContainer;

    /* when user clicks a date, send me the selected date as a String 
            - user clicks the date
            - calendar reports it (04/01/2023 for example)
            - MainFrame recieves it (selectedDate) */
    public JPanel build(Consumer<String> onDateSelected) {
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
        calendarGridContainer.add(buildMonthGrid(displayedMonth, onDateSelected), BorderLayout.CENTER);
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
            boolean isToday       = dayDateString.equals(java.time.LocalDate.now().toString());

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
            displayedMonth = displayedMonth.minusMonths(1);
            refreshCalendarGrid(onDateSelected);
        });
        nextMonthButton.addActionListener(e -> { // rebuilds calendar when you click ◄ or ►
            displayedMonth = displayedMonth.plusMonths(1);
            refreshCalendarGrid(onDateSelected);
        });

        return outerPanel;
    }

    /** It opens a popup where you choose a month and year, then updates your calendar. 
     * Open popup -> choose month/year -> click "Go" -> displayedMonth is updated -> 
     * calendar grid is rebuilt with the new month/year and shown on the calendar screen.
    */
    private void showMonthYearPickerDialog(Component anchorComponent, Consumer<String> onDateSelected) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(anchorComponent);
        JDialog pickerDialog = new JDialog(parentFrame, "Go to Month / Year", true);
        pickerDialog.setSize(360, 240);
        pickerDialog.setLocationRelativeTo(parentFrame);

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
        monthDropdown.setSelectedIndex(displayedMonth.getMonthValue() - 1);
        monthDropdown.setPreferredSize(new Dimension(180, 34));

        int currentYear = displayedMonth.getYear();
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
            displayedMonth = YearMonth.of(
                (Integer) yearSpinner.getValue(),
                monthDropdown.getSelectedIndex() + 1);
            pickerDialog.dispose();
            refreshCalendarGrid(onDateSelected);
        });

        pickerDialog.setContentPane(dialogContent);
        pickerDialog.setVisible(true);
    }

    // It converts a word into "Capitalized form" (first letter uppercase, rest lowercase).
    private String capitalizeFirstLetter(String text) {
        return text.isEmpty() ? text : text.charAt(0) + text.substring(1).toLowerCase();
    }
}
