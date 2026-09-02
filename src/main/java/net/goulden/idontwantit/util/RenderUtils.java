package net.goulden.idontwantit.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import static net.goulden.idontwantit.util.GUIVariables.*;

public final class RenderUtils {

    private RenderUtils() {}

    public static int lerpColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int)(a1 + (a2 - a1) * t);
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float) Math.pow(-2.0F * t + 2.0F, 3) / 2.0F;
    }

    public static void renderCustomOutline(GuiGraphics g, int x, int y, int width, int height, boolean renderUpLine, int color) {
        // UP
        if (renderUpLine) g.fill(x + spaceBetweenButtons, y, x + width - spaceBetweenButtons, y + spaceBetweenButtons, color);
        // LEFT
        g.fill(x, y, x + spaceBetweenButtons, y + height, color);
        // RIGHT
        g.fill(x + width - spaceBetweenButtons, y, x + width, y + height, color);
        // BOTTOM
        g.fill(x + spaceBetweenButtons, y + height - spaceBetweenButtons, x + width - spaceBetweenButtons, y + height, color);
    }

    public static void drawCutString(GuiGraphics g, int mouseX, int mouseY, String text, int maxWidth, int x, int y, int color) {
        String displayName;
        if (font.width(text) > maxWidth && !(mouseX >= x && mouseX <= x + maxWidth && mouseY >= y && mouseY <= y + fontHeight)) {
            displayName = font.plainSubstrByWidth(text, maxWidth - font.width("...")) + "...";
        } else {
            displayName = text;
        }
        g.drawString(
                font,
                displayName,
                x,
                y,
                color
        );
    }
}
