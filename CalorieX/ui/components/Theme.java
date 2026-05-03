package ui.components;

import java.awt.*;

// Theme — colour palette and shared font helpers

public class Theme {
    public static final Color BACKGROUND_DARK_CARD  = new Color(20, 20, 20);
    public static final Color BACKGROUND_MEAL_CARD  = new Color(25, 25, 25);
    public static final Color BACKGROUND_MACRO_TILE = new Color(30, 30, 30);

    public static Font font(int fontStyle, int fontSize) {
        return new Font("SansSerif", fontStyle, fontSize);
    }
    public static Font bold(int fontSize)  { return font(Font.BOLD,  fontSize); }
    public static Font plain(int fontSize) { return font(Font.PLAIN, fontSize); }
}
