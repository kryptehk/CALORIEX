package logics.model;

import java.io.*;

/**
 * Represents a single food item with its full nutritional breakdown.
 *
 * All macro values are stored as the total for the logged portion
 * java prevents objects from written to files for stability reason. 
 * by implemeting serializable, i am giving JVM the permission to "scan" and
 * turn its private data into bytes
 */
public class Food implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String foodName;
    private final int    caloriesKcal;
    private final double proteinGrams;
    private final double carbsGrams;
    private final double fatGrams;
    private final double fibreGrams;
    private final double sodiumMilligrams;
    private final double sugarGrams;

  
    // Full constructor with all macros and micronutrients.
    public Food(String foodName, int caloriesKcal,
                double proteinGrams, double carbsGrams, double fatGrams,
                double fibreGrams, double sodiumMilligrams, double sugarGrams) {
        this.foodName = foodName;
        this.caloriesKcal = caloriesKcal;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatGrams = fatGrams;
        this.fibreGrams = fibreGrams;
        this.sodiumMilligrams = sodiumMilligrams;
        this.sugarGrams = sugarGrams;
    }

    // Getters 

    public String getName()    { return foodName; }
    public int    getCalories(){ return caloriesKcal; }
    public double getProtein() { return proteinGrams; }
    public double getCarbs()   { return carbsGrams; }
    public double getFat()     { return fatGrams; }
    public double getFibre()   { return fibreGrams; }
    public double getSodium()  { return sodiumMilligrams; }
    public double getSugar()   { return sugarGrams; }

   
    @Override
    public String toString() {
        return foodName + " (" + caloriesKcal + " kcal)";
    }
}
