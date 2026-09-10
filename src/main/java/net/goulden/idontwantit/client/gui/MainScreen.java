package net.goulden.idontwantit.client.gui;

import net.goulden.idontwantit.client.gui.widgets.custom.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.ProfilesList;
import net.goulden.idontwantit.client.gui.widgets.SettingsWidget;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.util.GUIVariables.*;

public class MainScreen extends Screen {

    public MainScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    ProfilesList profilesList;
    private CustomButton settingsButton;
    private CustomButton whitelistButton;
    private CustomButton closeButton;
    SettingsWidget settingsWidget;

    int settingsButtonX;
    int whitelistButtonWidth;
    int whitelistButtonX;

    @Override
    protected void init() {
        super.init();

        recalculate(height, font);
        settingsButtonX = width - spacedX - customHeight;
        whitelistButtonWidth = spacedText + font.width("Modo Whitelist") + customHeight;
        whitelistButtonX = settingsButtonX - spaceBetweenButtons - whitelistButtonWidth;

// LIST OF PROFILES
        profilesList = new ProfilesList(
                minecraft,
                MainScreen.this,
                spacedX + customHeight + spaceBetweenButtons,
                firstLineEndInY + spaceBetweenButtons,
                width - (spacedX + customHeight + spaceBetweenButtons) * 2,
                height - (firstLineEndInY + spaceBetweenButtons) - (height - (secondLineStartInY - spaceBetweenButtons)),
                spacedText * 2 + fontHeight * 2 + spacedText / 4 * 2
        );
        addRenderableWidget(profilesList);

// CLOSE BUTTON
        closeButton = new CustomButton(
                b -> onClose(),
                "Cerrar",
                true,
                spacedX,
                secondLineStartInY,
                width - spacedX * 2 - customHeight - whitelistButtonWidth - spaceBetweenButtons * 2,
                customHeight,
                1
        );
        addRenderableWidget(closeButton);

// WHITELIST MODE BUTTON
        whitelistButton = new CustomButton(
                b -> ProfileManager.toggleWhitelistMode(),
                "Modo Whitelist",
                false,
                whitelistButtonX,
                secondLineStartInY,
                whitelistButtonWidth,
                customHeight,
                1
        );
        addRenderableWidget(whitelistButton);

// SETTINGS BUTTON
        settingsButton = new CustomButton(
                b -> {
                    settingsWidget = new SettingsWidget(
                            0,
                            0,
                            200,
                            200,
                            () -> {
                                removeWidget(settingsWidget);
                                settingsWidget = null;
                            }
                    );
                    addRenderableWidget(settingsWidget);
                },
                settingsButtonX,
                secondLineStartInY,
                customHeight,
                customHeight,
                1
        );
        addRenderableWidget(settingsButton);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        recalculate(height, font);
        int activeCount = ProfileManager.getActiveProfileCount();
        String activeCountText = activeCount + (activeCount == 1 ? " activo de " : " activos de ") + ProfileManager.getAllProfiles().size();
        int checkboxSize = customHeight - spacedText * 2;
        settingsButtonX = width - spacedX - customHeight;
        whitelistButtonWidth = spacedText + font.width("Modo Whitelist") + customHeight;
        whitelistButtonX = settingsButtonX - spaceBetweenButtons - whitelistButtonWidth;
        int checkboxX = whitelistButtonX + whitelistButtonWidth - spacedText - checkboxSize;
        int checkboxY = secondLineStartInY + spacedText;

// SCREEN NAME
        g.fill(spacedX,
                spacedY,
                (width - spacedX - font.width(activeCountText) - spacedText * 2) - spaceBetweenButtons,
                firstLineEndInY,
                primaryColor
        );
        g.drawString(font,
                "I Don't Want It",
                spacedX + spacedText,
                spacedY + spacedText,
                linesColor
        );

// ACTIVE COUNT
        g.fill(width - spacedX - font.width(activeCountText) - spacedText * 2,
                spacedY,
                width - spacedX,
                firstLineEndInY,
                primaryColor
        );
        g.drawString(font,
                activeCountText,
                width - spacedX - font.width(activeCountText) - spacedText,
                spacedY + spacedText,
                (linesColor & 0x00FFFFFF) | 0xCC000000
        );

// LIST OF PROFILES
        profilesList.setPosition(
                spacedX + customHeight + spaceBetweenButtons,
                firstLineEndInY + spaceBetweenButtons
        );
        profilesList.setSize(
                width - (spacedX + customHeight + spaceBetweenButtons) * 2,
                height - (firstLineEndInY + spaceBetweenButtons) - (height - (secondLineStartInY - spaceBetweenButtons))
        );
        profilesList.setItemHeight(spacedText * 2 + fontHeight * 2 + spacedText / 4 * 2);

// CLOSE BUTTON
        closeButton.setPosition(
                spacedX,
                secondLineStartInY
        );
        closeButton.setSize(
                width - spacedX * 2 - customHeight - whitelistButtonWidth - spaceBetweenButtons * 2,
                customHeight
        );

// WHITELIST MODE BUTTON
        whitelistButton.setPosition(
                whitelistButtonX,
                secondLineStartInY
        );
        whitelistButton.setSize(
                whitelistButtonWidth,
                customHeight
        );
        g.renderOutline(
                checkboxX,
                checkboxY,
                checkboxSize,
                checkboxSize,
                linesColor
        );
        g.fill(checkboxX + 2,
                checkboxY + 2,
                checkboxX + 2 + (checkboxSize - 4),
                checkboxY + 2 + (checkboxSize - 4),
                ProfileManager.isWhitelistMode() ? goodMeaningColor : badMeaningColor
        );

// SETTINGS BUTTON
        settingsButton.setPosition(
                settingsButtonX,
                secondLineStartInY
        );
        settingsButton.setSize(
                customHeight,
                customHeight
        );
        g.pose().pushPose();
        g.pose().translate(
                settingsButtonX + (float) customHeight / 2,
                secondLineStartInY + (float) customHeight / 2,
                0
        );
        g.pose().scale(
                2,
                2,
                1
        );
        g.drawString(font,
                "⛭",
                (float) -font.width("⛭") / 2,
                (float) -fontHeight / 2,
                linesColor,
                true
        );
        g.pose().popPose();
    }

    public void tick() {

        profilesList.tick();

        for (var entry : profilesList.children()) {
            if (entry instanceof ProfilesList.ProfileEntry profileEntry) {
                profileEntry.tick();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (settingsWidget != null) return settingsWidget.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (settingsWidget != null) return settingsWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (settingsWidget != null) return settingsWidget.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (settingsWidget != null) return settingsWidget.charTyped(chr, keyCode);
        return super.charTyped(chr, keyCode);
    }
}