package logics.validator;

/**
Input validation helpers used by profile and food entry forms.
Each method returns an error message string if the input is invalid,
or null if the input passes validation — making it easy to chain checks.
 */
public class Validator {

    // Returns an error message if the name is blank, or null if valid.
    public static String validateName(String nameInput) {
        if (nameInput == null || nameInput.trim().isEmpty()) return "Name cannot be empty.";
        return null;
    }

    // Returns an error message if the age is outside a reasonable range, or null if valid. 
    public static String validateAge(String ageInputText) {
        try {
            int parsedAge = Integer.parseInt(ageInputText.trim());
            if (parsedAge < 1 || parsedAge > 120) return "Age must be between 1 and 120.";
        } catch (NumberFormatException notANumber) {
            return "Age must be a whole number.";
        }
        return null;
    }

    // Returns an error message if the weight (kg) is unreasonable, or null if valid.
    public static String validateWeight(String weightInputText) {
        try {
            float parsedWeight = Float.parseFloat(weightInputText.trim());
            if (parsedWeight < 0 || parsedWeight > 500) return "Weight must be between 0 and 500 kg.";
        } catch (NumberFormatException notANumber) {
            return "Weight must be a number (e.g. 70.5).";
        }
        return null;
    }

    // Returns an error message if the height (cm) is unreasonable, or null if valid.
    public static String validateHeight(String heightInputText) {
        try {
            float parsedHeight = Float.parseFloat(heightInputText.trim());
            if (parsedHeight < 50 || parsedHeight > 300) 
                return "Height must be between 50 and 300 cm.";
        } catch (NumberFormatException notANumber) {
            return "Height must be a number (e.g. 170.0).";
        }
        return null;
    }

    // Returns an error message if the calorie value is negative, or null if valid.
    public static String validateCalories(String caloriesInputText) {
        try {
            int parsedCalories = Integer.parseInt(caloriesInputText.trim());
            if (parsedCalories < 0) 
                return "Calories cannot be negative.";
        } catch (NumberFormatException notANumber) {
            return "Calories must be a whole number.";
        }
        return null;
    }

    // Returns an error message if a macro gram value is negative, or null if valid.
    public static String validateMacroGrams(String gramsInputText, String macroName) {
        try {
            double parsedGrams = Double.parseDouble(gramsInputText.trim());
            if (parsedGrams < 0) return macroName + " cannot be negative.";
        } catch (NumberFormatException notANumber) {
            return macroName + " must be a number (e.g. 12.5).";
        }
        return null;
    }

    /**
     Validates all four fields on the initial profile setup form.
      Returns the first error message found, or null if everything passes.
     */
    public static String validateProfileForm(String nameInput, String ageInputText, String weightInputText, String heightInputText) {
        String nameError   = validateName(nameInput);       
        if (nameError   != null) 
            return nameError;
        String ageError    = validateAge(ageInputText);     
        if (ageError    != null) 
            return ageError;
        String weightError = validateWeight(weightInputText); 
        if (weightError != null) 
            return weightError;
        String heightError = validateHeight(heightInputText); 
        if (heightError != null) 
            return heightError;
        return null;
    }
}
