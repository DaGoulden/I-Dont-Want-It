package net.goulden.idontwantit.client.gui;

import net.goulden.idontwantit.client.gui.widgets.AddItemWidget;
import net.goulden.idontwantit.client.gui.widgets.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.IconSelectorWidget;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EditProfileItemsScreenOld extends Screen {

    private final Screen parent;
    private String profileName;

    public EditProfileItemsScreenOld(Screen parent, String profileName) {
        super(Component.empty());
        this.parent = parent;
        this.profileName = profileName;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    private IgnoredItemListWidget itemList;
    private Button addItemsButton;
    private Button doneButton;

    private AddItemWidget addItemWidget;
    private IconSelectorWidget iconSelector;

// ================= INIT =================
    @Override
    protected void init() {

// LIST OF ITEMS
        itemList = new IgnoredItemListWidget(
                minecraft,
                width - 20,
                height - 130,
                80,
                10);
        addRenderableWidget(itemList);

// ADD ITEMS BUTTON
        addItemsButton = new CustomButton(
                width / 2 - 100,
                height - 45,
                200,
                20,
                b -> openAddItemWidget(),
                0xFF3A3A3A, 0xFF555555);
        addRenderableWidget(addItemsButton);

// DONE BUTTON
        doneButton = new CustomButton(
                width / 2 - 100, height - 20, 200, 20,
                b -> {
                    assert minecraft != null;
                    minecraft.setScreen(parent);
                },
                0xFF3A3A3A, 0xFF555555);
        addRenderableWidget(doneButton);
    }

    /* ticks */

    /* name change */



    /* icon */

    private int getIconX() {
        return width / 2 - 120;
    }

    private int getIconY() {
        return 30;
    }

    private boolean isHoveringIcon(double mouseX, double mouseY) {

        int x = getIconX();
        int y = getIconY();

        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    /* modals */

    private void openAddItemWidget() {
        //addItemWidget = new AddItemWidget(this, profileName);
    }

    public void closeAddItemWidget() {
        addItemWidget = null;

        if (itemList != null)
            itemList.refreshList();
    }

    private void openIconSelector() {
        iconSelector = new IconSelectorWidget(
                this::closeIconSelector,
                profileName,
                width / 2,
                height / 2
        );
    }

    public void closeIconSelector() {
        iconSelector = null;
    }

    /* input */

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (iconSelector != null)
            return iconSelector.keyPressed(keyCode, scanCode, modifiers);

        if (addItemWidget != null)
            return addItemWidget.keyPressed(keyCode, scanCode, modifiers);

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        boolean wasFocused = nameEditBox.isFocused();

        if (iconSelector != null)
            return iconSelector.mouseClicked(mouseX, mouseY, button);

        if (addItemWidget != null)
            return addItemWidget.mouseClicked(mouseX, mouseY, button);

        if (isHoveringIcon(mouseX, mouseY)) {
            openIconSelector();
            return true;
        }

        boolean result = super.mouseClicked(mouseX, mouseY, button);

        if (wasFocused && !nameEditBox.isFocused()) {
            commitNameChange();
        }

        return result;
    }*/

    /* list */

    private class IgnoredItemListWidget
            extends ObjectSelectionList<IgnoredItemListWidget.ItemEntry> {

        private final int leftX;

        public IgnoredItemListWidget(Minecraft mc, int width, int height, int y, int x) {
            super(mc, width, height, y, 25);
            this.leftX = x;
            setX(x);
            refreshList();
        }

        public void refreshList() {

            clearEntries();

            ProfileManager.ItemProfile profile =
                    ProfileManager.getProfile(profileName);

            if (profile == null)
                return;

            List<Item> items = new ArrayList<>();

            for (String idString : profile.ignoredItems) {

                ResourceLocation id = ResourceLocation.tryParse(idString);
                if (id == null) continue;

                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != null)
                    items.add(item);
            }

            items.sort(Comparator.comparing(a -> a.getDescription().getString()));

            for (Item item : items)
                addEntry(new ItemEntry(item));
        }

        @Override
        public int getRowWidth() {
            return width - 20;
        }

        @Override
        protected int getScrollbarPosition() {
            return leftX + width - 6;
        }

        class ItemEntry extends ObjectSelectionList.Entry<ItemEntry> {

            private final Item item;
            private final ItemStack stack;
            private final Button removeButton;

            ItemEntry(Item item) {

                this.item = item;
                this.stack = new ItemStack(item);

                removeButton = Button.builder(
                        Component.literal("Eliminar"),
                        b -> {
                            ProfileManager.removeIgnoredItem(profileName, item);
                            refreshList();
                        }
                ).bounds(0, 0, 70, 20).build();
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left,
                               int width, int height, int mouseX, int mouseY,
                               boolean hover, float partialTick) {

                graphics.renderItem(stack, left + 2, top + 2);

                graphics.drawString(
                        minecraft.font,
                        item.getDescription().getString(),
                        left + 25,
                        top + 6,
                        0xFFFFFF
                );

                removeButton.setX(left + width - 75);
                removeButton.setY(top);
                removeButton.render(graphics, mouseX, mouseY, partialTick);

            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return removeButton.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public Component getNarration() {
                return item.getDescription();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        this.setFocused(null);
        return result;
    }
}