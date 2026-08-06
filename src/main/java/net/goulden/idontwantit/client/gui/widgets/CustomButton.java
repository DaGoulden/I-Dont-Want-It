package net.goulden.idontwantit.client.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CustomButton extends Button {

    private final int normalColor;
    private final int hoverColor;

    public CustomButton(int x, int y, int w, int h, OnPress onPress, int normalColor, int hoverColor) {
        super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
    }

    public CustomButton(OnPress onPress, int normalColor, int hoverColor) {
        super(0, 0, 0, 0, Component.empty(), onPress, DEFAULT_NARRATION);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        g.fill(getX(), getY(), getX() + width, getY() + height, normalColor);

        if (isHovered()) {
            g.fill(getX(), getY(), getX() + width, getY() + height, hoverColor);
        }

        /*if (isFocused()) {
            g.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
        }*/
    }
}