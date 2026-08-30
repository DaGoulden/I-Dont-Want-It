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

public class IgnoredItemsList extends CustomContainerList<IgnoredItemsList.EntryBase> {

    public String profileName;
    EditProfileScreen screen;

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
                    }
            );
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

// ITEM BACKGROUND
            g.fill(left,
                    top,
                    left + width - height - spaceBetweenButtons,
                    top + height,
                    tertiaryColor
            );

// ITEM ICON
            g.renderItem(new ItemStack(item),
                    left + spaceBetweenButtons,
                    top + spaceBetweenButtons
            );

// ITEM NAME
            g.drawString(
                    font,
                    item.getDescription().getString(),
                    left + spaceBetweenButtons * 2 + iconSize,
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
            removeItemButton.setBackgroundColor(secondaryColor);
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
                        screen.addAvailableItemsList();
                        setFocused(null);
                        addItemEditBox.setFocused(true);
                    }
            );

// ADD ITEM EDIT BOX
            addItemEditBox = new CustomEditBox(
                    32,
                    text -> {
                        AvailableItemsList.currentFilter = text.toLowerCase();
                        availableItemsList.refreshList();
                    }
            );
        }

        @Override
        public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hover, float partialTick) {

            if (!screen.renderables.contains(availableItemsList)) {
// OPEN AVAILABLE ITEMS LIST BUTTON
                openAvailableItemsListButton.setPosition(
                        left,
                        top
                );
                openAvailableItemsListButton.setSize(
                        width,
                        height
                );
                openAvailableItemsListButton.setBackgroundColor(secondaryColor);
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
                        customHeight
                );
                addItemEditBox.setBackgroundColor(secondaryColor);
                addItemEditBox.render(g, mouseX, mouseY, partialTick);
                addItemEditBox.active = true;
                openAvailableItemsListButton.active = false;
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(openAvailableItemsListButton, addItemEditBox);
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

        for (Item item : items) addEntry(new IgnoredItemEntry(item));
        addEntry(new AddIgnoredItemEntry());
    }

    public void tick() {

    }

    public void changeProfileName(String newName) {
        profileName = newName;
        availableItemsList.profileName = newName;
    }
}
