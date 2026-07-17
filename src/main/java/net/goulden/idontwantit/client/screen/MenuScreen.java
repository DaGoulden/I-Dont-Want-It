package net.goulden.idontwantit.client.screen;

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

public class MenuScreen extends Screen {

    public MenuScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    final int primaryColor = 0x99000000;
    final int secondaryColor = 0x55000000;
    final int hoveredAdditiveColor = 0x88000000;
    final int linesColor = 0xFFFFFF;
    final int activeColor = 0xFF00AA00;
    final int inactiveColor = 0xFFAAAAAA;

    int customHeight;
    int spacedX;
    int spacedY;
    final int spacedText = 3;
    final int spaceBetweenButtons = 1;
    int firstLineEndInY;
    int secondLineStartInY;

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

// LIST OF PROFILES
        profilesList = new ProfileListWidget(
                minecraft,
                width,
                (height - (firstLineEndInY + spaceBetweenButtons)) - (height - (secondLineStartInY - spaceBetweenButtons)),
                firstLineEndInY + spaceBetweenButtons,
                0);
        addRenderableWidget(profilesList);

// SETTINGS BUTTON
        int settingsButtonX = spacedX * 3 - customHeight;
        settingsButton = new CustomButton(
                settingsButtonX,
                secondLineStartInY,
                customHeight,
                customHeight,
                b -> {},
                primaryColor, hoveredAdditiveColor);
        addRenderableWidget(settingsButton);

// WHITELIST MODE BUTTON
        int whitelistButtonWidth = font.width("Modo Whitelist") + spacedText + customHeight;
        whitelistButton = new CustomButton(
                settingsButtonX - spaceBetweenButtons - (whitelistButtonWidth),
                secondLineStartInY,
                whitelistButtonWidth,
                customHeight,
                b -> ProfileManager.toggleWhitelistMode(),
                primaryColor, hoveredAdditiveColor);
        addRenderableWidget(whitelistButton);

// CLOSE BUTTON
        closeButton = new CustomButton(
                spacedX,
                secondLineStartInY,
                spacedX * 2 - customHeight - whitelistButtonWidth - spaceBetweenButtons * 2,
                customHeight,
                b -> onClose(),
                primaryColor, hoveredAdditiveColor);
        addRenderableWidget(closeButton);
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
                    activeColor);
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
                        b -> minecraft.setScreen(new EditProfileItemsScreen(MenuScreen.this, profileName)),
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
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hover, float partialTick) {

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
                float stateProgress = stateButtonElapsed / (float) stateButtonDuration;
                int stateColor = lerpColor(inactiveColor, activeColor, stateProgress);
                g.fill( stateBoxX + 2 + (int)((stateBoxWidth - stateBoxHeight) * stateProgress),
                        stateBoxY + 2,
                        stateBoxX + stateBoxHeight - 2 + (int)((stateBoxWidth - stateBoxHeight) * stateProgress),
                        stateBoxY + stateBoxHeight - 2,
                        stateColor);

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
                    float deleteT = deleteConfirmationElapsed / (float) deleteConfirmationDuration;
                    int deleteAlpha = (int)((1f - deleteT) * 255);
                    int deleteColor = (deleteAlpha << 24) | 0xFF0000;
                    if (deleteAlpha > 0) {
                        g.fill( deleteButton.getX(),
                            deleteButton.getY(),
                            deleteButton.getX() + deleteButton.getWidth(),
                            deleteButton.getY() + deleteButton.getHeight(),
                            deleteColor);
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
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean result = super.mouseClicked(mouseX, mouseY, button);
                this.setFocused(null);
                return result;
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

// ================= CREATE ENTRY =================
        class CreateProfileEntry extends EntryBase {

            private final Button createProfileButton;

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
                        secondaryColor, hoveredAdditiveColor);
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hover, float partialTick) {

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

        @Override
        protected void renderListBackground(@NotNull GuiGraphics g) {}

        @Override
        protected void renderListSeparators(@NotNull GuiGraphics g) {}
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        this.setFocused(null);
        return result;
    }

    @Override
    public void tick() {
        for (var entry : profilesList.children()) {
            if (entry instanceof ProfileListWidget.ProfileEntry profileEntry) {
                profileEntry.tick();
            }
        }
    }

    private int lerpColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int)(a1 + (a2 - a1) * t);
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}