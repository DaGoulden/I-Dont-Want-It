package net.goulden.idontwantit.client.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.util.GUIVariables.hoveredAdditiveColor;
import static net.goulden.idontwantit.util.GUIVariables.recalculate;

public class CustomButton extends Button {

    public int usedColor;
    public boolean hoverable = true;

    public CustomButton(int x, int y, int w, int h, OnPress onPress, int usedColor) {
        super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
        this.usedColor = usedColor;
    }

    public CustomButton(OnPress onPress, int usedColor) {
        super(0, 0, 0, 0, Component.empty(), onPress, DEFAULT_NARRATION);
        this.usedColor = usedColor;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        recalculate(width, height);

        g.fill(getX(), getY(), getX() + width, getY() + height, usedColor);

        if (isHovered() && hoverable) {
            g.fill(getX(), getY(), getX() + width, getY() + height, hoveredAdditiveColor);
        }

//        if (isFocused()) {
//            g.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
//        }
    }
}