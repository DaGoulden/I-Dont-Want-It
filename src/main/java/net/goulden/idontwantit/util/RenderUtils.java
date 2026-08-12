package net.goulden.idontwantit.util;

import net.minecraft.client.gui.GuiGraphics;

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
        if (t < 0.5f) {
            return 4f * t * t * t;
        } else {
            float f = 2f * t - 2f;
            return 1f + 0.5f * f * f * f;
        }
    }

    public static void renderCustomOutline(GuiGraphics g, int x, int y, int width, int height, int color) {
        g.fill(x, y, x + width, y + spaceBetweenButtons, color);
        g.fill(x, y + height - spaceBetweenButtons, x + width, y + height, color);
        g.fill(x, y + spaceBetweenButtons, x + spaceBetweenButtons, y + height - spaceBetweenButtons, color);
        g.fill(x + width - spaceBetweenButtons, y + spaceBetweenButtons, x + width, y + height - spaceBetweenButtons, color);
    }
}
