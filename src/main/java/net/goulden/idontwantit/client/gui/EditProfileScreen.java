package net.goulden.idontwantit.client.gui;

import net.goulden.idontwantit.client.gui.widgets.*;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomEditBox;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.client.gui.widgets.IgnoredItemsList.AddIgnoredItemEntry.addItemEditBox;
import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;

public class EditProfileScreen extends Screen {

    Screen parent;
    String profileName;

    public EditProfileScreen(Screen parent, String profileName) {
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
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    CustomButton iconSelectorButton;
    CustomEditBox nameEditBox;
    CustomButton stateButton;
    CustomButton itemsTabButton;
    CustomButton tagsTabButton;
    public static IgnoredItemsList ignoredItemsList;
    public static AvailableItemsList availableItemsList;
    public static IgnoredTagsList ignoredTagsList;
    CustomButton closeButton;
    IconSelectorWidget iconSelectorWidget;

    int editBoxConfirmationElapsed;
    boolean changedNameSuccessfully;
    int stateButtonElapsed;

    @Override
    protected void init() {
        super.init();

        recalculate(height, font);

        editBoxConfirmationElapsed = editBoxConfirmationDuration + 1;
        changedNameSuccessfully = false;
        stateButtonElapsed = ProfileManager.isProfileActive(profileName) ? stateButtonDuration : 0;

// ICON SELECTOR BUTTON
        iconSelectorButton = new CustomButton(
                b -> {
                    iconSelectorWidget = new IconSelectorWidget(
                            () -> removeWidget(iconSelectorWidget),
                            profileName
                    );
                    addRenderableWidget(iconSelectorWidget);
                },
                spacedX,
                spacedY,
                cornerButtonsSize,
                cornerButtonsSize,
                primaryColor
        );
        addRenderableWidget(iconSelectorButton);

// NAME EDIT BOX
        nameEditBox = new CustomEditBox(
                16,
                profileName,
                spacedX + cornerButtonsSize + spaceBetweenButtons,
                spacedY,
                width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2,
                customHeight,
                primaryColor
        );
        addRenderableWidget(nameEditBox);

// STATE BUTTON
        stateButton = new CustomButton(
                b -> ProfileManager.toggleProfileState(profileName),
                width - spacedX - cornerButtonsSize,
                spacedY,
                cornerButtonsSize,
                cornerButtonsSize,
                primaryColor
        );
        addRenderableWidget(stateButton);

// ITEMS TAB
        itemsTabButton = new CustomButton(
                b -> {
                    if (renderables.contains(ignoredTagsList)) {
                        this.addRenderableWidget(ignoredItemsList);
                        this.removeWidget(ignoredTagsList);
                    }
                },
                "Items",
                true,
                spacedX + cornerButtonsSize + spaceBetweenButtons * 2,
                spacedY + customHeight + spaceBetweenButtons,
                (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 - spaceBetweenButtons * 2,
                customHeight,
                secondaryColor
        );
        addRenderableWidget(itemsTabButton);

// TAGS TAB
        tagsTabButton = new CustomButton(
                b -> {
                    if (renderables.contains(ignoredItemsList)) {
                        this.addRenderableWidget(ignoredTagsList);
                        this.removeWidget(ignoredItemsList);
                    }
                },
                "Tags",
                true,
                spacedX + cornerButtonsSize + spaceBetweenButtons + (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 + spaceBetweenButtons,
                spacedY + customHeight + spaceBetweenButtons,
                (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 - spaceBetweenButtons * 2,
                customHeight,
                tertiaryColor
        );
        addRenderableWidget(tagsTabButton);

// AVAILABLE ITEMS LIST
        availableItemsList = new AvailableItemsList(
                profileName
        );

// LIST OF ITEMS
        ignoredItemsList = new IgnoredItemsList(
                this,
                profileName,
                spacedX + customHeight + spaceBetweenButtons * 3,
                spacedY + cornerButtonsSize + spaceBetweenButtons * 3,
                width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2 - spaceBetweenButtons * 4,
                height - spacedY * 2 - spaceBetweenButtons * 2 - cornerButtonsSize - customHeight - spaceBetweenButtons * 4,
                iconSize + spaceBetweenButtons * 2
        );
        addRenderableWidget(ignoredItemsList);

// LIST OF TAGS
        ignoredTagsList = new IgnoredTagsList(
                profileName,
                spacedX + customHeight + spaceBetweenButtons * 3,
                spacedY + cornerButtonsSize + spaceBetweenButtons * 3,
                width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2 - spaceBetweenButtons * 4,
                height - spacedY * 2 - spaceBetweenButtons * 2 - cornerButtonsSize - customHeight - spaceBetweenButtons * 4,
                customHeight
        );

// CLOSE BUTTON
        closeButton = new CustomButton(
                b -> onClose(),
                "Aceptar",
                true,
                spacedX,
                secondLineStartInY,
                width - spacedX * 2,
                customHeight,
                primaryColor
        );
        addRenderableWidget(closeButton);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        recalculate(height, font);
        int stateBoxWidth = cornerButtonsSize / 2;
        int stateBoxHeight = cornerButtonsSize / 3;
        int stateBoxX = (width - spacedX - cornerButtonsSize) + cornerButtonsSize / 2 - stateBoxWidth / 2;
        int stateBoxY = spacedY + cornerButtonsSize / 2 - stateBoxHeight / 2;
        float stateProgress = easeInOutCubic((float) stateButtonElapsed / stateButtonDuration);
        String newName = nameEditBox.getValue().trim();

// ICON SELECTOR BUTTON
        iconSelectorButton.setPosition(
                spacedX,
                spacedY
        );
        iconSelectorButton.setSize(
                cornerButtonsSize,
                cornerButtonsSize
        );
        iconSelectorButton.setBackgroundColor(primaryColor);
        g.pose().pushPose();
        g.pose().translate(
                spacedX + spacedText,
                spacedY + spacedText,
                0
        );
        g.pose().scale(
                (cornerButtonsSize - spacedText * 2) / 16f,
                (cornerButtonsSize - spacedText * 2) / 16f,
                1.0f
        );
        g.renderItem(
                new ItemStack(ProfileManager.getProfile(profileName).getIconItem()),
                0,
                0
        );
        g.pose().popPose();

// NAME EDIT BOX
        nameEditBox.setPosition(
                spacedX + cornerButtonsSize + spaceBetweenButtons,
                spacedY
        );
        nameEditBox.setSize(
                width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2,
                customHeight
        );
        nameEditBox.setBackgroundColor(primaryColor);
        if (!nameEditBox.isFocused() && ProfileManager.getAllProfiles().containsKey(newName) && !newName.equals(profileName)) {
            editBoxConfirmationElapsed = 0;
        }
        if (editBoxConfirmationElapsed <= editBoxConfirmationDuration) {
            float errorProgress = easeInOutCubic((float) editBoxConfirmationElapsed / editBoxConfirmationDuration);
            int errorAlpha = Math.max(5, (int) ((1f - errorProgress) * 255));
            int color = changedNameSuccessfully ? goodMeaningColor : badMeaningColor;
            g.drawString(font,
                    newName,
                    nameEditBox.getX() + spacedText,
                    nameEditBox.getY() + spacedText,
                    (errorAlpha << 24) | (color & 0x00FFFFFF)
            );
        }

// STATE BUTTON
        stateButton.setPosition(
                width - spacedX - cornerButtonsSize,
                spacedY
        );
        stateButton.setSize(
                cornerButtonsSize,
                cornerButtonsSize
        );
        stateButton.setBackgroundColor(primaryColor);
        g.renderOutline(
                stateBoxX,
                stateBoxY,
                stateBoxWidth,
                stateBoxHeight,
                linesColor | (0xFF << 24)
        );
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

// ITEMS TAB
        itemsTabButton.setPosition(
                spacedX + cornerButtonsSize + spaceBetweenButtons * 2,
                spacedY + customHeight + spaceBetweenButtons
        );
        itemsTabButton.setSize(
                (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 - spaceBetweenButtons * 2,
                customHeight
        );
        itemsTabButton.setBackgroundColor(renderables.contains(ignoredItemsList) ? secondaryColor : tertiaryColor);
        itemsTabButton.setTextUsedColor(renderables.contains(ignoredItemsList) ? linesColor : linesColor | (0x99 << 24));
        itemsTabButton.setHoverable(!renderables.contains(ignoredItemsList));

// TAGS TAB
        tagsTabButton.setPosition(
                spacedX + cornerButtonsSize + spaceBetweenButtons + (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 + spaceBetweenButtons,
                spacedY + customHeight + spaceBetweenButtons
        );
        tagsTabButton.setSize(
                (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 - spaceBetweenButtons * 2,
                customHeight
        );
        tagsTabButton.setBackgroundColor(renderables.contains(ignoredTagsList) ? secondaryColor : tertiaryColor);
        tagsTabButton.setTextUsedColor(renderables.contains(ignoredTagsList) ? linesColor : linesColor | (0x99 << 24));
        tagsTabButton.setHoverable(!renderables.contains(ignoredTagsList));

// LIST BORDER
        renderCustomOutline(g,
                spacedX + customHeight + spaceBetweenButtons,
                spacedY + cornerButtonsSize + spaceBetweenButtons,
                width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2,
                height - spacedY * 2 - spaceBetweenButtons * 2 - cornerButtonsSize - customHeight,
                true,
                secondaryColor
        );

// LIST OF ITEMS
        if (renderables.contains(ignoredItemsList)) {
            ignoredItemsList.setPosition(
                    spacedX + customHeight + spaceBetweenButtons * 3,
                    spacedY + cornerButtonsSize + spaceBetweenButtons * 3
            );
            ignoredItemsList.setSize(
                    width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2 - spaceBetweenButtons * 4,
                    height - spacedY * 2 - spaceBetweenButtons * 2 - cornerButtonsSize - customHeight - spaceBetweenButtons * 4
            );
            ignoredItemsList.setItemHeight(iconSize + spaceBetweenButtons * 2);
        }

// LIST OF TAGS
        if (renderables.contains(ignoredTagsList)) {
            ignoredTagsList.setPosition(
                    spacedX + customHeight + spaceBetweenButtons * 3,
                    spacedY + cornerButtonsSize + spaceBetweenButtons * 3
            );
            ignoredTagsList.setSize(
                    width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2 - spaceBetweenButtons * 4,
                    height - spacedY * 2 - spaceBetweenButtons * 2 - cornerButtonsSize - customHeight - spaceBetweenButtons * 4
            );
            ignoredTagsList.setItemHeight(iconSize + spaceBetweenButtons * 2);
        }

// AVAILABLE ITEMS LIST
        if (renderables.contains(availableItemsList)) {

            availableItemsList.setPosition(
                    addItemEditBox.getX() + spaceBetweenButtons * 2,
                    addItemEditBox.getBottom() + spaceBetweenButtons
            );
            availableItemsList.setSize(
                    addItemEditBox.getWidth() - spaceBetweenButtons * 4,
                    150 - spaceBetweenButtons * 3
            );
            availableItemsList.setItemHeight(iconSize);
            availableItemsList.setScissorBottom(Math.min(ignoredItemsList.getBottom(), availableItemsList.getBottom()));

            ignoredItemsList.addToMaxPosition(150);

            g.enableScissor(
                    ignoredItemsList.getX(),
                    ignoredItemsList.getY(),
                    ignoredItemsList.getRight(),
                    ignoredItemsList.getBottom()
            );
            renderCustomOutline(g,
                    addItemEditBox.getX(),
                    addItemEditBox.getBottom(),
                    addItemEditBox.getWidth(),
                    150,
                    false,
                    secondaryColor
            );
            g.disableScissor();

        } else {
            ignoredItemsList.addToMaxPosition(0);
        }

// CLOSE BUTTON
        closeButton.setPosition(
                spacedX,
                secondLineStartInY
        );
        closeButton.setSize(
                width - spacedX * 2,
                customHeight
        );
        closeButton.setBackgroundColor(primaryColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (!nameEditBox.isMouseOver(mouseX, mouseY)) tryToChangeProfileName();

        if (renderables.contains(availableItemsList)
                && (mouseX < ignoredItemsList.getX() - spaceBetweenButtons * 2
                || mouseX > ignoredItemsList.getRight() + spaceBetweenButtons * 2
                || mouseY < ignoredItemsList.getY() - spaceBetweenButtons * 2
                || mouseY > ignoredItemsList.getBottom() + spaceBetweenButtons * 2)
        ) {
            removeWidget(availableItemsList);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == GLFW_KEY_ENTER || keyCode == GLFW_KEY_KP_ENTER && nameEditBox.isFocused()) tryToChangeProfileName();

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        if (mouseY > ignoredItemsList.getBottom() + spaceBetweenButtons * 2) return false;

        if (renderables.contains(availableItemsList)
                && mouseX > availableItemsList.getX() - spaceBetweenButtons * 2
                && mouseX < availableItemsList.getRight() + spaceBetweenButtons * 2
                && mouseY > availableItemsList.getY() - spaceBetweenButtons - customHeight
                && mouseY < ignoredItemsList.getBottom()
        ) {
            availableItemsList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            return true;
        }

        if (mouseX > ignoredItemsList.getX() - spaceBetweenButtons * 2
                && mouseX < ignoredItemsList.getRight() + spaceBetweenButtons * 2
                && mouseY > ignoredItemsList.getY() - spaceBetweenButtons * 2
                && mouseY < ignoredItemsList.getBottom() + spaceBetweenButtons * 2
        ) {
            ignoredItemsList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void tick() {

        ignoredItemsList.tick();

        if (editBoxConfirmationElapsed <= editBoxConfirmationDuration) {
            editBoxConfirmationElapsed++;
        }
        if (editBoxConfirmationElapsed == editBoxConfirmationDuration + 1) {
            changedNameSuccessfully = false;
        }

        boolean isActive = ProfileManager.isProfileActive(profileName);
        if (isActive && stateButtonElapsed < stateButtonDuration) {
            stateButtonElapsed++;
        } else if (!isActive && stateButtonElapsed > 0) {
            stateButtonElapsed--;
        }
    }

    public void tryToChangeProfileName() {

        String newName = nameEditBox.getValue().trim();
        if (newName.equals(profileName)) {
            nameEditBox.setFocused(false);
        } else if (newName.isEmpty()) {
            nameEditBox.setValue(profileName);
            nameEditBox.setFocused(false);
        } else if (ProfileManager.getAllProfiles().containsKey(newName)) {
            editBoxConfirmationElapsed = 0;
        } else {
            ProfileManager.renameProfile(profileName, newName);
            nameEditBox.setFocused(false);
            profileName = newName;
            ignoredItemsList.changeProfileName(newName);
            editBoxConfirmationElapsed = 0;
            changedNameSuccessfully = true;
        }
    }

    public void addAvailableItemsList() {
        if (!renderables.contains(availableItemsList)) addRenderableWidget(availableItemsList);
    }
}