package net.goulden.idontwantit.client.gui;

import net.goulden.idontwantit.client.gui.widgets.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.CustomEditBox;
import net.goulden.idontwantit.client.gui.widgets.IconSelectorWidget;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static net.goulden.idontwantit.util.ScreenVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.*;

public class EditProfileItemsScreen extends Screen {

    private final Screen parent;
    private String profileName;

    public EditProfileItemsScreen(Screen parent, String profileName) {
        super(Component.empty());
        this.parent = parent;
        this.profileName = profileName;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    static int iconSize = 16;
    static int iconSelectorWidth = iconSize + spacedText * 2;

    int editBoxErrorDuration = 20;
    int editBoxErrorElapsed;

    private Button iconSelectorButton;
    private EditBox nameEditBox;

    private IconSelectorWidget iconSelectorWidget;

// ================= INIT =================
    @Override
    protected void init() {
        super.init();

        editBoxErrorElapsed = editBoxErrorDuration + 1;

// ICON SELECTOR BUTTON
        iconSelectorButton = new CustomButton(
                spacedX,
                spacedY,
                iconSelectorWidth,
                iconSelectorWidth,
                b -> iconSelectorWidget = new IconSelectorWidget(
                        (Runnable) (iconSelectorWidget = null),
                        profileName,
                        width / 2,
                        height / 2),
                primaryColor,
                hoveredAdditiveColor);
        addRenderableWidget(iconSelectorButton);

// NAME EDIT BOX
        nameEditBox = new CustomEditBox(font,
                spacedX + iconSelectorWidth + spaceBetweenButtons + spacedText,
                spacedY + spacedText,
                (spacedX * 3) - (spacedX + iconSize + spaceBetweenButtons),
                customHeight,
                Component.literal("Nombre del perfil"),
                16,
                profileName);
        addRenderableWidget(nameEditBox);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

// ICON SELECTOR BUTTON
        ProfileManager.ItemProfile profile = ProfileManager.getProfile(profileName);
        ItemStack iconStack = new ItemStack(profile.getIconItem());
        g.renderItem(iconStack, spacedX + spacedText, spacedY + spacedText);

// NAME EDIT BOX
//        g.pose().pushPose();
//        g.pose().translate(spacedText, spacedText, 0);
//        nameEditBox.render(g, mouseX, mouseY, partialTick);
//        g.pose().popPose();
        if (editBoxErrorElapsed <= editBoxErrorDuration) {
            float errorProgress = easeInOutCubic((float) editBoxErrorElapsed / editBoxErrorDuration);
            int errorAlpha = (int)((1f - errorProgress) * 255);
            if (errorAlpha > 2) {
                g.drawString(font,
                        nameEditBox.getValue().trim(),
                        iconSelectorWidth + spaceBetweenButtons + spacedText,
                        spacedY,
                        (errorAlpha << 24) | badMeaningColor);
            }
        }
    }

    public void tick() {
        if (editBoxErrorElapsed <= editBoxErrorDuration) {
            editBoxErrorElapsed++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (!nameEditBox.isMouseOver(mouseX, mouseY)) nameEditBox.setFocused(false);
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335 && nameEditBox.isFocused()) {

            String newName = nameEditBox.getValue().trim();

            if (newName.equals(profileName)) {
                nameEditBox.setFocused(false);
            } else if (newName.isEmpty() || ProfileManager.getAllProfiles().containsKey(newName)) {
                editBoxErrorElapsed = 0;
            } else {
                ProfileManager.renameProfile(profileName, newName);
                nameEditBox.setFocused(false);
                profileName = newName;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}