package net.goulden.idontwantit.util;

import net.minecraft.client.gui.Font;

public final class GUIVariables {

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
    public static int fontHeight; // Automatic
    public static int customHeight; // Automatic
    public static int firstLineEndInY; // Automatic
    public static int secondLineStartInY; // Automatic
    public static int cornerButtonsSize; // Automatic
    public static int scrollbarWidth = 6; // Do not change
    public static int iconSize = 16; // Do not change

    // Animations
    public static int stateButtonDuration;
    public static int deleteConfirmationDuration;
    public static int editBoxConfirmationDuration;
    public static int scrollbarAnimationDuration;
    public static int openListAnimationDuration;

    // Font
    public static Font font; // Automatic

    public static void recalculate(int screenHeight, Font usedFont) {
        font = usedFont;
        fontHeight = usedFont.lineHeight;
        customHeight = fontHeight + spacedText * 2;
        firstLineEndInY = spacedY + customHeight;
        secondLineStartInY = screenHeight - spacedY - customHeight;
        cornerButtonsSize = customHeight * 2;
    }

}
