package logics.controller;

import java.io.*;
import java.util.*;
import logics.model.Food;

/**
 The central hub for all food-related data operations, including the reusable food library and the daily meal log.

 */
public class FoodController {

    private static final String FOOD_LOG_FILE_PATH = "data/foodlog.dat";
    /* A reusable library of food items the user can add to meals without retyping details each time. */
    private static List<Food> foodLibrary = new ArrayList<>(); 

    /*Outer map = Date "2025-06-01" 
      Inner map = Inner map = Meal "Breakfast"
      The list = List of Food items for the Breakfast meal on "2025-06-01" */
    private static Map<String,Map <String,List<Food> > >  dailyMealLog = new HashMap<>(); 


    static {loadFromDisk();} //This is a static initializer block. so that data from foodlog.dat is loaded as soon as this class is accessed.

    /**
      Adds a new food to the reusable library and persists the updated library to disk.
      @param foodToAdd the food item to add
      add food object to food library then save to disk
     */
    public static void addFoodToLibrary(Food foodToAdd) {
        foodLibrary.add(foodToAdd);
        saveToDisk();
    }

    /**
      Returns an unmodifiable view of the full food library.
      @return the list of all food items
     */
    public static List<Food> getFoodLibrary() {
        return Collections.unmodifiableList(foodLibrary);
    }

    // Removes the food at the given library index and persists.
    public static void removeFoodFromLibrary(int libraryIndex) {
        if (libraryIndex >= 0 && libraryIndex < foodLibrary.size()) {
            foodLibrary.remove(libraryIndex); //goes to ram
            saveToDisk(); 
        }
    }

    // Daily Log 
    /**
     
      @param dateString  ISO date string, e.g. "2025-06-01"
      @param mealName    meal slot, e.g. "Breakfast"
     @param foodToLog   the food item to record
     computeIfAbasent = shorcut for "Check if this key exists; if it does, return the value.
     "If this key is missing, run this function to create it; then, return the value (existing or new) so I can use it."

     */
    public static void logFoodForMeal(String dateString, String mealName, Food foodToLog) {
        dailyMealLog
            .computeIfAbsent(dateString, date -> new HashMap<>())
            .computeIfAbsent(mealName,   meal -> new ArrayList<>())
            .add(foodToLog);
        saveToDisk();
    }

    /**
      Removes a specific food entry from the daily log by its position in the meal list.
     
      @param dateString  ISO date string
      @param mealName    meal slot name
      @param foodIndex   zero-based position of the food to remove
     */
    public static void removeFoodFromLog(String dateString, String mealName, int foodIndex) {
        Map<String, List<Food>> mealsOnDate = dailyMealLog.get(dateString);
        if (mealsOnDate == null) return;

        List<Food> foodsInMeal = mealsOnDate.get(mealName);
        if (foodsInMeal == null || foodIndex < 0 || foodIndex >= foodsInMeal.size()) return;

        foodsInMeal.remove(foodIndex);

        // Clean up empty containers so the data stays tidy
        if (foodsInMeal.isEmpty())  mealsOnDate.remove(mealName);
        if (mealsOnDate.isEmpty())  dailyMealLog.remove(dateString);

        saveToDisk();
    }

    /**
      Replaces a logged food entry with an updated version.
     
      @param dateString    ISO date string
      @param mealName      meal slot name
      @param foodIndex     zero-based position to replace
      @param updatedFood   the new food to put in that position
     */
    public static void updateFoodInLog(String dateString, String mealName,
                                       int foodIndex, Food updatedFood) {
        Map<String, List<Food>> mealsOnDate = dailyMealLog.get(dateString);
        if (mealsOnDate == null) return;

        List<Food> foodsInMeal = mealsOnDate.get(mealName);
        if (foodsInMeal == null || foodIndex < 0 || foodIndex >= foodsInMeal.size()) return;

        foodsInMeal.set(foodIndex, updatedFood);
        saveToDisk();
    }

    /**
     * Returns all meals and their food lists logged on a specific date.
     * Returns an empty map if nothing has been logged for that date yet.
     */
    public static Map<String, List<Food>> getMealsForDate(String dateString) {
        return dailyMealLog.getOrDefault(dateString, new HashMap<>());
    }

    // Returns every date that has at least one food logged.
    public static Set<String> getAllLoggedDates() {
        return Collections.unmodifiableSet(dailyMealLog.keySet());
    }

    //  

    // Serialisation wrapper that bundles both the library and the daily log together.
    private static class PersistedData implements Serializable {
        private static final long serialVersionUID = 2L;
        List<Food> library;
        Map<String, Map<String, List<Food>>> dailyLog;
    }

    @SuppressWarnings("unchecked")
    private static void loadFromDisk() {
        File dataFile = new File(FOOD_LOG_FILE_PATH);
        if (!dataFile.exists()) return;

        try (ObjectInputStream inputStream =
                     new ObjectInputStream(new FileInputStream(dataFile))) {

            Object rawData = inputStream.readObject();

            if(rawData instanceof PersistedData savedData) {
                foodLibrary  = savedData.library  != null ? savedData.library  : new ArrayList<>();
                dailyMealLog = savedData.dailyLog != null ? savedData.dailyLog : new HashMap<>();
            } else {
                // Legacy format: file only contained the daily log map (no library)
                dailyMealLog = (Map<String, Map<String, List<Food>>>) rawData;
                foodLibrary  = new ArrayList<>();
            }

        } catch (Exception loadException) {
            foodLibrary  = new ArrayList<>();
            dailyMealLog = new HashMap<>();
        }
    }

    private static void saveToDisk() {
        new File("data").mkdirs();

        PersistedData dataToSave = new PersistedData();
        dataToSave.library  = foodLibrary;
        dataToSave.dailyLog = dailyMealLog;

        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(new FileOutputStream(FOOD_LOG_FILE_PATH))) {
            outputStream.writeObject(dataToSave);
        } catch (IOException saveException) {
        }
    }
}
