package logics.model;

import java.io.*;

/**
 * Represents a single food item with its full nutritional breakdown.
 *
 * All macro values are stored as the total for the logged portion
 * (i.e., already multiplied by quantity when entered via ManualEntryDialog).
 */
public class Food implements Serializable {
    private static final long serialVersionUID = 1L;

    private String foodName;
    private int    caloriesKcal;
    private double proteinGrams;
    private double carbsGrams;
    private double fatGrams;
    private double fibreGrams;
    private double sodiumMilligrams;
    private double sugarGrams;

    /* Constructors 
    OVERLOADING
     Basic constructor — fibre, sodium, and sugar default to 0.*/
    public Food(String foodName, int caloriesKcal,
                double proteinGrams, double carbsGrams, double fatGrams) {
        this(foodName, caloriesKcal, proteinGrams, carbsGrams, fatGrams, 0, 0, 0);
    }

    // Full constructor with all macros and micronutrients.
    public Food(String foodName, int caloriesKcal,
                double proteinGrams, double carbsGrams, double fatGrams,
                double fibreGrams, double sodiumMilligrams, double sugarGrams) {
        this.foodName         = foodName;
        this.caloriesKcal     = caloriesKcal;
        this.proteinGrams     = proteinGrams;
        this.carbsGrams       = carbsGrams;
        this.fatGrams         = fatGrams;
        this.fibreGrams       = fibreGrams;
        this.sodiumMilligrams = sodiumMilligrams;
        this.sugarGrams       = sugarGrams;
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

    // Setters
    public void setName(String foodName)           { this.foodName = foodName; }
    public void setCalories(int caloriesKcal)      { this.caloriesKcal = caloriesKcal; }
    public void setProtein(double proteinGrams)    { this.proteinGrams = proteinGrams; }
    public void setCarbs(double carbsGrams)        { this.carbsGrams = carbsGrams; }
    public void setFat(double fatGrams)            { this.fatGrams = fatGrams; }
    public void setFibre(double fibreGrams)        { this.fibreGrams = fibreGrams; }
    public void setSodium(double sodiumMilligrams) { this.sodiumMilligrams = sodiumMilligrams; }
    public void setSugar(double sugarGrams)        { this.sugarGrams = sugarGrams; }

    @Override
    public String toString() {
        return foodName + " (" + caloriesKcal + " kcal)";
    }
}
