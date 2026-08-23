package net.goulden.idontwantit.client.gui;

import net.goulden.idontwantit.client.gui.widgets.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.CustomContainerList;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.*;

public class MainScreen extends Screen {

    public MainScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    ProfileListWidget profilesList;
    private CustomButton settingsButton;
    private CustomButton whitelistButton;
    private CustomButton closeButton;

    @Override
    protected void init() {
        super.init();

        recalculate(width, height, font.lineHeight);

// LIST OF PROFILES
        profilesList = new ProfileListWidget(minecraft);
        addRenderableWidget(profilesList);

// CLOSE BUTTON
        closeButton = new CustomButton(
                b -> onClose(),
                "Cerrar",
                true
        );
        addRenderableWidget(closeButton);

// WHITELIST MODE BUTTON
        whitelistButton = new CustomButton(
                b -> ProfileManager.toggleWhitelistMode(),
                "Modo Whitelist",
                false
        );
        addRenderableWidget(whitelistButton);

// SETTINGS BUTTON
        settingsButton = new CustomButton(
                b -> {}
        );
        addRenderableWidget(settingsButton);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        recalculate(width, height, font.lineHeight);

        int activeCount = ProfileManager.getActiveProfileCount();
        int totalCount = ProfileManager.getAllProfiles().size();
        String activeCountText = activeCount + (activeCount == 1 ? " activo de " : " activos de ") + totalCount;
        int settingsButtonX = width - spacedX - customHeight;
        int whitelistButtonWidth = spacedText + font.width(whitelistButton.getText()) + customHeight;

// SCREEN NAME
        g.fill(spacedX,
                spacedY,
                width - spacedX - font.width(activeCountText) - spacedText * 2 - spaceBetweenButtons,
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
                linesColor | (0xBB << 24)
        );

// LIST OF PROFILES
        profilesList.setPosition(
                spacedX + customHeight + spaceBetweenButtons,
                firstLineEndInY + spaceBetweenButtons
        );
        profilesList.setSize(
                width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2,
                (height - (firstLineEndInY + spaceBetweenButtons)) - (height - (secondLineStartInY - spaceBetweenButtons))
        );
        profilesList.setItemHeight(spacedText * 2 + fontHeight * 2 + 2);

// CLOSE BUTTON
        closeButton.setPosition(
                spacedX,
                secondLineStartInY
        );
        closeButton.setSize(
                width - spacedX * 2 - customHeight - whitelistButtonWidth - spaceBetweenButtons * 2,
                customHeight
        );
        closeButton.setUsedColor(primaryColor);

// WHITELIST MODE BUTTON
        whitelistButton.setPosition(
                settingsButton.getX() - spaceBetweenButtons - (whitelistButtonWidth),
                secondLineStartInY
        );
        whitelistButton.setSize(
                whitelistButtonWidth,
                customHeight
        );
        whitelistButton.setUsedColor(primaryColor);
        int checkboxSize = customHeight - spacedText * 2;
        int checkboxX = whitelistButton.getX() + whitelistButton.getWidth() - spacedText - checkboxSize;
        int checkboxY = whitelistButton.getY() + spacedText;
        g.renderOutline(
                checkboxX,
                checkboxY,
                checkboxSize,
                checkboxSize,
                linesColor | (0xFF << 24)
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
        settingsButton.setUsedColor(primaryColor);
        g.pose().pushPose();
        g.pose().translate(
                settingsButton.getX() + (float) settingsButton.getWidth() / 2,
                settingsButton.getY() + (float) settingsButton.getHeight() / 2,
                0
        );
        g.pose().scale(
                2/*(float) (settingsButton.getWidth() - spacedText * 2) / font.width("⛭")*/,
                2/*(float) (settingsButton.getHeight() - spacedText * 2) / font.width("⛭")*/,
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

    @Override
    public void tick() {
        for (var entry : profilesList.children()) {
            if (entry instanceof ProfileListWidget.ProfileEntry profileEntry) {
                profileEntry.tick();
            }
        }
    }

// - - - PROFILE LIST - - -
    private class ProfileListWidget extends CustomContainerList<ProfileListWidget.EntryBase> {

        public ProfileListWidget(Minecraft minecraft) {
            super(minecraft);
            refreshList();
        }

        abstract static class EntryBase extends CustomContainerList.Entry<EntryBase> {}

        class ProfileEntry extends EntryBase {

            private final CustomButton profileButton;
            private final CustomButton moveUpButton;
            private final CustomButton moveDownButton;
            private final CustomButton stateButton;
            private final CustomButton deleteButton;

            final String profileName;

            boolean isMouseOverProfile;

            int stateButtonElapsed;
            int deleteConfirmationElapsed;

            public ProfileEntry(String profileName) {
                this.profileName = profileName;

                stateButtonElapsed = ProfileManager.isProfileActive(profileName) ? stateButtonDuration : 0;
                deleteConfirmationElapsed = deleteConfirmationDuration + 1;

// PROFILE BUTTON
                profileButton = new CustomButton(
                        b -> minecraft.setScreen(new EditProfileScreen(MainScreen.this, profileName))
                );

// MOVE UP BUTTON
                moveUpButton = new CustomButton(
                        b -> {
                            ProfileManager.moveProfileUp(profileName);
                            refreshList();
                        }
                );

// MOVE DOWN BUTTON
                moveDownButton = new CustomButton(
                        b -> {
                            ProfileManager.moveProfileDown(profileName);
                            refreshList();
                        }
                );

// STATE BUTTON
                stateButton = new CustomButton(
                        b -> ProfileManager.toggleProfileState(profileName)
                );

// DELETE BUTTON
                deleteButton = new CustomButton(
                        b -> {
                            if (deleteConfirmationElapsed < deleteConfirmationDuration) {
                                ProfileManager.deleteProfile(profileName);
                                refreshList();
                            }
                            deleteConfirmationElapsed = 0;
                        }
                );
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

                List<String> allNames = new ArrayList<>(ProfileManager.getAllProfiles().keySet());
                int myIndex = allNames.indexOf(profileName);
                isMouseOverProfile = mouseX >= left && mouseY >= top && mouseX < left + width && mouseY < top + height;

// PROFILE BUTTON
                profileButton.setPosition(
                        left,
                        top
                );
                profileButton.setSize(
                        isMouseOverProfile ? width - height * 2 - height / 2 - spaceBetweenButtons : width - height * 2 - spaceBetweenButtons,
                        height
                );
                profileButton.setUsedColor(secondaryColor);
                profileButton.render(g, mouseX, mouseY, partialTick);
                g.renderItem(
                        new ItemStack(ProfileManager.getProfile(profileName).getIconItem()),
                        left + (height - iconSize) / 2,
                        top + (height - iconSize) / 2
                );
                g.drawString(font,
                        profileName,
                        left + height,
                        top + spacedText,
                        linesColor
                );
                int count = ProfileManager.getProfile(profileName).ignoredItems.size();
                g.drawString(font,
                        count + (count == 1 ? " item" : " items"),
                        left + height + spacedText,
                        top + spacedText + fontHeight + 2,
                        linesColor | (0xAA << 24)
                );

// MOVE UP BUTTON
                moveUpButton.setPosition(
                        left + width - height * 2 - height / 2 - spaceBetweenButtons,
                        top
                );
                moveUpButton.setSize(
                        height / 2,
                        height / 2
                );
                moveUpButton.setUsedColor(secondaryColor);
                if (isMouseOverProfile) {
                    if (myIndex > 0) {
                        moveUpButton.render(g, mouseX, mouseY, partialTick);
                        g.drawString(font,
                                "\uD83E\uDC39",
                                (float) (moveUpButton.getX() + (double) moveUpButton.getWidth() / 2 - 2),
                                (float) (moveUpButton.getY() + (double) moveUpButton.getHeight() / 2 - 4),
                                linesColor,
                                true
                        );
                    } else {
                        g.fill(moveUpButton.getX(),
                                moveUpButton.getY(),
                                moveUpButton.getX() + moveUpButton.getWidth(),
                                moveUpButton.getY() + moveUpButton.getHeight(),
                                secondaryColor
                        );
                    }
                }

// MOVE DOWN BUTTON
                moveDownButton.setPosition(
                        left + width - height * 2 - height / 2 - spaceBetweenButtons,
                        top + height / 2
                );
                moveDownButton.setSize(
                        height / 2,
                        height / 2
                );
                moveDownButton.setUsedColor(secondaryColor);
                if (isMouseOverProfile) {
                    if (myIndex < allNames.size() - 1) {
                        moveDownButton.render(g, mouseX, mouseY, partialTick);
                        g.drawString(font,
                                "\uD83E\uDC3B",
                                (float) (moveDownButton.getX() + (double) moveDownButton.getWidth() / 2 - 2),
                                (float) (moveDownButton.getY() + (double) moveDownButton.getHeight() / 2 - 4),
                                linesColor,
                                true
                        );
                    } else {
                        g.fill(moveDownButton.getX(),
                                moveDownButton.getY(),
                                moveDownButton.getX() + moveDownButton.getWidth(),
                                moveDownButton.getY() + moveDownButton.getHeight(),
                                secondaryColor
                        );
                    }
                }

// STATE BUTTON
                stateButton.setPosition(
                        left + width - height * 2 - spaceBetweenButtons,
                        top
                );
                stateButton.setSize(
                        height,
                        height
                );
                stateButton.setUsedColor(secondaryColor);
                stateButton.render(g, mouseX, mouseY, partialTick);
                int stateBoxWidth = stateButton.getWidth() / 2;
                int stateBoxHeight = stateButton.getHeight() / 4;
                int stateBoxX = stateButton.getX() + stateButton.getWidth() / 2 - stateBoxWidth / 2;
                int stateBoxY = stateButton.getY() + stateButton.getHeight() / 2 - stateBoxHeight / 2;
                g.renderOutline(
                        stateBoxX,
                        stateBoxY,
                        stateBoxWidth,
                        stateBoxHeight,
                        linesColor | (0xFF << 24)
                );
                float stateProgress = easeInOutCubic((float) stateButtonElapsed / stateButtonDuration);
                g.pose().pushPose();
                g.pose().translate(
                        stateBoxX + ((stateBoxWidth - stateBoxHeight) * stateProgress),
                        stateBoxY,
                        0
                );
                g.fill(2,
                        2,
                        stateBoxHeight - 2,
                        stateBoxHeight - 2,
                        lerpColor(badMeaningColor, goodMeaningColor, stateProgress)
                );
                g.pose().popPose();

// DELETE BUTTON
                deleteButton.setPosition(
                        left + width - height,
                        top
                );
                deleteButton.setSize(
                        height,
                        height
                );
                deleteButton.setUsedColor(secondaryColor);
                deleteButton.render(g, mouseX, mouseY, partialTick);
                g.drawCenteredString(font,
                        "\uD83D\uDDD1",
                        deleteButton.getX() + deleteButton.getWidth() / 2,
                        deleteButton.getY() + deleteButton.getHeight() / 2 - fontHeight / 2,
                        linesColor
                );
                if (deleteConfirmationElapsed <= deleteConfirmationDuration) {
                    float deleteProgress = (float) deleteConfirmationElapsed / deleteConfirmationDuration;
                    int deleteAlpha = Math.max(5, (int)((1f - deleteProgress) * 255));
                    g.fill(deleteButton.getX(),
                            deleteButton.getY(),
                            deleteButton.getX() + deleteButton.getWidth(),
                            deleteButton.getY() + deleteButton.getHeight(),
                            (deleteAlpha << 24) | (badMeaningColor & 0x00FFFFFF)
                    );
                    g.drawCenteredString(font,
                            "¿\uD83D\uDDD1?",
                            deleteButton.getX() + deleteButton.getWidth() / 2,
                            deleteButton.getY() + deleteButton.getHeight() / 2 - fontHeight / 2,
                            linesColor | (deleteAlpha << 24)
                    );
                }
            }

            public void tick() {

                boolean isActive = ProfileManager.isProfileActive(profileName);
                if (isActive && stateButtonElapsed < stateButtonDuration) {
                    stateButtonElapsed++;
                } else if (!isActive && stateButtonElapsed > 0) {
                    stateButtonElapsed--;
                }

                if (deleteConfirmationElapsed <= deleteConfirmationDuration) {
                    deleteConfirmationElapsed++;
                }
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() {
                return List.of(profileButton, moveUpButton, moveDownButton, stateButton, deleteButton);
            }

            @Override
            public @NotNull List<? extends NarratableEntry> narratables() {
                return List.of(profileButton, moveUpButton, moveDownButton, stateButton, deleteButton);
            }
        }

        class CreateProfileEntry extends EntryBase {

            private final CustomButton createProfileButton;

            public CreateProfileEntry() {

// CREATE PROFILE BUTTON
                createProfileButton = new CustomButton(
                        b -> {
                            int count = 1;
                            String name = "Nuevo Perfil " + count;

                            while (ProfileManager.getAllProfiles().containsKey(name)) {
                                count++;
                                name = "Nuevo Perfil " + count;
                            }

                            ProfileManager.createProfile(name, "minecraft:paper");
                            refreshList();
                        }
                );
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

// CREATE PROFILE BUTTON
                createProfileButton.setPosition(
                        left,
                        top
                );
                createProfileButton.setSize(
                        width,
                        height
                );
                createProfileButton.setUsedColor(secondaryColor);
                createProfileButton.render(g, mouseX, mouseY, partialTick);
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() {
                return List.of(createProfileButton);
            }

            @Override
            public @NotNull List<? extends NarratableEntry> narratables() {
                return List.of(createProfileButton);
            }
        }

        public void refreshList() {
            clearEntries();

            for (String name : ProfileManager.getAllProfiles().keySet()) {
                addEntry(new ProfileEntry(name));
            }
            addEntry(new CreateProfileEntry());
        }
    }
}