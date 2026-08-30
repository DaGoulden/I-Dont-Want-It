package net.goulden.idontwantit.client.gui.widgets.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.goulden.idontwantit.util.GUIVariables.*;

public class CustomEditBox extends EditBox {

    protected int backgroundColor;

    public CustomEditBox(int maxLength, String defaultText) {
        super(font, -100, -100, 100, 100, Component.empty());
        this.setBordered(false);
        this.setMaxLength(maxLength);
        this.setValue(defaultText);
    }

    public CustomEditBox(int maxLength, String defaultText, int x, int y, int w, int h, int color) {
        super(font, x, y, w, h, Component.empty());
        this.backgroundColor = color;
        this.setBordered(false);
        this.setMaxLength(maxLength);
        this.setValue(defaultText);
    }

    public CustomEditBox(int maxLength, Consumer<String> text) {
        super(font, -100, -100, 100, 100, Component.empty());
        this.setBordered(false);
        this.setMaxLength(maxLength);
        this.setResponder(text);
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        g.fill(getX(),
                getY(),
                getX() + width,
                getY() + height,
                backgroundColor
        );

        if (isHovered()) {
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

        this.setTextColor(linesColor);

        g.pose().pushPose();
        g.pose().translate(spacedText, spacedText, 0);
        super.renderWidget(g, mouseX, mouseY, partialTick);
        g.pose().popPose();

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX - spacedText, mouseY, button);
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}