package net.goulden.idontwantit.client.gui;

import net.goulden.idontwantit.client.gui.widgets.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.CustomList;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
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

        int settingsButtonX = width - spacedX - customHeight;
        int whitelistButtonWidth = font.width("Modo Whitelist") + spacedText + customHeight;

// LIST OF PROFILES
        profilesList = new ProfileListWidget(
                minecraft,
                spacedX + customHeight + spaceBetweenButtons,
                firstLineEndInY + spaceBetweenButtons,
                width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2,
                (height - (firstLineEndInY + spaceBetweenButtons)) - (height - (secondLineStartInY - spaceBetweenButtons)),
                spacedText * 2 + font.lineHeight * 2 + 2);
        addRenderableWidget(profilesList);

// CLOSE BUTTON
        closeButton = new CustomButton(
                spacedX,
                secondLineStartInY,
                width - spacedX * 2 - customHeight - whitelistButtonWidth - spaceBetweenButtons * 2,
                customHeight,
                b -> onClose(),
                primaryColor);
        addRenderableWidget(closeButton);

// WHITELIST MODE BUTTON
        whitelistButton = new CustomButton(
                settingsButtonX - spaceBetweenButtons - (whitelistButtonWidth),
                secondLineStartInY,
                whitelistButtonWidth,
                customHeight,
                b -> ProfileManager.toggleWhitelistMode(),
                primaryColor);
        addRenderableWidget(whitelistButton);

// SETTINGS BUTTON
        settingsButton = new CustomButton(
                settingsButtonX,
                secondLineStartInY,
                customHeight,
                customHeight,
                b -> {},
                primaryColor);
        addRenderableWidget(settingsButton);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        recalculate(width, height, font.lineHeight);

        int activeCount = ProfileManager.getActiveProfileCount();
        int totalCount = ProfileManager.getAllProfiles().size();
        String activeCountText = activeCount + (activeCount == 1 ? " activo de " : " activos de ") + totalCount;

// SCREEN NAME
        g.fill(spacedX,
                spacedY,
                width - spacedX - font.width(activeCountText) - spacedText * 2 - spaceBetweenButtons,
                firstLineEndInY,
                primaryColor);
        g.drawString(font,
                "I Don't Want It",
                spacedX + spacedText,
                spacedY + spacedText,
                linesColor);

// ACTIVE COUNT
        g.fill(width - spacedX - font.width(activeCountText) - spacedText * 2,
                spacedY,
                width - spacedX,
                firstLineEndInY,
                primaryColor);
        g.drawString(font,
                activeCountText,
                width - spacedX - font.width(activeCountText) - spacedText,
                spacedY + spacedText,
                linesColor | (0xBB << 24));

// CLOSE BUTTON
        g.drawCenteredString(font,
                "Cerrar",
                closeButton.getX() + closeButton.getWidth() / 2,
                closeButton.getY() + spacedText,
                linesColor);

// WHITELIST MODE BUTTON
        int checkboxSize = customHeight - spacedText * 2;
        int checkboxX = whitelistButton.getX() + whitelistButton.getWidth() - spacedText - checkboxSize;
        int checkboxY = whitelistButton.getY() + spacedText;
        g.drawString(font,
                "Modo Whitelist",
                whitelistButton.getX() + spacedText,
                whitelistButton.getY() + spacedText,
                linesColor);
        g.renderOutline(
                checkboxX,
                checkboxY,
                checkboxSize,
                checkboxSize,
                linesColor | (0xFF << 24));
        g.fill(checkboxX + 2,
                checkboxY + 2,
                checkboxX + 2 + (checkboxSize - 4),
                checkboxY + 2 + (checkboxSize - 4),
                ProfileManager.isWhitelistMode() ? goodMeaningColor : badMeaningColor);

// SETTINGS BUTTON
        // textura de settings
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
    private class ProfileListWidget extends CustomList<ProfileListWidget.EntryBase> {

        public ProfileListWidget(Minecraft mc, int x, int y, int width, int height, int itemHeight) {
            super(mc, width, height, y, x, itemHeight);
            refreshList();
        }

        abstract static class EntryBase extends ContainerObjectSelectionList.Entry<EntryBase> {}

        class ProfileEntry extends EntryBase {

            private final CustomButton stateButton;
            private final CustomButton nameButton;
            private final CustomButton deleteButton;

            final String profileName;

            int stateButtonElapsed;
            int deleteConfirmationElapsed;

            public ProfileEntry(String profileName) {
                this.profileName = profileName;

                stateButtonElapsed = ProfileManager.isProfileActive(profileName) ? stateButtonDuration : 0;
                deleteConfirmationElapsed = deleteConfirmationDuration + 1;

// STATE BUTTON
                stateButton = new CustomButton(
                        b -> ProfileManager.toggleProfileState(profileName),
                        secondaryColor);

// PROFILE BUTTON
                nameButton = new CustomButton(
                        b -> minecraft.setScreen(new EditProfileScreen(MainScreen.this, profileName)),
                        secondaryColor);

// DELETE BUTTON
                deleteButton = new CustomButton(
                        b -> {
                            if (deleteConfirmationElapsed < deleteConfirmationDuration) {
                                ProfileManager.deleteProfile(profileName);
                                refreshList();
                            }
                            deleteConfirmationElapsed = 0;
                        },
                        secondaryColor);
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

// STATE BUTTON
                stateButton.setPosition(left + width - height * 2 - spaceBetweenButtons, top);
                stateButton.setSize(height, height);
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
                        linesColor | (0xFF << 24));
                float stateProgress = easeInOutCubic((float) stateButtonElapsed / stateButtonDuration);
                g.pose().pushPose();
                g.pose().translate(stateBoxX + ((stateBoxWidth - stateBoxHeight) * stateProgress), stateBoxY, 0);
                g.fill(2,
                        2,
                        stateBoxHeight - 2,
                        stateBoxHeight - 2,
                        lerpColor(badMeaningColor, goodMeaningColor, stateProgress));
                g.pose().popPose();

// PROFILE BUTTON
                nameButton.setPosition(left, top);
                nameButton.setSize(width - height * 2 - spaceBetweenButtons, height);
                nameButton.render(g, mouseX, mouseY, partialTick);
                g.renderItem(new ItemStack(ProfileManager.getProfile(profileName).getIconItem()),
                        left + (height - itemSize) / 2,
                        top + (height - itemSize) / 2);
                g.drawString(minecraft.font,
                        profileName,
                        left + height,
                        top + spacedText,
                        linesColor);
                int count = ProfileManager.getProfile(profileName).ignoredItems.size();
                g.drawString(minecraft.font,
                        count + (count == 1 ? " item" : " items"),
                        left + height + spacedText,
                        top + spacedText + font.lineHeight + 2,
                        linesColor | (0xAA << 24));

// DELETE BUTTON
                deleteButton.setPosition(left + width - height, top);
                deleteButton.setSize(height, height);
                deleteButton.render(g, mouseX, mouseY, partialTick);
                g.drawCenteredString(font,
                        "\uD83D\uDDD1",
                        deleteButton.getX() + deleteButton.getWidth() / 2,
                        deleteButton.getY() + deleteButton.getHeight() / 2 - font.lineHeight / 2,
                        linesColor);
                if (deleteConfirmationElapsed <= deleteConfirmationDuration) {
                    float deleteProgress = (float) deleteConfirmationElapsed / deleteConfirmationDuration;
                    int deleteAlpha = Math.max(5, (int)((1f - deleteProgress) * 255));
                    g.fill(deleteButton.getX(),
                            deleteButton.getY(),
                            deleteButton.getX() + deleteButton.getWidth(),
                            deleteButton.getY() + deleteButton.getHeight(),
                            (deleteAlpha << 24) | (badMeaningColor & 0x00FFFFFF));
                    g.drawCenteredString(font,
                            "¿\uD83D\uDDD1?",
                            deleteButton.getX() + deleteButton.getWidth() / 2,
                            deleteButton.getY() + deleteButton.getHeight() / 2 - font.lineHeight / 2,
                            linesColor | (deleteAlpha << 24));
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
                return List.of(stateButton, nameButton, deleteButton);
            }

            @Override
            public @NotNull List<? extends NarratableEntry> narratables() {
                return List.of(stateButton, nameButton, deleteButton);
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
                        },
                        secondaryColor);
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

// CREATE PROFILE BUTTON
                createProfileButton.setPosition(left, top);
                createProfileButton.setSize(width, height);
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

            List<String> names = new ArrayList<>(ProfileManager.getAllProfiles().keySet());
            names.sort(String::compareToIgnoreCase);

            for (String name : names) addEntry(new ProfileEntry(name));
            addEntry(new CreateProfileEntry());
        }
    }
}