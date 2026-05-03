package logics.model;

import java.io.*;
import java.util.*;

/**
 * A named meal (e.g. "Breakfast") that holds a list of Food items logged by the user.
 */
public class Meal implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String     mealName;
    private final List<Food> loggedFoods;

    public Meal(String mealName) {
        this.mealName    = mealName;
        this.loggedFoods = new ArrayList<>();
    }

    public String     getMealName()  { return mealName; }
    public List<Food> getFoodItems() { return loggedFoods; }

    @Override
    public String toString() {
        return mealName + " (" + loggedFoods.size() + " items)";
    }
}
