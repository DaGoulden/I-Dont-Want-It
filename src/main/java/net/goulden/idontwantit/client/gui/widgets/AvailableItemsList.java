package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.widgets.custom.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomContainerList;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.goulden.idontwantit.util.GUIVariables.*;

public class AvailableItemsList extends CustomContainerList<AvailableItemsList.ItemEntry> {

    public String profileName;
    public static String currentFilter = "";

    public AvailableItemsList(String profileName, int x, int y, int w, int h, int itemHeight) {
        super(x, y, w, h, itemHeight);
        this.profileName = profileName;
        refreshList();
    }

    public AvailableItemsList(String profileName) {
        this.profileName = profileName;
        refreshList();
    }

    class ItemEntry extends CustomContainerList.Entry<AvailableItemsList.ItemEntry> {

        CustomButton addButton;

        Item item;

        ItemEntry(Item item) {
            this.item = item;

// ADD ITEM BUTTON
            addButton = new CustomButton(
                    b -> {
                        ProfileManager.addIgnoredItem(profileName, item);
                        refreshList();
                    }
            );
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

// ITEM BACKGROUND
            g.fill(left,
                    top,
                    left + width - height - spaceBetweenButtons,
                    top + height,
                    tertiaryColor
            );

// ITEM ICON
            g.renderItem(new ItemStack(item),
                    left,
                    top
            );

// ITEM NAME
            g.drawString(
                    font,
                    item.getDescription().getString(),
                    left + spaceBetweenButtons + iconSize,
                    top + (height - fontHeight) / 2,
                    linesColor
            );

// ADD ITEM BUTTON
            addButton.setPosition(
                    left + width - height,
                    top
            );
            addButton.setSize(
                    height,
                    height
            );
            addButton.setBackgroundColor(secondaryColor);
            addButton.render(g, mouseX, mouseY, partialTick);
            g.drawCenteredString(font,
                    "+",
                    addButton.getX() + addButton.getWidth() / 2,
                    addButton.getY() + (addButton.getHeight() - fontHeight) / 2,
                    linesColor
            );
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(addButton);
        }
    }

    public void refreshList() {
        clearEntries();

        if (ProfileManager.getProfile(profileName) == null) return;

        List<Item> availableItems = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;

            if (ProfileManager.isItemIgnoredInProfile(profileName, item)) continue;

            if (currentFilter.isEmpty()
                    || item.getDescription().getString().toLowerCase().contains(currentFilter)
                    || BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase().contains(currentFilter)
            ) {
                availableItems.add(item);
            }
        }

        availableItems.sort(Comparator.comparing(a -> a.getDescription().getString()));

        for (Item item : availableItems) addEntry(new AvailableItemsList.ItemEntry(item));
    }
}