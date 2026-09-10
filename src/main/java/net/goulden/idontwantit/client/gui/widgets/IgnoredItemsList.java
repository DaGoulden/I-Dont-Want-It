package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.EditProfileScreen;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomContainerList;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomEditBox;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.goulden.idontwantit.client.gui.EditProfileScreen.availableItemsList;
import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.drawCutString;
import static net.goulden.idontwantit.util.RenderUtils.easeInOutQuart;

public class IgnoredItemsList extends CustomContainerList<IgnoredItemsList.EntryBase> {

    String profileName;
    EditProfileScreen screen;
    public boolean shouldAvailableListBeOpen;
    int openListElapsed;
    public float openListProgress;

    public IgnoredItemsList(EditProfileScreen screen, String profileName, int x, int y, int w, int h, int itemHeight) {
        super(x, y, w, h, itemHeight);
        this.profileName = profileName;
        this.screen = screen;
        refreshList();
    }

    abstract static class EntryBase extends CustomContainerList.Entry<IgnoredItemsList.EntryBase> { }

    class IgnoredItemEntry extends IgnoredItemsList.EntryBase {

        CustomButton removeItemButton;

        Item item;

        IgnoredItemEntry(Item item) {

            this.item = item;

// REMOVE ITEM BUTTON
            removeItemButton = new CustomButton(
                    b -> {
                        ProfileManager.removeIgnoredItem(profileName, item);
                        refreshList();
                    },
                    2
            );
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, float partialTick) {

// ITEM BACKGROUND
            int backgroundWidth = width - height - spaceBetweenButtons;
            g.fill(left,
                    top,
                    left + backgroundWidth,
                    top + height,
                    tertiaryColor
            );

// ITEM ICON
            g.renderItem(new ItemStack(item),
                    left + spacedText,
                    top + spacedText
            );

// ITEM NAME
            drawCutString(g,
                    mouseX,
                    mouseY,
                    item.getDescription().getString(),
                    backgroundWidth - (spacedText * 3 + iconSize),
                    left + spacedText * 2 + iconSize,
                    top + (height - fontHeight) / 2,
                    linesColor
            );

// REMOVE ITEM BUTTON
            removeItemButton.setPosition(
                    left + width - height,
                    top
            );
            removeItemButton.setSize(
                    height,
                    height
            );
            removeItemButton.render(g, mouseX, mouseY, partialTick);
            g.drawCenteredString(font,
                    "x",
                    removeItemButton.getX() + removeItemButton.getWidth() / 2,
                    removeItemButton.getY() + (removeItemButton.getHeight() - fontHeight) / 2,
                    linesColor
            );
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(removeItemButton);
        }
    }

    public class AddIgnoredItemEntry extends IgnoredItemsList.EntryBase {

        public static CustomEditBox addItemEditBox;
        static CustomButton openAvailableItemsListButton;

        public AddIgnoredItemEntry() {

// OPEN AVAILABLE ITEMS LIST BUTTON
            openAvailableItemsListButton = new CustomButton(
                    b -> {
                        shouldAvailableListBeOpen = true;
                        addItemEditBox.setValue("");
                    },
                    2
            );

// ADD ITEM EDIT BOX
            addItemEditBox = new CustomEditBox(
                    32,
                    text -> {
                        AvailableItemsList.currentFilter = text.toLowerCase();
                        availableItemsList.refreshList();
                    },
                    2
            );
        }

        @Override
        public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, float partialTick) {

            openListProgress =
                    !(openListAnimationDuration == 0)
                            ? easeInOutQuart((float) openListElapsed / openListAnimationDuration)
                            : shouldAvailableListBeOpen ? 1 : 0;

            if (!shouldAvailableListBeOpen && openListProgress == 0) {
// OPEN AVAILABLE ITEMS LIST BUTTON
                openAvailableItemsListButton.setPosition(
                        left,
                        top
                );
                openAvailableItemsListButton.setSize(
                        width,
                        height
                );
                openAvailableItemsListButton.render(g, mouseX, mouseY, partialTick);
                openAvailableItemsListButton.active = true;
                addItemEditBox.active = false;

            } else {

// ADD ITEM EDIT BOX
                addItemEditBox.setPosition(
                        left,
                        top
                );
                addItemEditBox.setSize(
                        width,
                        (int) (height - (height - customHeight) * openListProgress)
                );
                addItemEditBox.render(g, mouseX, mouseY, partialTick);
                addItemEditBox.active = true;
                openAvailableItemsListButton.active = false;
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(openAvailableItemsListButton, addItemEditBox);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean handled = super.mouseClicked(mouseX, mouseY, button);
            if (screen.renderables.contains(availableItemsList)) this.setFocused(addItemEditBox);
            return handled;
        }
    }

    @Override
    public void tick() {

        if (shouldAvailableListBeOpen && openListElapsed < openListAnimationDuration) {
            openListElapsed++;
        } else if (!shouldAvailableListBeOpen && openListElapsed > 0) {
            openListElapsed--;
        } else if (openListElapsed > openListAnimationDuration) {
            openListElapsed = 0;
        }

        super.tick();

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

        for (Item item : items) addEntry(new IgnoredItemEntry(item));
        addEntry(new AddIgnoredItemEntry());
    }

    public void changeProfileName(String newName) {
        profileName = newName;
        availableItemsList.profileName = newName;
    }
}
