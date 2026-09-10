package net.goulden.idontwantit.client.gui.widgets.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.util.GUIVariables.*;

public class CustomButton extends Button {

    private final int backgroundColor;
    private String text;
    private boolean isTextCentered;
    private boolean lowOpacity = false;
    private boolean hoverable = true;

    public CustomButton(OnPress onPress, int backgroundColor) {
        super(0, 0, 0, 0, Component.empty(), onPress, DEFAULT_NARRATION);
        this.backgroundColor = backgroundColor;
    }

    public CustomButton(OnPress onPress, String text, boolean isTextCentered, int backgroundColor) {
        super(0, 0, 0, 0, Component.empty(), onPress, DEFAULT_NARRATION);
        this.backgroundColor = backgroundColor;
        this.text = text;
        this.isTextCentered = isTextCentered;
    }

    public CustomButton(OnPress onPress, int x, int y, int w, int h, int backgroundColor) {
        super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
        this.backgroundColor = backgroundColor;
    }

    public CustomButton(OnPress onPress, String text, boolean isTextCentered, int x, int y, int w, int h, int backgroundColor) {
        super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
        this.backgroundColor = backgroundColor;
        this.text = text;
        this.isTextCentered = isTextCentered;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        g.fill(getX(),
                getY(),
                getX() + width,
                getY() + height,
                switch (backgroundColor) {
                    case 1 -> lowOpacity ? (primaryColor & 0x00FFFFFF) | (((primaryColor >>> 24) / 2) << 24)  : primaryColor;
                    case 2 -> lowOpacity ? (secondaryColor & 0x00FFFFFF) | (((secondaryColor >>> 24) / 2) << 24) : secondaryColor;
                    case 3 -> lowOpacity ? (tertiaryColor & 0x00FFFFFF) | (((tertiaryColor >>> 24) / 2) << 24) : tertiaryColor;
                    default -> 0;
                }
        );

        if (isHovered() && hoverable) {
            g.fill(getX(),
                    getY(),
                    getX() + width,
                    getY() + height,
                    hoveredAdditiveColor
            );
        }

        if (text != null) {
            if (isTextCentered) {
                g.drawCenteredString(
                        Minecraft.getInstance().font,
                        text,
                        getX() + width / 2,
                        getY() + spacedText,
                        lowOpacity ? (linesColor & 0x00FFFFFF) | 0x99000000 : linesColor
                );
            } else {
                g.drawString(
                        Minecraft.getInstance().font,
                        text,
                        getX() + spacedText,
                        getY() + spacedText,
                        lowOpacity ? (linesColor & 0x00FFFFFF) | 0x99000000 : linesColor
                );
            }
        }
    }

    public void setLowOpacity(boolean lowOpacity) {
        this.lowOpacity = lowOpacity;
    }

    public void setHoverable(boolean hoverable) {
        this.hoverable = hoverable;
    }
}