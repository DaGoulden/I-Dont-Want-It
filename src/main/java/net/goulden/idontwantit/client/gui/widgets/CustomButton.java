package net.goulden.idontwantit.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.util.GUIVariables.*;

public class CustomButton extends Button {

    protected int usedColor;
    protected String text;
    protected boolean isTextCentered;
    protected int usedTextColor = linesColor;
    protected boolean hoverable = true;

    public CustomButton(OnPress onPress, String text, boolean isTextCentered) {
        super(0, 0, 0, 0, Component.empty(), onPress, DEFAULT_NARRATION);
        this.text = text;
        this.isTextCentered = isTextCentered;
    }

    public CustomButton(OnPress onPress) {
        super(0, 0, 0, 0, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        g.fill(getX(),
                getY(),
                getX() + width,
                getY() + height,
                usedColor
        );

        if (isHovered() && hoverable) {
            g.fill(getX(),
                    getY(),
                    getX() + width,
                    getY() + height,
                    hoveredAdditiveColor
            );
        }

        if (isFocused()) {
            g.renderOutline(
                    getX(),
                    getY(),
                    width,
                    height,
                    linesColor
            );
        }

        if (text != null) {
            if (isTextCentered) {
                g.drawCenteredString(
                        Minecraft.getInstance().font,
                        text,
                        getX() + width / 2,
                        getY() + spacedText,
                        usedTextColor
                );
            } else {
                g.drawString(
                        Minecraft.getInstance().font,
                        text,
                        getX() + spacedText,
                        getY() + spacedText,
                        usedTextColor
                );
            }
        }
    }

    public void setUsedColor(int usedColor) {
        this.usedColor = usedColor;
    }

    public String getText() {
        return text;
    }

    public void setTextUsedColor(int usedTextColor) {
        this.usedTextColor = usedTextColor;
    }

    public void setHoverable(boolean hoverable) {
        this.hoverable = hoverable;
    }
}