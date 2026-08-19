package net.goulden.idontwantit.client.gui;

import net.goulden.idontwantit.client.gui.widgets.*;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.*;

public class EditProfileScreen extends Screen {

    private final Screen parent;
    private String profileName;

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
        minecraft.setScreen(parent);
    }

    private CustomButton iconSelectorButton;
    private CustomEditBox nameEditBox;
    private CustomButton stateButton;
    private CustomButton itemTabButton;
    private CustomButton tagTabButton;
    ItemListWidget itemListWidget;
    TagListWidget tagListWidget;
    private CustomButton closeButton;

    private IconSelectorWidget iconSelectorWidget;
    private AddItemWidget addItemWidget;

    int editBoxErrorElapsed;
    int stateButtonElapsed;

    private boolean isItemListTabOpen = true;
    private boolean isTagListTabOpen = false;

    @Override
    protected void init() {
        super.init();

        recalculate(width, height, font.lineHeight);

        editBoxErrorElapsed = editBoxErrorDuration + 1;
        stateButtonElapsed = ProfileManager.isProfileActive(profileName) ? stateButtonDuration : 0;

// ICON SELECTOR BUTTON
        iconSelectorButton = new CustomButton(
                b -> iconSelectorWidget = new IconSelectorWidget(
                        () -> iconSelectorWidget = null,
                        profileName)
        );
        addRenderableWidget(iconSelectorButton);

// NAME EDIT BOX
        nameEditBox = new CustomEditBox(
                font,
                16,
                profileName
        );
        addRenderableWidget(nameEditBox);

// STATE BUTTON
        stateButton = new CustomButton(
                b -> ProfileManager.toggleProfileState(profileName)
        );
        addRenderableWidget(stateButton);

// ITEMS TAB
        itemTabButton = new CustomButton(
                b -> {
                    if (isTagListTabOpen) {
                        isTagListTabOpen = false;
                        isItemListTabOpen = true;
                        this.removeWidget(tagListWidget);
                        this.addRenderableWidget(itemListWidget);
                    }
                },
                "Items",
                true
        );
        addRenderableWidget(itemTabButton);

// TAGS TAB
        tagTabButton = new CustomButton(
                b -> {
                    if (isItemListTabOpen) {
                        isItemListTabOpen = false;
                        isTagListTabOpen = true;
                        this.removeWidget(itemListWidget);
                        this.addRenderableWidget(tagListWidget);
                    }
                },
                "Tags",
                true
        );
        addRenderableWidget(tagTabButton);

// LIST OF ITEMS
        itemListWidget = new ItemListWidget(minecraft);
        addRenderableWidget(itemListWidget);

// LIST OF TAGS
        tagListWidget = new TagListWidget(minecraft);

// CLOSE BUTTON
        closeButton = new CustomButton(
                b -> onClose(),
                "Aceptar",
                true
        );
        addRenderableWidget(closeButton);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        recalculate(width, height, font.lineHeight);

// ICON SELECTOR BUTTON
        iconSelectorButton.setPosition(
                spacedX,
                spacedY
        );
        iconSelectorButton.setSize(
                cornerButtonsSize,
                cornerButtonsSize
        );
        iconSelectorButton.setUsedColor(primaryColor);
        g.pose().pushPose();
        g.pose().translate(
                iconSelectorButton.getX() + spacedText,
                iconSelectorButton.getY() + spacedText, 0
        );
        g.pose().scale(
                (cornerButtonsSize - spacedText * 2) / 16f,
                (cornerButtonsSize - spacedText * 2) / 16f, 1.0f
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
        nameEditBox.setUsedColor(primaryColor);
        String newName = nameEditBox.getValue().trim();
        if (!nameEditBox.isFocused() && ProfileManager.getAllProfiles().containsKey(newName) && !newName.equals(profileName)) {
            editBoxErrorElapsed = 0;
        }
        if (editBoxErrorElapsed <= editBoxErrorDuration) {
            float errorProgress = easeInOutCubic((float) editBoxErrorElapsed / editBoxErrorDuration);
            int errorAlpha = Math.max(5, (int) ((1f - errorProgress) * 255));
            g.drawString(font,
                    newName,
                    nameEditBox.getX() + spacedText,
                    nameEditBox.getY() + spacedText,
                    (errorAlpha << 24) | (badMeaningColor & 0x00FFFFFF)
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
        stateButton.setUsedColor(primaryColor);
        int stateBoxWidth = stateButton.getWidth() / 2;
        int stateBoxHeight = stateButton.getHeight() / 3;
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
                stateBoxX + ((stateBoxWidth - stateBoxHeight) * stateProgress), stateBoxY,
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
        itemTabButton.setPosition(
                spacedX + cornerButtonsSize + spaceBetweenButtons * 2,
                spacedY + customHeight + spaceBetweenButtons
        );
        itemTabButton.setSize(
                (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 - spaceBetweenButtons * 2,
                customHeight
        );
        itemTabButton.setUsedColor(isItemListTabOpen ? secondaryColor : tertiaryColor);
        itemTabButton.setTextUsedColor(isItemListTabOpen ? linesColor : linesColor | (0x99 << 24));
        itemTabButton.setHoverable(!isItemListTabOpen);

// TAGS TAB
        tagTabButton.setPosition(
                spacedX + cornerButtonsSize + spaceBetweenButtons + (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 + spaceBetweenButtons,
                spacedY + customHeight + spaceBetweenButtons
        );
        tagTabButton.setSize(
                (width - spacedX * 2 - cornerButtonsSize * 2 - spaceBetweenButtons * 2) / 2 - spaceBetweenButtons * 2,
                customHeight
        );
        tagTabButton.setUsedColor(isTagListTabOpen ? secondaryColor : tertiaryColor);
        tagTabButton.setTextUsedColor(isTagListTabOpen ? linesColor : linesColor | (0x99 << 24));
        tagTabButton.setHoverable(!isTagListTabOpen);

// LIST BORDER
        renderCustomOutline(g,
                spacedX + customHeight + spaceBetweenButtons,
                spacedY + cornerButtonsSize + spaceBetweenButtons,
                width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2,
                height - spacedY * 2 - spaceBetweenButtons * 2 - cornerButtonsSize - customHeight,
                secondaryColor);

// LIST OF ITEMS
        itemListWidget.setPosition(
                spacedX + customHeight + spaceBetweenButtons * 3,
                spacedY + cornerButtonsSize + spaceBetweenButtons * 3
        );
        itemListWidget.setSize(
                width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2 - spaceBetweenButtons * 4,
                height - spacedY * 2 - spaceBetweenButtons * 2 - cornerButtonsSize - customHeight - spaceBetweenButtons * 4
        );
        itemListWidget.setItemHeight(iconSize + spaceBetweenButtons * 2);

// LIST OF TAGS
        tagListWidget.setPosition(
                spacedX + customHeight + spaceBetweenButtons * 3,
                spacedY + cornerButtonsSize + spaceBetweenButtons * 3
        );
        tagListWidget.setSize(
                width - spacedX * 2 - (customHeight + spaceBetweenButtons) * 2 - spaceBetweenButtons * 4,
                height - spacedY * 2 - spaceBetweenButtons * 2 - cornerButtonsSize - customHeight - spaceBetweenButtons * 4
        );
        tagListWidget.setItemHeight(iconSize + spaceBetweenButtons * 2);

// CLOSE BUTTON
        closeButton.setPosition(
                spacedX,
                secondLineStartInY
        );
        closeButton.setSize(
                width - spacedX * 2,
                customHeight
        );
        closeButton.setUsedColor(primaryColor);

        if (iconSelectorWidget != null) iconSelectorWidget.render(g, mouseX, mouseY, partialTick);
        if (addItemWidget != null) addItemWidget.render(g, mouseX, mouseY, partialTick);
    }

    public void tick() {

        if (editBoxErrorElapsed <= editBoxErrorDuration) {
            editBoxErrorElapsed++;
        }

        boolean isActive = ProfileManager.isProfileActive(profileName);
        if (isActive && stateButtonElapsed < stateButtonDuration) {
            stateButtonElapsed++;
        } else if (!isActive && stateButtonElapsed > 0) {
            stateButtonElapsed--;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (iconSelectorWidget != null) return iconSelectorWidget.mouseClicked(mouseX, mouseY, button);
        if (addItemWidget != null) return addItemWidget.mouseClicked(mouseX, mouseY, button);

        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (!nameEditBox.isMouseOver(mouseX, mouseY)) {
            tryToChangeName();
        }
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (iconSelectorWidget != null) return iconSelectorWidget.keyPressed(keyCode, scanCode, modifiers);
        if (addItemWidget != null) return addItemWidget.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == 257 || keyCode == 335 && nameEditBox.isFocused()) {
            tryToChangeName();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {

        if (iconSelectorWidget != null) return iconSelectorWidget.charTyped(codePoint, modifiers);
        if (addItemWidget != null) return addItemWidget.charTyped(codePoint, modifiers);

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        if (iconSelectorWidget != null) return iconSelectorWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (addItemWidget != null) return addItemWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void tryToChangeName() {

        String newName = nameEditBox.getValue().trim();
        if (newName.equals(profileName)) {
            nameEditBox.setFocused(false);
        } else if (newName.isEmpty()) {
            nameEditBox.setValue(profileName);
            nameEditBox.setFocused(false);
        } else if (ProfileManager.getAllProfiles().containsKey(newName)) {
            editBoxErrorElapsed = 0;
        } else {
            ProfileManager.renameProfile(profileName, newName);
            nameEditBox.setFocused(false);
            profileName = newName;
        }
    }

// - - - ITEM LIST - - -
    public class ItemListWidget extends CustomContainerList<ItemListWidget.EntryBase> {

        public ItemListWidget(Minecraft mc) {
            super(mc);
            refreshList();
        }

        abstract static class EntryBase extends CustomContainerList.Entry<EditProfileScreen.ItemListWidget.EntryBase> { }

        class ItemEntry extends EditProfileScreen.ItemListWidget.EntryBase {

            private final CustomButton removeItemButton;

            private final Item item;
            private final ItemStack stack;

            ItemEntry(Item item) {

                this.item = item;
                this.stack = new ItemStack(item);

// REMOVE ITEM BUTTON
                removeItemButton = new CustomButton(
                        b -> {
                            ProfileManager.removeIgnoredItem(profileName, item);
                            refreshList();
                        },
                        "x",
                        true
                );
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

// REMOVE ITEM BUTTON
                removeItemButton.setPosition(
                        left + width - height + spaceBetweenButtons,
                        top + spaceBetweenButtons
                );
                removeItemButton.setSize(
                        height - spaceBetweenButtons * 2,
                        height - spaceBetweenButtons * 2
                );
                removeItemButton.setUsedColor(secondaryColor);
                removeItemButton.render(g, mouseX, mouseY, partialTick);
                g.fill(left,
                        top,
                        left + width,
                        top + height,
                        secondaryColor
                );
                g.renderItem(stack,
                        left + spaceBetweenButtons,
                        top + spaceBetweenButtons
                );
                g.drawString(
                        minecraft.font,
                        item.getDescription().getString(),
                        left + spaceBetweenButtons * 2 + iconSize,
                        top + (height - font.lineHeight) / 2,
                        0xFFFFFF
                );
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() {
                return List.of(removeItemButton);
            }

            @Override
            public @NotNull List<? extends NarratableEntry> narratables() {
                return List.of(removeItemButton);
            }
        }

        class AddItemEntry extends EditProfileScreen.ItemListWidget.EntryBase {

            private final CustomButton addItemButton;

            public AddItemEntry() {

// ADD ITEM BUTTON
                addItemButton = new CustomButton(
                        b -> {
                            addItemWidget = new AddItemWidget(
                                    () -> addItemWidget = null,
                                    profileName
                            );
                            refreshList();
                        }
                );
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

// ADD ITEM BUTTON
                addItemButton.setPosition(
                        left,
                        top
                );
                addItemButton.setSize(
                        width,
                        height
                );
                addItemButton.setUsedColor(secondaryColor);
                addItemButton.render(g, mouseX, mouseY, partialTick);
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() {
                return List.of(addItemButton);
            }

            @Override
            public @NotNull List<? extends NarratableEntry> narratables() {
                return List.of(addItemButton);
            }
        }

        public void refreshList() {

            clearEntries();

            ProfileManager.ItemProfile profile = ProfileManager.getProfile(profileName);

            if (profile == null) return;

            List<Item> items = new ArrayList<>();

            for (String idString : profile.ignoredItems) {
                ResourceLocation id = ResourceLocation.tryParse(idString);
                if (id == null) continue;
                items.add(BuiltInRegistries.ITEM.get(id));
            }

            items.sort(Comparator.comparing(a -> a.getDescription().getString()));

            for (Item item : items) addEntry(new EditProfileScreen.ItemListWidget.ItemEntry(item));
            addEntry(new AddItemEntry());
        }
    }

    // - - - TAG LIST - - -
    public class TagListWidget extends CustomContainerList<TagListWidget.EntryBase> {

        public TagListWidget(Minecraft mc) {
            super(mc);
            refreshList();
        }

        abstract static class EntryBase extends CustomContainerList.Entry<EditProfileScreen.TagListWidget.EntryBase> { }

        class TagEntry extends EditProfileScreen.TagListWidget.EntryBase {

            private final CustomButton removeTagButton;

            private final TagKey<Item> tag;

            TagEntry(TagKey<Item> tag) {

                this.tag = tag;

// REMOVE TAG BUTTON
                removeTagButton = new CustomButton(
                        b -> {
                            ProfileManager.removeIgnoredTag(profileName, tag);
                            refreshList();
                        },
                        "x",
                        true
                );
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

// REMOVE TAG BUTTON
                removeTagButton.setPosition(
                        left + width - height + spaceBetweenButtons,
                        top + spaceBetweenButtons
                );
                removeTagButton.setSize(
                        height - spaceBetweenButtons * 2,
                        height - spaceBetweenButtons * 2
                );
                removeTagButton.setUsedColor(secondaryColor);
                removeTagButton.render(g, mouseX, mouseY, partialTick);
                g.fill(left,
                        top,
                        left + width,
                        top + height,
                        secondaryColor
                );
                g.drawString(
                        minecraft.font,
                        tag.toString(),
                        left + spaceBetweenButtons * 2 + iconSize,
                        top + (height - font.lineHeight) / 2,
                        0xFFFFFF
                );
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() {
                return List.of(removeTagButton);
            }

            @Override
            public @NotNull List<? extends NarratableEntry> narratables() {
                return List.of(removeTagButton);
            }
        }

        class AddTagEntry extends EditProfileScreen.TagListWidget.EntryBase {

            private final CustomButton addTagButton;

            public AddTagEntry() {

// ADD TAG BUTTON
                addTagButton = new CustomButton(
                        b -> {
                            refreshList();
                        }
                );
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

// ADD TAG BUTTON
                addTagButton.setPosition(
                        left,
                        top
                );
                addTagButton.setSize(
                        width,
                        height
                );
                addTagButton.setUsedColor(secondaryColor);
                addTagButton.render(g, mouseX, mouseY, partialTick);
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() {
                return List.of(addTagButton);
            }

            @Override
            public @NotNull List<? extends NarratableEntry> narratables() {
                return List.of(addTagButton);
            }
        }

        public void refreshList() {
            clearEntries();

            ProfileManager.ItemProfile profile = ProfileManager.getProfile(profileName);

            if (profile == null) return;

            List<TagKey<Item>> tags = new ArrayList<>();

            for (String idString : profile.ignoredTags) {
                ResourceLocation id = ResourceLocation.tryParse(idString);
                if (id == null) continue;
                tags.add(TagKey.create(Registries.ITEM, id));
            }

            tags.sort(Comparator.comparing(tag -> tag.location().toString()));

            for (TagKey<Item> tag : tags) addEntry(new TagEntry(tag));
            addEntry(new AddTagEntry());
        }
    }
}