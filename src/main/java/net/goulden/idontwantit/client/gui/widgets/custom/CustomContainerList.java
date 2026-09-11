package net.goulden.idontwantit.client.gui.widgets.custom;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.List;

import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.RenderUtils.easeInOutQuart;

@OnlyIn(Dist.CLIENT)
public abstract class CustomContainerList<E extends CustomContainerList.Entry<E>> extends AbstractContainerWidget {
    private int itemHeight;
    private int addMaxPosition = 0;
    private final TrackedList children = new TrackedList();
    private int scissorBottom = getBottom();
    private double scrollAmount;
    private boolean scrolling;
    private int scrollbarAnimationElapsed = 0;
    private float scrollbarAnimationProgress;

    public CustomContainerList() {
        super(0, 0, 0, 0, CommonComponents.EMPTY);
    }

    public CustomContainerList(int x, int y, int w, int h, int itemHeight) {
        super(x, y, w, h, CommonComponents.EMPTY);
        this.itemHeight = itemHeight;
    }

    public int getRowLeft() {
        return getX();
    }

    public int getRowRight() {
        return getRowLeft() + getWidth();
    }

    public int getRowWidth() {
        if (isScrollbarVisible()) return width - scrollbarWidth - spaceBetweenButtons;
        return width;
    }

    public int getRowTop(int index) {
        return getY() - (int)getScrollAmount() + index * (itemHeight + spaceBetweenButtons);
    }

    public int getRowBottom(int index) {
        return getRowTop(index) + itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public void maxPositionAdditive(int height) {
        addMaxPosition = height;
    }

    public int getMaxPosition() {
        return getItemCount() * (itemHeight + spaceBetweenButtons) - spaceBetweenButtons + addMaxPosition;
    }

    @Nullable
    public E getFocused() {
        return (E)super.getFocused();
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (getFocused() != focused) {
            super.setFocused(focused);
        }
    }

    @Override
    public final @NotNull List<E> children() {
        return children;
    }

    protected void clearEntries() {
        children.clear();
    }

    protected E getEntry(int index) {
        return children().get(index);
    }

    protected void addEntry(E entry) {
        children.add(entry);
    }

    protected int getItemCount() {
        return children().size();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        if (!isScrollbarVisible()) {
            scrollbarAnimationElapsed = scrollbarAnimationDuration;
        } else if (scrollbarAnimationDuration == 0) {
            scrollbarAnimationElapsed = 0;
        }
        setScrollAmount(scrollAmount);

        g.enableScissor(getX(), getY(), getRight() + 100, scissorBottom);
        renderItems(g, mouseX, mouseY, partialTick);
        g.disableScissor();
        g.enableScissor(getRight() - scrollbarWidth, getY(), getRight(), scissorBottom);
        if (isScrollbarVisible()) renderScrollbar(g);
        g.disableScissor();
    }

    protected void renderItems(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        for (int index = 0; index < getItemCount(); index++) {
            int top = getRowTop(index);
            int k1 = getRowBottom(index);
            if (k1 >= getY() && top <= getBottom()) {
                E e = getEntry(index);
                e.render(g, index, top, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, partialTick);
            }
        }
    }

    protected void renderScrollbar(GuiGraphics g) {
        scrollbarAnimationProgress =
                !(scrollbarAnimationDuration == 0)
                ? easeInOutQuart((float) scrollbarAnimationElapsed / scrollbarAnimationDuration)
                : 0;
        int maxScrollbarHeight = Mth.clamp((int)((float)(height * height) / (float)getMaxPosition()), 32, height - 4);
        int scrollbarY = (int)getScrollAmount() * (height - maxScrollbarHeight) / getMaxScroll() + getY();
        if (scrollbarY < getY()) scrollbarY = getY();

        g.pose().pushPose();
        g.pose().translate(
                getScrollbarX(),
                0,
                0
        );
        g.fill(0, getY(), scrollbarWidth, getY() + getHeight(), tertiaryColor);
        g.fill(0, scrollbarY, scrollbarWidth, scrollbarY + maxScrollbarHeight, secondaryColor);
        g.pose().popPose();
    }

    public void tick() {
        if (isScrollbarVisible() && scrollbarAnimationElapsed > 0) {
            scrollbarAnimationElapsed--;
        }
    }

    public void setScissorBottom(int bottom) {
        scissorBottom = bottom;
    }

    public void scroll(double scrolled) {
        setScrollAmount(getScrollAmount() + scrolled);
    }

    protected boolean isScrollbarVisible() {
        return getMaxScroll() > 0;
    }

    protected float getScrollbarX() {
        return getRight() - scrollbarWidth + scrollbarWidth * scrollbarAnimationProgress;
    }

    public double getScrollAmount() {
        return scrollAmount;
    }

    public void setScrollAmount(double scroll) {
        scrollAmount = Mth.clamp(scroll, 0.0, getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, getMaxPosition() - height);
    }

    protected void updateScrollingState(double mouseX, int button) {
        scrolling = button == 0 && mouseX >= (double) getScrollbarX() && mouseX < (double)(getScrollbarX() + scrollbarWidth);
    }

    @Nullable
    protected final E getEntryAtPosition(double mouseX, double mouseY) {
        int inListY = Mth.floor(mouseY - (double)getY()) + (int)getScrollAmount();
        int index = inListY / (itemHeight + spaceBetweenButtons);
        if (mouseX >= getRowLeft()
                && mouseX <= getRowRight()
                && index >= 0
                && inListY >= 0
                && index < getItemCount()
        ) {
            return children().get(index);
        } else {
            return null;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!(button == 0)) {
            return false;
        } else {
            updateScrollingState(mouseX, button);
            if (!isMouseOver(mouseX, mouseY)) {
                return false;
            } else {
                E e = getEntryAtPosition(mouseX, mouseY);
                if (e != null) {
                    if (e.mouseClicked(mouseX, mouseY, button)) {
                        E e1 = getFocused();
                        if (e1 != e && e1 instanceof ContainerEventHandler containereventhandler) {
                            containereventhandler.setFocused(null);
                        }

                        setFocused(e);
                        setDragging(true);
                        return true;
                    }
                }

                return scrolling;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return getFocused() != null && getFocused().mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        } else if (button == 0 && scrolling) {
            if (mouseY < (double)getY()) {
                setScrollAmount(0.0);
            } else if (mouseY > (double)getBottom()) {
                setScrollAmount(getMaxScroll());
            } else {
                double d0 = Math.max(1, getMaxScroll());
                int i = height;
                int j = Mth.clamp((int)((float)(i * i) / (float)getMaxPosition()), 32, i - 8);
                double d1 = Math.max(1.0, d0 / (double)(i - j));
                setScrollAmount(getScrollAmount() + dragY * d1);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll(-scrollY * (double)itemHeight / 2.0);
        return true;
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) { }

    @OnlyIn(Dist.CLIENT)
    protected abstract static class Entry<E extends CustomContainerList.Entry<E>> implements ContainerEventHandler {

        protected CustomContainerList<E> list;

        public abstract void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, float partialTick);

        @Nullable
        private GuiEventListener focused;
        private boolean dragging;

        @Override
        public void setDragging(boolean dragging) {
            this.dragging = dragging;
        }

        @Override
        public boolean isDragging() {
            return dragging;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener listener) {
            if (focused != null) {
                focused.setFocused(false);
            }

            if (listener != null) {
                listener.setFocused(true);
            }

            focused = listener;
        }

        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return focused;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class TrackedList extends AbstractList<E> {
        private final List<E> delegate = Lists.newArrayList();

        public E get(int index) {
            return delegate.get(index);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        public E set(int index, E entry) {
            E e = delegate.set(index, entry);
            CustomContainerList.this.bindEntryToSelf(entry);
            return e;
        }

        public void add(int index, E entry) {
            delegate.add(index, entry);
            CustomContainerList.this.bindEntryToSelf(entry);
        }

        public E remove(int index) {
            return delegate.remove(index);
        }
    }

    void bindEntryToSelf(CustomContainerList.Entry<E> entry) {
        entry.list = this;
    }
}