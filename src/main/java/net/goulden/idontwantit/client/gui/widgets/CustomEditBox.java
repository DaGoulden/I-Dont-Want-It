package net.goulden.idontwantit.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.util.ScreenVariables.*;

public class CustomEditBox extends EditBox {

    Font font;

    public CustomEditBox(Font font, int x, int y, int width, int height, Component component, int maxLength, String defaultText) {
        super(font, x, y, width, height, component);
        this.setMaxLength(maxLength);
        this.setValue(defaultText);
        this.setBordered(false);
        //this.setHeight(height);
        this.font = font;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        g.fill(getX(), getY(), getX() + width, getY() + height, 0x77FF0000);

        g.pose().pushPose();
        g.pose().translate(spacedText, spacedText, 0);
        super.renderWidget(g, mouseX, mouseY, partialTick);
        g.pose().popPose();

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX - spacedText, mouseY, button);
    }

}