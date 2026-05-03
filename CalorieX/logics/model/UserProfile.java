package logics.model;

import java.io.*;

/**
 * Stores the user's physical stats and fitness preferences,
 * and derives daily nutrition targets from them.
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userName;
    private int ageYears;
    private float heightCentimeters;
    private float weightKilograms;
    private String fitnessGoal;       // "Lose Weight" | "Maintain Weight" | "Gain Weight"
    private String biologicalGender;  // "Male" | "Female"
    private String activityLevel;     // "Sedentary" | "Lightly Active" | ... | "Extra Active"

    public UserProfile(String userName, int ageYears,
                       float heightCentimeters, float weightKilograms,
                       String fitnessGoal, String biologicalGender, String activityLevel) {
        this.userName = userName;
        this.ageYears = ageYears;
        this.heightCentimeters = heightCentimeters;
        this.weightKilograms = weightKilograms;
        this.fitnessGoal = fitnessGoal;
        this.biologicalGender = biologicalGender;
        this.activityLevel = activityLevel;
    }

    // Getters

    public String getName(){return userName; }
    public int getAge()    {return ageYears;}
    public float  getHeight(){
         return heightCentimeters; }
    public float  getWeight(){
         return weightKilograms; 
        }
    public String getGoal(){
         return fitnessGoal; 
        }
    public String getGender(){
         return biologicalGender; 
        }
    public String getActivityLevel() {
         return activityLevel;
         }

    //Setters (for profile edits in SettingsPanel) 

    public void setAge(int ageYears){
         this.ageYears = ageYears; }
    public void setHeight(float heightCentimeters){
         this.heightCentimeters = heightCentimeters;}
    public void setWeight(float weightKilograms){
         this.weightKilograms = weightKilograms; }
    public void setGoal(String fitnessGoal){
         this.fitnessGoal = fitnessGoal;}
    public void setGender(String biologicalGender){
         this.biologicalGender = biologicalGender; }
    public void setActivityLevel(String activityLevel){
         this.activityLevel = activityLevel; }

    //Calculations 
    /* Basal Metabolic Rate using the Mifflin-St Jeor formula. 
       this is used  */
    public double calculateBMR() {
        double baseBMR = (10.0 * weightKilograms) + (6.25 * heightCentimeters) - (5.0 * ageYears);
        return "Male".equals(biologicalGender) ? baseBMR + 5 : baseBMR - 161;       //adjust by gender
    }

    /* Total Daily Energy Expenditure = BMR multiplied by the activity level factor. */
    public double calculateTDEE() {
        double bmr = calculateBMR();
        //switch expression used in 
        return switch (activityLevel) {
        case "Lightly Active | 1-2x / week"      -> bmr * 1.375;
        case "Moderately Active | 3-5x / week"   -> bmr * 1.55;
        case "Very Active | 6-7x / week"         -> bmr * 1.725;
        case "Extra Active | Athlete / 2x daily" -> bmr * 1.9;
        default                                  -> bmr * 1.2;
        };
    }

    /* Daily calorie target adjusted up or down based on the user's fitness goal. */
    public double getDailyCalorieTarget() {
        double tdee = calculateTDEE();
        return switch (fitnessGoal) {
            case "Lose Weight" -> tdee - 500;
            case "Gain Weight" -> tdee + 300;
            default -> tdee;
        };
    }

    /* Daily protein target in grams, scaled by activity level and age. */
    public double getDailyProteinTargetGrams() {
        boolean isActiveUser = !activityLevel.equals("Sedentary | No training")
                            && !activityLevel.equals("Lightly Active | 1-2x / week");

        double gramsPerKilogram;
        if (null == fitnessGoal)      gramsPerKilogram = isActiveUser ? 1.6 : 1.2;
        else gramsPerKilogram = switch (fitnessGoal) {
            case "Lose Weight" -> isActiveUser ? 2.2 : 1.8;
            case "Gain Weight" -> isActiveUser ? 2.0 : 1.6;
            default -> isActiveUser ? 1.6 : 1.2;
        };

        double baseProteinGrams = gramsPerKilogram * weightKilograms;

        // Older adults need ~10% more protein to preserve muscle mass
        return ageYears > 50 ? baseProteinGrams * 1.10 : baseProteinGrams;
    }

    /** Daily fat target in grams, derived from a goal-specific fraction of total calories. */
    public double getDailyFatTargetGrams() {
        double fatCalorieFraction = "Lose Weight".equals(fitnessGoal) ? 0.25
                                  : "Gain Weight".equals(fitnessGoal) ? 0.30
                                  : 0.28;
        return (getDailyCalorieTarget() * fatCalorieFraction) / 9.0;
    }

    /** Daily carbohydrate target in grams (minimum 50g floor). */
    public double getDailyCarbohydrateTargetGrams() {
        double carbCalories = getDailyCalorieTarget()
                            - getDailyProteinTargetGrams() * 4.0
                            - getDailyFatTargetGrams()     * 9.0;
        return Math.max(carbCalories / 4.0, 50);
    }

    /** Daily fibre target in grams, based on calories and age/gender minimums. */
    public double getDailyFibreTargetGrams() {
        double calculatedFibreGrams = (getDailyCalorieTarget() / 1000.0) * 14.0;
        double minimumFibreGrams    = "Male".equals(biologicalGender)
                                    ? (ageYears > 50 ? 30 : 38)
                                    : (ageYears > 50 ? 21 : 25);
        return Math.max(calculatedFibreGrams, minimumFibreGrams);
    }

    /** Daily sodium target in milligrams — lower for users over 50. */
    public double getDailySodiumTargetMilligrams() {
        return ageYears > 50 ? 1500 : 2300;
    }

    /** Daily added-sugar target in grams (10% of total calorie target). */
    public double getDailySugarTargetGrams() {
        return (getDailyCalorieTarget() * 0.10) / 4.0;
    }
}

