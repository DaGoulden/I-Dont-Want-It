package net.goulden.idontwantit.client.gui;

import net.goulden.idontwantit.client.gui.widgets.CustomButton;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.goulden.idontwantit.util.ScreenVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.*;

public class MainScreen extends Screen {

    public MainScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    int stateButtonDuration = 10;
    int deleteConfirmationDuration = 30;

    ProfileListWidget profilesList;
    private Button settingsButton;
    private Button whitelistButton;
    private Button closeButton;

// ================= INIT =================
    @Override
    protected void init() {
        super.init();

        customHeight = font.lineHeight + spacedText * 2;
        spacedX = width / 4;
        spacedY = height / 10;
        firstLineEndInY = spacedY + customHeight;
        secondLineStartInY = spacedY * 9 - customHeight;
        int settingsButtonX = spacedX * 3 - customHeight;
        int whitelistButtonWidth = font.width("Modo Whitelist") + spacedText + customHeight;

// LIST OF PROFILES
        profilesList = new ProfileListWidget(
                minecraft,
                width,
                (height - (firstLineEndInY + spaceBetweenButtons)) - (height - (secondLineStartInY - spaceBetweenButtons)),
                firstLineEndInY + spaceBetweenButtons,
                0);
        addRenderableWidget(profilesList);

// CLOSE BUTTON
        closeButton = new CustomButton(
                spacedX,
                secondLineStartInY,
                spacedX * 2 - customHeight - whitelistButtonWidth - spaceBetweenButtons * 2,
                customHeight,
                b -> onClose(),
                primaryColor, hoveredAdditiveColor);
        addRenderableWidget(closeButton);

// WHITELIST MODE BUTTON
        whitelistButton = new CustomButton(
                settingsButtonX - spaceBetweenButtons - (whitelistButtonWidth),
                secondLineStartInY,
                whitelistButtonWidth,
                customHeight,
                b -> ProfileManager.toggleWhitelistMode(),
                primaryColor, hoveredAdditiveColor);
        addRenderableWidget(whitelistButton);

// SETTINGS BUTTON
        settingsButton = new CustomButton(
                settingsButtonX,
                secondLineStartInY,
                customHeight,
                customHeight,
                b -> {},
                primaryColor, hoveredAdditiveColor);
        addRenderableWidget(settingsButton);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        int activeCount = ProfileManager.getActiveProfileCount();
        int totalCount = ProfileManager.getAllProfiles().size();
        String activeCountText = activeCount + (activeCount == 1 ? " activo de " : " activos de ") + totalCount;

// SCREEN NAME
        g.fill( spacedX,
                spacedY,
                spacedX * 3 - font.width(activeCountText) - spacedText * 2 - spaceBetweenButtons,
                firstLineEndInY,
                primaryColor);
        g.drawString(font,
                "I Don't Want It",
                spacedX + spacedText,
                spacedY + spacedText,
                linesColor);

// ACTIVE COUNT
        g.fill( spacedX * 3 - font.width(activeCountText) - spacedText * 2,
                spacedY,
                spacedX * 3,
                firstLineEndInY,
                primaryColor);
        g.drawString(font,
                activeCountText,
                spacedX * 3 - font.width(activeCountText) - spacedText,
                spacedY + spacedText,
                (0xBB << 24) | linesColor);

// SETTINGS BUTTON
        // textura de settings

// WHITELIST MODE BUTTON
        int squareSize = customHeight - spacedText * 2;
        int checkboxX = spacedX * 3 - customHeight - spaceBetweenButtons - spacedText - squareSize;
        int checkboxY = secondLineStartInY + spacedText;
        g.drawString(font,
                "Modo Whitelist",
                whitelistButton.getX() + spacedText,
                whitelistButton.getY() + spacedText,
                linesColor);
        g.renderOutline(
                checkboxX,
                checkboxY,
                squareSize,
                squareSize,
                (0xFF << 24) | linesColor);
        if (ProfileManager.isWhitelistMode()) {
            g.fill( checkboxX + 2,
                    checkboxY + 2,
                    checkboxX + 2 + (squareSize - 4),
                    checkboxY + 2 + (squareSize - 4),
                    goodMeaningColor);
        }

// CLOSE BUTTON
        g.drawCenteredString(font,
                "Cerrar",
                closeButton.getX() + closeButton.getWidth() / 2,
                closeButton.getY() + spacedText,
                linesColor);
    }

// ================= CUSTOM LIST ====================
    private class ProfileListWidget extends ContainerObjectSelectionList<ProfileListWidget.EntryBase> {

        public ProfileListWidget(Minecraft mc, int width, int height, int y, int x) {
            super(mc, width, height, y, 32);
            setX(x);
            refreshList();
        }

        abstract static class EntryBase extends ContainerObjectSelectionList.Entry<EntryBase> {}

// ================= PROFILE ENTRY =================
        class ProfileEntry extends EntryBase {

            private final Button stateButton;
            private final Button nameButton;
            private final Button deleteButton;

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
                        secondaryColor, hoveredAdditiveColor);

// NAME BUTTON
                nameButton = new CustomButton(
                        b -> minecraft.setScreen(new EditProfileItemsScreen(MainScreen.this, profileName)),
                        secondaryColor, hoveredAdditiveColor);

// DELETE BUTTON
                deleteButton = new CustomButton(
                        b -> {
                            if (deleteConfirmationElapsed <= deleteConfirmationDuration) {
                                ProfileManager.deleteProfile(profileName);
                                refreshList();
                            }
                            deleteConfirmationElapsed = 0;
                        },
                        secondaryColor, hoveredAdditiveColor);
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

// STATE BUTTON
                stateButton.setPosition(left, top);
                stateButton.setSize(height, height);
                stateButton.render(g, mouseX, mouseY, partialTick);
                int stateBoxWidth = stateButton.getWidth() / 2;
                int stateBoxHeight = stateButton.getHeight() / 3;
                int stateBoxX = stateButton.getX() + stateButton.getWidth() / 2 - stateBoxWidth / 2;
                int stateBoxY = stateButton.getY() + stateButton.getHeight() / 2 - stateBoxHeight / 2;
                g.renderOutline(
                        stateBoxX,
                        stateBoxY,
                        stateBoxWidth,
                        stateBoxHeight,
                        (0xFF << 24) | linesColor);
                float stateProgress = easeInOutCubic((float) stateButtonElapsed / stateButtonDuration);
                g.pose().pushPose();
                g.pose().translate(stateBoxX + ((stateBoxWidth - stateBoxHeight) * stateProgress), stateBoxY, 0);
                g.fill( 2,
                        2,
                        stateBoxHeight - 2,
                        stateBoxHeight - 2,
                        lerpColor(secondaryColor, goodMeaningColor, stateProgress));
                g.pose().popPose();

// NAME BUTTON
                int nameButtonX = left + height + spaceBetweenButtons;
                nameButton.setPosition(nameButtonX, top);
                nameButton.setSize(width - height * 2 - spaceBetweenButtons * 2, height);
                nameButton.render(g, mouseX, mouseY, partialTick);
                g.renderItem(new ItemStack(ProfileManager.getProfile(profileName).getIconItem()),
                        nameButtonX + 4,
                        top + (height - 16) / 2);
                g.drawString(minecraft.font,
                        profileName,
                        nameButtonX + 24,
                        top + 6,
                        linesColor);
                int count = ProfileManager.getProfile(profileName).ignoredItems.size();
                g.drawString(minecraft.font,
                        count + (count == 1 ? " item" : " items"),
                        nameButtonX + 24,
                        top + 16,
                        (0xAA << 24) | linesColor);

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
                    float deleteProgress = easeInOutCubic((float) deleteConfirmationElapsed / deleteConfirmationDuration);
                    int deleteAlpha = (int)((1f - deleteProgress) * 255);
                    if (deleteAlpha > 2) {
                        g.fill( deleteButton.getX(),
                            deleteButton.getY(),
                            deleteButton.getX() + deleteButton.getWidth(),
                            deleteButton.getY() + deleteButton.getHeight(),
                            (deleteAlpha << 24) | badMeaningColor);
                        g.drawCenteredString(font,
                                "¿\uD83D\uDDD1?",
                                deleteButton.getX() + deleteButton.getWidth() / 2,
                                deleteButton.getY() + deleteButton.getHeight() / 2 - font.lineHeight / 2,
                                (deleteAlpha << 24) | linesColor);
                    }
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

        // Create Entry
        class CreateProfileEntry extends EntryBase {

            private final Button createProfileButton;

            public CreateProfileEntry() {

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
                        secondaryColor, hoveredAdditiveColor);
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

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

        @Override
        protected void renderListBackground(@NotNull GuiGraphics g) {}

        @Override
        protected void renderListSeparators(@NotNull GuiGraphics g) {}
    }

    @Override
    public void tick() {
        for (var entry : profilesList.children()) {
            if (entry instanceof ProfileListWidget.ProfileEntry profileEntry) {
                profileEntry.tick();
            }
        }
    }
}