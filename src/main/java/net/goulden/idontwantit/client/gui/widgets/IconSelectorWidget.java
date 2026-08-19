package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.goulden.idontwantit.util.GUIVariables.*;

public class IconSelectorWidget {
    private final Runnable onClose;
    private final String profileName;
    private final Minecraft minecraft;
    private final int screenWidth;
    private final int screenHeight;

    private final int widgetX = spacedX + 20;
    private final int widgetY = spacedY + customHeight + spaceBetweenButtons;
    private final int widgetWidth = 200;
    private final int widgetHeight = 250;

    private EditBox searchBox;
    private IconGridWidget iconGrid;
    private String currentFilter = "";

    public IconSelectorWidget(Runnable onClose, String profileName) {
        this.onClose = onClose;
        this.profileName = profileName;
        this.minecraft = Minecraft.getInstance();
        this.screenWidth = minecraft.getWindow().getGuiScaledWidth();
        this.screenHeight = minecraft.getWindow().getGuiScaledHeight();

        init();
    }

    private void init() {
        // Campo de búsqueda
        this.searchBox = new EditBox(
                minecraft.font,
                widgetX + 5,
                widgetY + 25,
                widgetWidth - 10,
                20,
                Component.literal("Buscar")
        );
        this.searchBox.setHint(Component.literal("Buscar..."));
        this.searchBox.setResponder(text -> {
            currentFilter = text.toLowerCase();
            if (iconGrid != null) {
                iconGrid.refreshGrid();
            }
        });
        this.searchBox.setFocused(true);

        // Grid de iconos
        this.iconGrid = new IconGridWidget(
                widgetX + 5,
                widgetY + 50,
                widgetWidth - 10,
                widgetHeight - 55
        );
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Elevar z-level
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1000);

        // Fondo del widget
        graphics.fill(widgetX, widgetY, widgetX + widgetWidth, widgetY + widgetHeight, 0xFF2B2B2B);

        // Borde
        graphics.fill(widgetX, widgetY, widgetX + widgetWidth, widgetY + 2, 0xFF00AA00);
        graphics.fill(widgetX, widgetY + widgetHeight - 2, widgetX + widgetWidth, widgetY + widgetHeight, 0xFF00AA00);
        graphics.fill(widgetX, widgetY, widgetX + 2, widgetY + widgetHeight, 0xFF00AA00);
        graphics.fill(widgetX + widgetWidth - 2, widgetY, widgetX + widgetWidth, widgetY + widgetHeight, 0xFF00AA00);

        // Título
        graphics.drawString(minecraft.font, "Seleccionar Icono", widgetX + 5, widgetY + 10, 0xFFFFFF);

        // Componentes
        searchBox.render(graphics, mouseX, mouseY, partialTick);
        iconGrid.render(graphics, mouseX, mouseY, partialTick);

        graphics.pose().popPose();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Click en searchBox
        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            searchBox.setFocused(true);
            return true;
        }

        // Click en grid
        if (iconGrid.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Click dentro del widget
        if (mouseX >= widgetX && mouseX <= widgetX + widgetWidth &&
                mouseY >= widgetY && mouseY <= widgetY + widgetHeight) {
            searchBox.setFocused(false);
            return true;
        }

        // Click fuera = cerrar
        onClose.run();
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= widgetX && mouseX <= widgetX + widgetWidth &&
                mouseY >= widgetY && mouseY <= widgetY + widgetHeight) {
            return iconGrid.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            onClose.run();
            return true;
        }

        if (searchBox.isFocused() && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox.isFocused() && searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return false;
    }

    private class IconGridWidget {
        private final int x, y, width, height;
        private final int itemSize = 18;
        private final int itemsPerRow;
        private List<Item> filteredItems;
        private int scrollOffset = 0;
        private int maxScroll = 0;

        public IconGridWidget(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.itemsPerRow = width / itemSize;
            refreshGrid();
        }

        public void refreshGrid() {
            filteredItems = new ArrayList<>();

            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) continue;

                String itemName = item.getDescription().getString().toLowerCase();
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                String idString = itemId != null ? itemId.toString().toLowerCase() : "";

                if (currentFilter.isEmpty() ||
                        itemName.contains(currentFilter) ||
                        idString.contains(currentFilter)) {
                    filteredItems.add(item);
                }
            }

            filteredItems.sort(Comparator.comparing(a -> a.getDescription().getString()));

            int rows = (int) Math.ceil((double) filteredItems.size() / itemsPerRow);
            int visibleRows = height / itemSize;
            maxScroll = Math.max(0, rows - visibleRows);
            scrollOffset = Math.min(scrollOffset, maxScroll);
        }

        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(x, y, x + width, y + height, 0xFF1A1A1A);

            graphics.enableScissor(x, y, x + width, y + height);

            int visibleRows = height / itemSize;
            int startRow = scrollOffset;
            int endRow = Math.min(scrollOffset + visibleRows + 1,
                    (int) Math.ceil((double) filteredItems.size() / itemsPerRow));

            for (int row = startRow; row < endRow; row++) {
                for (int col = 0; col < itemsPerRow; col++) {
                    int index = row * itemsPerRow + col;
                    if (index >= filteredItems.size()) break;

                    Item item = filteredItems.get(index);
                    int itemX = x + col * itemSize + 1;
                    int itemY = y + (row - scrollOffset) * itemSize + 1;

                    boolean isHovered = mouseX >= itemX && mouseX < itemX + 16 &&
                            mouseY >= itemY && mouseY < itemY + 16;

                    if (isHovered) {
                        graphics.fill(itemX, itemY, itemX + 16, itemY + 16, 0xFF555555);
                    }

                    ItemStack stack = new ItemStack(item);
                    graphics.renderItem(stack, itemX, itemY);

                    if (isHovered) {
                        graphics.renderTooltip(minecraft.font, item.getDescription(), (int) mouseX, (int) mouseY);
                    }
                }
            }

            graphics.disableScissor();
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
                return false;
            }

            int col = (int) ((mouseX - x) / itemSize);
            int row = (int) ((mouseY - y) / itemSize) + scrollOffset;
            int index = row * itemsPerRow + col;

            if (index >= 0 && index < filteredItems.size() && col < itemsPerRow) {
                int itemRow = index / itemsPerRow;
                int itemCol = index % itemsPerRow;

                if (itemRow == row && itemCol == col) {
                    int itemX = x + col * itemSize + 1;
                    int itemY = y + (row - scrollOffset) * itemSize + 1;

                    if (mouseX >= itemX && mouseX < itemX + 16 &&
                            mouseY >= itemY && mouseY < itemY + 16) {
                        Item selectedItem = filteredItems.get(index);
                        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(selectedItem);
                        if (itemId != null) {
                            ProfileManager.ItemProfile profile = ProfileManager.getProfile(profileName);
                            if (profile != null) {
                                profile.iconItemId = itemId.toString();
                                ProfileManager.saveProfiles();
                                onClose.run();
                            }
                        }
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
                return false;
            }

            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
            return true;
        }
    }
}