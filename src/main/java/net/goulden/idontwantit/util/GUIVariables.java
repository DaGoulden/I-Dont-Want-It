package net.goulden.idontwantit.util;

import net.goulden.idontwantit.config.ConfigBuilder;

public final class GUIVariables {

    private GUIVariables() {}

    // Colors
    public static int primaryColor;
    public static int secondaryColor;
    public static int tertiaryColor;
    public static int hoveredAdditiveColor;
    public static int linesColor;
    public static int goodMeaningColor;
    public static int badMeaningColor;

    // Pixels
    public static int spacedX;
    public static int spacedY;
    public static int spacedText;
    public static int spaceBetweenButtons;
    public static int fontHeight;
    public static int customHeight;
    public static int firstLineEndInY;
    public static int secondLineStartInY;
    public static int scrollBarWidth = 6; // Do not change
    public static int itemSize = 16; // Do not change

    // Durations
    public static int stateButtonDuration;
    public static int deleteConfirmationDuration;
    public static int editBoxErrorDuration;

    public static void recalculate(int screenWidth, int screenHeight, int fontLineHeight) {
        fontHeight = fontLineHeight;
        customHeight = fontHeight + spacedText * 2;
        firstLineEndInY = spacedY + customHeight;
        secondLineStartInY = screenHeight - spacedY - customHeight;
    }

    public static void recalculate (int screenWidth, int screenHeight) {
        customHeight = fontHeight + spacedText * 2;
        firstLineEndInY = spacedY + customHeight;
        secondLineStartInY = screenHeight - spacedY - customHeight;
    }
}
