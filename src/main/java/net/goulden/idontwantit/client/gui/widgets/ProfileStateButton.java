package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.widgets.custom.CustomButton;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.easeInOutQuart;
import static net.goulden.idontwantit.util.RenderUtils.lerpColor;

public class ProfileStateButton extends CustomButton {

    private final String profileName;
    private int animationElapsed;

    public ProfileStateButton(OnPress onPress, String profileName, int backgroundColor) {
        super(onPress, backgroundColor);
        this.profileName = profileName;
        animationElapsed = ProfileManager.isProfileActive(profileName) ? stateButtonDuration : 0;
    }

    public ProfileStateButton(OnPress onPress, String profileName, int x, int y, int w, int h, int backgroundColor) {
        super(onPress, x, y, w, h, backgroundColor);
        this.profileName = profileName;
        animationElapsed = ProfileManager.isProfileActive(profileName) ? stateButtonDuration : 0;
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
                        ? easeInOutQuart((float) animationElapsed / stateButtonDuration)
                        : ProfileManager.isProfileActive(profileName) ? 1 : 0;

        g.renderOutline(
                x,
                y,
                w,
                h,
                linesColor
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

    public void tick() {
        if (ProfileManager.isProfileActive(profileName) && animationElapsed < stateButtonDuration) {
            animationElapsed++;
        } else if (!ProfileManager.isProfileActive(profileName) && animationElapsed > 0) {
            animationElapsed--;
        } else if (animationElapsed > stateButtonDuration) {
            animationElapsed = ProfileManager.isProfileActive(profileName) ? stateButtonDuration : 0;
        }
    }
}
