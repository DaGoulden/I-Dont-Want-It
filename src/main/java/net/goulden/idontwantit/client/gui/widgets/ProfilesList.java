package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.EditProfileScreen;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomContainerList;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.*;

public class ProfilesList extends CustomContainerList<ProfilesList.EntryBase> {

    Minecraft minecraft;
    Screen parent;

    public ProfilesList(Minecraft mc, Screen parent, int x, int y, int w, int h, int itemHeight) {
        super(x, y, w, h, itemHeight);
        this.minecraft = mc;
        this.parent = parent;
        refreshList();
    }

    public abstract static class EntryBase extends CustomContainerList.Entry<EntryBase> {}

    public class ProfileEntry extends EntryBase {

        private final CustomButton profileButton;
        private final CustomButton moveUpButton;
        private final CustomButton moveDownButton;
        private final ProfileStateButton stateButton;
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
                    b -> minecraft.setScreen(
                            new EditProfileScreen(
                                    parent,
                                    profileName
                            )
                    )
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
            stateButton = new ProfileStateButton(
                    b -> ProfileManager.toggleProfileState(profileName),
                    profileName,
                    stateButtonElapsed
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
        public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, float partialTick) {

            int profileCount = ProfileManager.getProfile(profileName).ignoredItems.size();
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
            profileButton.setBackgroundColor(secondaryColor);
            profileButton.render(g, mouseX, mouseY, partialTick);
            g.renderItem(
                    new ItemStack(ProfileManager.getProfile(profileName).getIconItem()),
                    left + (height - iconSize) / 2,
                    top + (height - iconSize) / 2
            );
            drawCutString(g,
                    mouseX,
                    mouseY,
                    profileName,
                    profileButton.getWidth() - (height + spacedText),
                    left + height,
                    top + spacedText,
                    linesColor
            );
            g.drawString(font,
                    profileCount + (profileCount == 1 ? " item" : " items"),
                    left + height + spacedText,
                    top + spacedText + fontHeight + spacedText / 4 * 2,
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
            moveUpButton.setBackgroundColor(secondaryColor);
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
                            moveUpButton.getRight(),
                            moveUpButton.getBottom(),
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
            moveDownButton.setBackgroundColor(secondaryColor);
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
                            moveDownButton.getRight(),
                            moveDownButton.getBottom(),
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
            stateButton.setBackgroundColor(secondaryColor);
            stateButton.setElapsed(stateButtonElapsed);
            stateButton.render(g, mouseX, mouseY, partialTick);

// DELETE BUTTON
            deleteButton.setPosition(
                    left + width - height,
                    top
            );
            deleteButton.setSize(
                    height,
                    height
            );
            deleteButton.setBackgroundColor(secondaryColor);
            deleteButton.render(g, mouseX, mouseY, partialTick);
            g.drawCenteredString(font,
                    "\uD83D\uDDD1",
                    deleteButton.getX() + deleteButton.getWidth() / 2,
                    deleteButton.getY() + (deleteButton.getHeight() - fontHeight) / 2,
                    linesColor
            );
            if (deleteConfirmationElapsed <= deleteConfirmationDuration) {
                float deleteProgress = easeInOutCubic((float) deleteConfirmationElapsed / deleteConfirmationDuration);
                int deleteAlpha = Math.max(5, (int)((1f - deleteProgress) * 255));
                g.fill(deleteButton.getX(),
                        deleteButton.getY(),
                        deleteButton.getRight(),
                        deleteButton.getBottom(),
                        (deleteAlpha << 24) | (badMeaningColor & 0x00FFFFFF)
                );
                g.drawCenteredString(font,
                        "¿\uD83D\uDDD1?",
                        deleteButton.getX() + deleteButton.getWidth() / 2,
                        deleteButton.getY() + (deleteButton.getHeight() - fontHeight) / 2,
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
    }

    public class CreateProfileEntry extends EntryBase {

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
        public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, float partialTick) {

// CREATE PROFILE BUTTON
            createProfileButton.setPosition(
                    left,
                    top
            );
            createProfileButton.setSize(
                    width,
                    height
            );
            createProfileButton.setBackgroundColor(secondaryColor);
            createProfileButton.render(g, mouseX, mouseY, partialTick);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
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