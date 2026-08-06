package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.EditProfileItemsScreen;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class AddItemWidget {
    private final EditProfileItemsScreen parent;
    private final String profileName;
    private final Minecraft minecraft;
    private final int width;
    private final int height;

    private final int widgetX;
    private final int widgetY;
    private final int widgetWidth;
    private final int widgetHeight;

    private EditBox searchBox;
    private ItemListWidget itemList;
    private Button closeButton;
    private String currentFilter = "";

    public AddItemWidget(EditProfileItemsScreen parent, String profileName) {
        this.parent = parent;
        this.profileName = profileName;
        this.minecraft = Minecraft.getInstance();
        this.width = minecraft.getWindow().getGuiScaledWidth();
        this.height = minecraft.getWindow().getGuiScaledHeight();

        // Tamaño del widget (80% del ancho, 80% del alto)
        this.widgetWidth = (int)(width * 0.6);
        this.widgetHeight = (int)(height * 0.5);
        this.widgetX = (width - widgetWidth) / 2;
        this.widgetY = (height - widgetHeight) / 2;

        init();
    }

    private void init() {
        // Campo de búsqueda
        this.searchBox = new EditBox(
                minecraft.font,
                widgetX + 10,
                widgetY + 35,
                widgetWidth - 20,
                20,
                Component.literal("Buscar items")
        );
        this.searchBox.setHint(Component.literal("Buscar..."));
        this.searchBox.setResponder(text -> {
            currentFilter = text.toLowerCase();
            if (itemList != null) {
                itemList.refreshList();
            }
        });
        this.searchBox.setFocused(true); // Enfocar automáticamente

        // Lista de items
        this.itemList = new ItemListWidget(
                minecraft,
                widgetWidth - 20,
                widgetHeight - 100,
                widgetY + 60,
                widgetX + 10
        );

        // Botón cerrar
        /*this.closeButton = Button.builder(
                Component.literal("Cerrar"),
                button -> parent.closeAddItemWidget()
        ).bounds(widgetX + widgetWidth / 2 - 50, widgetY + widgetHeight - 30, 100, 20).build();*/
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Elevar el z-level para que TODO el widget esté por encima
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1000); // Elevar 1000 unidades en Z

        // 1. Overlay oscurecido (cubre toda la pantalla)
        //graphics.fill(0, 0, width, height, 0xE0000000);

        // 2. Fondo del widget
        graphics.fill(widgetX, widgetY, widgetX + widgetWidth, widgetY + widgetHeight, 0xFF2B2B2B);

        // 3. Borde del widget
        graphics.fill(widgetX, widgetY, widgetX + widgetWidth, widgetY + 2, 0xFF00AA00);
        graphics.fill(widgetX, widgetY + widgetHeight - 2, widgetX + widgetWidth, widgetY + widgetHeight, 0xFF00AA00);
        graphics.fill(widgetX, widgetY, widgetX + 2, widgetY + widgetHeight, 0xFF00AA00);
        graphics.fill(widgetX + widgetWidth - 2, widgetY, widgetX + widgetWidth, widgetY + widgetHeight, 0xFF00AA00);

        // 4. Renderizar componentes (lista, botones, searchBox)
        itemList.render(graphics, mouseX, mouseY, partialTick);
        searchBox.render(graphics, mouseX, mouseY, partialTick);
        closeButton.render(graphics, mouseX, mouseY, partialTick);

        // 5. Título AL FINAL (para que aparezca sobre todo)
        graphics.drawCenteredString(
                minecraft.font,
                "Agregar Items a " + profileName,
                widgetX + widgetWidth / 2,
                widgetY + 15,
                0xFFFFFF
        );

        graphics.pose().popPose();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Click en el search box (tiene prioridad)
        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            searchBox.setFocused(true);
            return true;
        }

        // Click en el botón cerrar
        if (closeButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Click en la lista
        if (itemList.mouseClicked(mouseX, mouseY, button)) {
            // Quitar foco del searchBox si se hace clic en la lista
            searchBox.setFocused(false);
            return true;
        }

        // Click dentro del widget pero fuera de componentes
        if (mouseX >= widgetX && mouseX <= widgetX + widgetWidth &&
                mouseY >= widgetY && mouseY <= widgetY + widgetHeight) {
            // Click dentro del widget, quitar foco del searchBox
            searchBox.setFocused(false);
            return true;
        }

        // Click fuera del widget = cerrar
        //parent.closeAddItemWidget();
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll en la lista
        if (mouseX >= widgetX && mouseX <= widgetX + widgetWidth &&
                mouseY >= widgetY && mouseY <= widgetY + widgetHeight) {
            return itemList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC para cerrar
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            //parent.closeAddItemWidget();
            return true;
        }

        // Delegar al search box (tiene prioridad)
        if (searchBox.isFocused() && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        // Delegar al search box
        if (searchBox.isFocused() && searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return false;
    }

    private class ItemListWidget extends ObjectSelectionList<ItemListWidget.ItemEntry> {
        private final int leftX;

        public ItemListWidget(Minecraft mc, int width, int height, int y, int x) {
            super(mc, width, height, y, 25);
            this.leftX = x;
            this.setX(x);
            refreshList();
        }

        @Override
        protected void renderSelection(GuiGraphics graphics, int top, int width, int height, int outerColor, int innerColor) {
            // No renderizar selección
        }

        public void refreshList() {
            this.clearEntries();

            ProfileManager.ItemProfile profile = ProfileManager.getProfile(profileName);
            if (profile == null) return;

            List<Item> availableItems = new ArrayList<>();

            // Obtener todos los items EXCEPTO los ya ignorados
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) continue;

                // Saltar si ya está ignorado
                if (ProfileManager.isItemIgnoredInProfile(profileName, item)) {
                    continue;
                }

                String itemName = item.getDescription().getString().toLowerCase();
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                String idString = itemId != null ? itemId.toString().toLowerCase() : "";

                if (currentFilter.isEmpty() ||
                        itemName.contains(currentFilter) ||
                        idString.contains(currentFilter)) {
                    availableItems.add(item);
                }
            }

            // Ordenar por nombre
            availableItems.sort((a, b) ->
                    a.getDescription().getString().compareTo(b.getDescription().getString())
            );

            for (Item item : availableItems) {
                this.addEntry(new ItemEntry(item));
            }
        }

        @Override
        public int getRowWidth() {
            return this.width - 20;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.leftX + this.width - 6;
        }

        @Override
        protected void renderListBackground(GuiGraphics graphics) {
            // No renderizar fondo por defecto
        }

        @Override
        protected void renderListSeparators(GuiGraphics graphics) {
            // No renderizar separadores
        }

        class ItemEntry extends ObjectSelectionList.Entry<ItemEntry> {
            private final Item item;
            private final ItemStack stack;
            private final Button addButton;

            public ItemEntry(Item item) {
                this.item = item;
                this.stack = new ItemStack(item);

                this.addButton = Button.builder(
                        Component.literal("Agregar"),
                        button -> {
                            ProfileManager.addIgnoredItem(profileName, item);
                            refreshList();
                        }
                ).bounds(0, 0, 70, 20).build();
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                // Fondo de la entrada
                if (isMouseOver) {
                    graphics.fill(left, top, left + width, top + height, 0x40FFFFFF);
                }

                // Renderizar icono del item
                graphics.renderItem(stack, left + 2, top + 2);

                // Renderizar nombre del item
                String itemName = item.getDescription().getString();
                graphics.drawString(minecraft.font, itemName, left + 25, top + 6, 0xFFFFFF);

                // Botón agregar
                addButton.setX(left + width - 75);
                addButton.setY(top);
                addButton.render(graphics, mouseX, mouseY, partialTick);
            }

            @Override
            public Component getNarration() {
                return item.getDescription();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return addButton.mouseClicked(mouseX, mouseY, button);
            }
        }
    }
}