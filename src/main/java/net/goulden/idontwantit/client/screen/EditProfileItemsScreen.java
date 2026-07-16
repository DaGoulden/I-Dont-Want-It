package net.goulden.idontwantit.client.screen;

import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditProfileItemsScreen extends Screen {

    private final Screen parent;
    private String profileName;

    private Button iconSelectorButton;
    private EditBox nameEditBox;
    private IgnoredItemListWidget itemList;
    private Button addItemsButton;
    private Button doneButton;

    private AddItemWidget addItemWidget;
    private IconSelectorWidget iconSelector;

    private static final int ICON_SIZE = 16;

    private int screenTicks = 0;
    private int nameErrorStartTick = -1;
    private static final int ERROR_DURATION = 20;

    public EditProfileItemsScreen(Screen parent, String profileName) {
        super(Component.empty());
        this.parent = parent;
        this.profileName = profileName;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ================= INIT =================
    @Override
    protected void init() {

// ICON SELECTOR BUTTON
        iconSelectorButton = new CustomButton(
                width / 2 - 120,
                60,
                ICON_SIZE,
                ICON_SIZE,
                b -> iconSelector = new IconSelectorWidget(
                        this::closeIconSelector,
                        profileName,
                        width / 2,
                        height / 2
                ),
                0xFF3A3A3A, 0xFF555555
        );
        addRenderableWidget(iconSelectorButton);

// NAME EDIT BOX
        nameEditBox = new EditBox(
                font,
                width / 2 - 100,
                30,
                140,
                20,
                Component.literal("Nombre del perfil")
        );
        nameEditBox.setMaxLength(16);
        nameEditBox.setValue(profileName);
        addRenderableWidget(nameEditBox);

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

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {

// SCREEN NAME
        g.drawString(font,
                "Editar: " + profileName,
                width / 2,
                30,
                0xFFFFFF);

// NAME EDIT BOX
        int shakeOffset = 0;
        if (nameErrorStartTick >= 0) {
            int elapsed = screenTicks - nameErrorStartTick;
            if (elapsed <= ERROR_DURATION) {
                float t = elapsed / (float) ERROR_DURATION;
                shakeOffset = (int)(Math.sin(elapsed * 2.5f) * (1f - 1f - (1f - t) * (1f - t)) * 4f);
                int color = ((int)((1f - t) * 255) << 24) | 0xFF0000;
                int x = nameEditBox.getX() - 1 + shakeOffset;
                int y = nameEditBox.getY() - 1;
                int w = nameEditBox.getWidth() + 2;
                int h = nameEditBox.getHeight() + 2;
                g.fill(x, y, x + w, y + 1, color);
                g.fill(x, y + h - 1, x + w, y + h, color);
                g.fill(x, y, x + 1, y + h, color);
                g.fill(x + w - 1, y, x + w, y + h, color);
            } else {
                nameErrorStartTick = -1;
            }
        }
        int originalX = nameEditBox.getX();
        if (shakeOffset != 0) {
            nameEditBox.setX(originalX + shakeOffset);
        }
        super.render(g, mouseX, mouseY, partialTick);
        nameEditBox.setX(originalX);

// SCREEN NAME
        g.drawCenteredString(
                font,
                "Editar: " + profileName,
                width / 2,
                15,
                0xFFFFFF);

        renderProfileIcon(g, mouseX, mouseY);

        if (iconSelector != null)
            iconSelector.render(g, mouseX, mouseY, partialTick);

        if (addItemWidget != null)
            addItemWidget.render(g, mouseX, mouseY, partialTick);
    }



    /* ticks */

    @Override
    public void tick() {
        screenTicks++;
    }

    /* name change */

    private void commitNameChange() {

        String newName = nameEditBox.getValue().trim();

        if (newName.isEmpty() || newName.equals(profileName) || ProfileManager.getAllProfiles().containsKey(newName)) {
            triggerNameError();
            return;
        }

        ProfileManager.renameProfile(profileName, newName);

        nameEditBox.setFocused(false);

        profileName = newName;
    }

    private void triggerNameError() {
        nameErrorStartTick = screenTicks;
    }

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

        return mouseX >= x && mouseX < x + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE;
    }

    private void renderProfileIcon(GuiGraphics graphics, int mouseX, int mouseY) {

        ProfileManager.ItemProfile profile = ProfileManager.getProfile(profileName);
        if (profile == null) return;

        int x = getIconX();
        int y = getIconY();

        ItemStack iconStack = new ItemStack(profile.getIconItem());

        if (isHoveringIcon(mouseX, mouseY)) {
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0x80FFFFFF);
        }

        graphics.renderItem(iconStack, x, y);

        int ignoredCount = profile.ignoredItems.size();

        String text = ignoredCount + (ignoredCount == 1 ? " item ignorado" : " items ignorados");

        graphics.drawString(font, text, width / 2 - 100, 55, 0xAAAAAA);
    }

    /* modals */

    private void openAddItemWidget() {
        addItemWidget = new AddItemWidget(this, profileName);
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

        if (keyCode == 257 || keyCode == 335) {

            if (nameEditBox.isFocused()) {
                commitNameChange();
                return true;
            }
        }

        if (iconSelector != null)
            return iconSelector.keyPressed(keyCode, scanCode, modifiers);

        if (addItemWidget != null)
            return addItemWidget.keyPressed(keyCode, scanCode, modifiers);

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
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
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

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

            items.sort((a, b) ->
                    a.getDescription().getString()
                            .compareTo(b.getDescription().getString())
            );

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

                graphics.fill(left, top, left + 2, top + 20, 0xFFFF0000);
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
}