package net.goulden.idontwantit.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import static net.goulden.idontwantit.util.GUIVariables.*;

public class CustomList<E extends ContainerObjectSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {

    public CustomList(Minecraft mc, int width, int height, int y, int x, int itemHeight) {
        super(mc, width, height, y, itemHeight + spaceBetweenButtons);
        this.setRectangle(width, height, x, y);
        this.headerHeight = -4;
    }

    @Override
    public int getRowWidth() {

        if (this.scrollbarVisible()) return width - scrollBarWidth - spaceBetweenButtons;

        return width;
    }

    @Override
    public int getRowLeft() {
        return this.getX();
    }

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight + this.headerHeight - spaceBetweenButtons;
    }

    @Override
    protected void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i = this.getRowLeft();
        int j = this.getRowWidth();
        int k = this.itemHeight;
        int l = this.getItemCount();

        for (int i1 = 0; i1 < l; i1++) {
            int j1 = this.getRowTop(i1);
            int k1 = this.getRowBottom(i1);
            if (k1 >= this.getY() && j1 <= this.getBottom()) {
                this.renderItem(guiGraphics, mouseX, mouseY, partialTick, i1, i, j1, j, k - spaceBetweenButtons);
            }
        }
    }

    @Override
    public int getScrollbarPosition() {
        return this.getRight() - scrollBarWidth;
    }

    @Override
    protected void renderListBackground(@NotNull GuiGraphics g) {}

    @Override
    protected void renderListSeparators(@NotNull GuiGraphics g) {}
}
