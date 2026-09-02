package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.widgets.custom.CustomButton;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.easeInOutCubic;
import static net.goulden.idontwantit.util.RenderUtils.lerpColor;

public class ProfileStateButton extends CustomButton {

    private final String profileName;
    private int elapsed;

    public ProfileStateButton(OnPress onPress, String profileName, int elapsed) {
        super(onPress);
        this.profileName = profileName;
        this.elapsed = elapsed;
    }

    public ProfileStateButton(OnPress onPress, String profileName, int elapsed, int x, int y, int w, int h, int color) {
        super(onPress, x, y, w, h, color);
        this.profileName = profileName;
        this.elapsed = elapsed;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(g, mouseX, mouseY, partialTick);

        int w = getWidth() / 2;
        int h = getHeight() / 4;
        int x = getX() + getWidth() / 2 - w / 2;
        int y = getY() + getHeight() / 2 - h / 2;
        float progress =
                !(stateButtonDuration == 0)
                        ? easeInOutCubic((float) elapsed / stateButtonDuration)
                        : ProfileManager.isProfileActive(profileName) ? 1 : 0;

        g.renderOutline(
                x,
                y,
                w,
                h,
                linesColor | (0xFF << 24)
        );

        g.pose().pushPose();
        g.pose().translate(
                x + ((w - h) * progress),
                y,
                0
        );
        g.fill(2,
                2,
                h - 2,
                h - 2,
                lerpColor(badMeaningColor, goodMeaningColor, progress)
        );
        g.pose().popPose();
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }
}
