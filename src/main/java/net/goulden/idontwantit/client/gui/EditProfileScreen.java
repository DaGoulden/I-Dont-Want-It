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
    boolean shouldGoToBottom = true;
    int listDynamicHeight;

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
    ProfileStateButton stateButton;
    CustomButton itemsTabButton;
    CustomButton tagsTabButton;
    public static IgnoredItemsList ignoredItemsList;
    public static AvailableItemsList availableItemsList;
    public static IgnoredTagsList ignoredTagsList;
    CustomButton closeButton;
    IconSelectorWidget iconSelectorWidget;
    
    @Override
    protected void init() {
        super.init();

        recalculate(height, font);

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
                1
        );
        addRenderableWidget(iconSelectorButton);

// NAME EDIT BOX
        nameEditBox = new CustomEditBox(
                16,
                profileName,
                () -> {
                    ProfileManager.renameProfile(profileName, nameEditBox.getValue().trim());
                    this.profileName = nameEditBox.getValue().trim();
                    ignoredItemsList.changeProfileName(nameEditBox.getValue().trim());
                },
                spacedX + cornerButtonsSize + spaceBetweenButtons,
                spacedY,
                width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2,
                customHeight,
                1
        );
        addRenderableWidget(nameEditBox);

// STATE BUTTON
        stateButton = new ProfileStateButton(
                b -> ProfileManager.toggleProfileState(profileName),
                profileName,
                width - spacedX - cornerButtonsSize,
                spacedY,
                cornerButtonsSize,
                cornerButtonsSize,
                1
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
                2
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
                2
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
                iconSize + spacedText * 2
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
                1
        );
        addRenderableWidget(closeButton);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        recalculate(height, font);

// ICON SELECTOR BUTTON
        iconSelectorButton.setPosition(
                spacedX,
                spacedY
        );
        iconSelectorButton.setSize(
                cornerButtonsSize,
                cornerButtonsSize
        );
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
        String value = nameEditBox.getValue().trim();
        nameEditBox.setIsWrongValue(ProfileManager.getAllProfiles().containsKey(value) && !value.equals(profileName));

// STATE BUTTON
        stateButton.setPosition(
                width - spacedX - cornerButtonsSize,
                spacedY
        );
        stateButton.setSize(
                cornerButtonsSize,
                cornerButtonsSize
        );

// ITEMS TAB
        itemsTabButton.setPosition(
                spacedX + cornerButtonsSize + spaceBetweenButtons * 2,
                spacedY + customHeight + spaceBetweenButtons
        );
        itemsTabButton.setSize(
                (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 - spaceBetweenButtons * 2,
                customHeight
        );
        itemsTabButton.setLowOpacity(!renderables.contains(ignoredItemsList));
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
        tagsTabButton.setLowOpacity(!renderables.contains(ignoredTagsList));
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
            ignoredItemsList.setItemHeight(iconSize + spacedText * 2);
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
            ignoredTagsList.setItemHeight(iconSize + spacedText * 2);
        }

// AVAILABLE ITEMS LIST

        if (renderables.contains(availableItemsList) && ignoredItemsList.openListProgress == 0) {
            removeWidget(availableItemsList);
        } else if (!renderables.contains(availableItemsList) && ignoredItemsList.openListProgress != 0) {
            addRenderableWidget(availableItemsList);
        }

        listDynamicHeight = (int) ((ignoredItemsList.getHeight() - customHeight) * ignoredItemsList.openListProgress) - spaceBetweenButtons * 2;

        if (ignoredItemsList.openListProgress != 0) {

            availableItemsList.setScissorBottom(Math.min(
                    addItemEditBox.getBottom() + listDynamicHeight,
                    ignoredItemsList.getBottom()
            ));
            availableItemsList.setPosition(
                    addItemEditBox.getX() + spaceBetweenButtons * 2,
                    addItemEditBox.getBottom() + spaceBetweenButtons
            );
            availableItemsList.setSize(
                    addItemEditBox.getWidth() - spaceBetweenButtons * 4,
                    ignoredItemsList.getHeight() - customHeight - spaceBetweenButtons * 3
            );
            availableItemsList.setItemHeight(iconSize + spacedText / 2 * 2);
            g.enableScissor(
                    ignoredItemsList.getX(),
                    ignoredItemsList.getY(),
                    ignoredItemsList.getRight(),
                    Math.min(
                            addItemEditBox.getBottom() + listDynamicHeight + spaceBetweenButtons * 2,
                            ignoredItemsList.getBottom()
                    )
            );
            renderCustomOutline(g,
                    addItemEditBox.getX(),
                    addItemEditBox.getBottom(),
                    addItemEditBox.getWidth(),
                    ignoredItemsList.getHeight() - customHeight,
                    false,
                    secondaryColor
            );
            g.disableScissor();
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
    }

    public void tick() {

        ignoredItemsList.maxPositionAdditive(Math.max(
                -ignoredItemsList.getItemHeight() + customHeight + listDynamicHeight + spaceBetweenButtons * 2,
                0
        ));
        if (shouldGoToBottom && ignoredItemsList.shouldAvailableListBeOpen) ignoredItemsList.setScrollAmount(ignoredItemsList.getMaxScroll());
        shouldGoToBottom = ignoredItemsList.openListProgress != 1;

        stateButton.tick();
        nameEditBox.tick();

        ignoredItemsList.tick();
        availableItemsList.tick();
        ignoredTagsList.tick();
        //availableTagsList.tick();

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (!nameEditBox.isMouseOver(mouseX, mouseY) && nameEditBox.isFocused()) nameEditBox.submitValue();

        if (renderables.contains(availableItemsList)
                && !(mouseX >= ignoredItemsList.getX() - spaceBetweenButtons * 2
                && mouseX <= ignoredItemsList.getRight() + spaceBetweenButtons * 2
                && mouseY >= ignoredItemsList.getY() - spaceBetweenButtons * 2
                && mouseY <= ignoredItemsList.getBottom() + spaceBetweenButtons * 2)
        ) {
            ignoredItemsList.shouldAvailableListBeOpen = false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if ((keyCode == GLFW_KEY_ENTER || keyCode == GLFW_KEY_KP_ENTER) && nameEditBox.isFocused()) nameEditBox.submitValue();

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
}