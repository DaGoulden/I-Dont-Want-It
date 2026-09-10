package net.goulden.idontwantit.client.gui.widgets.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.easeInOutQuart;

public class CustomEditBox extends EditBox {

    String profileName;
    protected int backgroundColor;
    int animationElapsed = editBoxConfirmationDuration + 1;
    boolean wrongValue;
    boolean successfulSubmit;
    Runnable onSubmit;

    public CustomEditBox(int maxLength, String defaultValue, Runnable onSubmit, int x, int y, int w, int h, int backgroundColor) {
        super(font, x, y, w, h, Component.empty());
        this.backgroundColor = backgroundColor;
        this.profileName = defaultValue;
        this.onSubmit = onSubmit;
        this.setBordered(false);
        this.setMaxLength(maxLength);
        this.setValue(defaultValue);
    }

    public CustomEditBox(int maxLength, String defaultValue, int backgroundColor) {
        super(font, -100, -100, 100, 100, Component.empty());
        this.backgroundColor = backgroundColor;
        this.setBordered(false);
        this.setMaxLength(maxLength);
        this.setValue(defaultValue);
    }

    public CustomEditBox(int maxLength, Consumer<String> text, int backgroundColor) {
        super(font, -100, -100, 100, 100, Component.empty());
        this.backgroundColor = backgroundColor;
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
                switch (backgroundColor) {
                    case 1 -> primaryColor;
                    case 2 -> secondaryColor;
                    case 3 -> tertiaryColor;
                    default -> 0;
                }
        );

        if (isHovered()) {
            g.fill(getX(),
                    getY(),
                    getX() + width,
                    getY() + height,
                    hoveredAdditiveColor
            );
        }

        this.setTextColor(linesColor);

        if (!isFocused() && wrongValue) {
            animationElapsed = 0;
        }

        g.pose().pushPose();
        g.pose().translate(spacedText, spacedText, 0);
        super.renderWidget(g, mouseX, mouseY, partialTick);
        if (animationElapsed <= editBoxConfirmationDuration) {
            float errorProgress = easeInOutQuart((float) animationElapsed / editBoxConfirmationDuration);
            int errorAlpha = Math.max(5, (int) ((1f - errorProgress) * 256));
            int color = successfulSubmit ? goodMeaningColor : badMeaningColor;
            g.drawString(font,
                    getValue().trim(),
                    getX(),
                    getY(),
                    (color & 0x00FFFFFF) | errorAlpha << 24
            );
        }
        g.pose().popPose();
    }

    public void tick() {
        if (animationElapsed <= editBoxConfirmationDuration) {
            animationElapsed++;
        }
    }

    public void submitValue() {

        String value = getValue().trim();
        if (value.equals(profileName) || value.isEmpty()) {
            animationElapsed = 0;
            successfulSubmit = true;
            setValue(profileName);
            setFocused(false);
        } else if (wrongValue) {
            animationElapsed = 0;
            successfulSubmit = false;
        } else {
            animationElapsed = 0;
            successfulSubmit = true;
            this.profileName = value;
            onSubmit.run();
            setFocused(false);
        }
    }

    public void setIsWrongValue(boolean wrongValue) {
        this.wrongValue = wrongValue;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX - spacedText, mouseY, button);
    }
}