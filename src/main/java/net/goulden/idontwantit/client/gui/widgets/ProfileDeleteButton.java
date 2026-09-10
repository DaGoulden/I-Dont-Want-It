package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.widgets.custom.CustomButton;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.easeInOutQuart;

public class ProfileDeleteButton extends CustomButton {

    private int animationElapsed = deleteConfirmationDuration;
    private final OnPress onPress;
    int deleteAlpha;

    public ProfileDeleteButton(Runnable onDelete, int backgroundColor) {
        super(b -> {}, backgroundColor);
        this.onPress = b -> {
            if (animationElapsed < deleteConfirmationDuration && deleteAlpha > 32) {
                onDelete.run();
            } else {
                animationElapsed = 0;
            }
        };
    }

    @Override
    public void onPress() {
        onPress.onPress(this);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(g, mouseX, mouseY, partialTick);

        g.drawCenteredString(font,
                "\uD83D\uDDD1",
                getX() + getWidth() / 2,
                getY() + (getHeight() - fontHeight) / 2,
                linesColor
        );
        if (animationElapsed < deleteConfirmationDuration) {
            float deleteProgress = easeInOutQuart((float) animationElapsed / deleteConfirmationDuration);
            deleteAlpha = Math.max(5, (int)((1f - deleteProgress) * 256));
            g.fill(getX(),
                    getY(),
                    getRight(),
                    getBottom(),
                    (badMeaningColor & 0x00FFFFFF) | deleteAlpha << 24
            );
            g.drawCenteredString(font,
                    "¿\uD83D\uDDD1?",
                    getX() + getWidth() / 2,
                    getY() + (getHeight() - fontHeight) / 2,
                    (linesColor & 0x00FFFFFF) | deleteAlpha << 24
            );
        }
    }
    
    public void tick() {
        if (animationElapsed < deleteConfirmationDuration) {
            animationElapsed++;
        }
    }
}