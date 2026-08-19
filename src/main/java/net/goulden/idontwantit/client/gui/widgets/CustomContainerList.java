package net.goulden.idontwantit.client.gui.widgets;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static net.goulden.idontwantit.util.GUIVariables.*;

@OnlyIn(Dist.CLIENT)
public abstract class CustomContainerList<E extends CustomContainerList.Entry<E>> extends AbstractContainerWidget {
    protected final Minecraft minecraft;
    public int itemHeight;
    private final TrackedList children = new TrackedList();
    private double scrollAmount;
    private boolean scrolling;
    @Nullable
    private E selected;
    @Nullable
    private E hovered;

    public CustomContainerList(Minecraft minecraft) {
        super(0, 0, 0, 0, CommonComponents.EMPTY);
        this.minecraft = minecraft;
    }

    public int getRowLeft() {
        return this.getX();
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getWidth();
    }

    public int getRowWidth() {
        if (this.scrollbarVisible()) return width - scrollbarWidth - spaceBetweenButtons;
        return width;
    }

    protected int getRowTop(int index) {
        return this.getY() - (int)this.getScrollAmount() + index * (this.itemHeight + spaceBetweenButtons);
    }

    protected int getRowBottom(int index) {
        return this.getRowTop(index) + this.itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    @Nullable
    public E getSelected() {
        return this.selected;
    }

    public void setSelected(@Nullable E selected) {
        this.selected = selected;
    }

    @Nullable
    public E getFocused() {
        return (E)super.getFocused();
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (this.getFocused() != focused) {
            super.setFocused(focused);
            if (focused == null) {
                this.setSelected(null);
            }
        }
    }

    @Override
    public final @NotNull List<E> children() {
        return this.children;
    }

    protected void clearEntries() {
        this.children.clear();
        this.selected = null;
    }

    protected E getEntry(int index) {
        return this.children().get(index);
    }

    protected void addEntry(E entry) {
        this.children.add(entry);
    }

    protected int getItemCount() {
        return this.children().size();
    }

    @Nullable
    protected final E getEntryAtPosition(double mouseX, double mouseY) {
        int i1 = Mth.floor(mouseY - (double)this.getY()) + (int)this.getScrollAmount();
        int j1 = i1 / (this.itemHeight + spaceBetweenButtons);
        return mouseX >= this.getRowLeft()
                && mouseX <= this.getRowRight()
                && j1 >= 0
                && i1 >= 0
                && j1 < this.getItemCount()
                ? this.children().get(j1)
                : null;
    }

    protected int getMaxPosition() {
        return this.getItemCount() * (this.itemHeight + spaceBetweenButtons) - spaceBetweenButtons;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;
        this.enableScissor(g);

        this.renderListItems(g, mouseX, mouseY, partialTick);
        g.disableScissor();
        if (this.scrollbarVisible()) {
            int scrollbarX = this.getScrollbarPosition();
            int maxScrollbarHeight = (int)((float)(this.height * this.height) / (float)this.getMaxPosition());
            maxScrollbarHeight = Mth.clamp(maxScrollbarHeight, 32, this.height - 4);
            int scrollbarY = (int)this.getScrollAmount() * (this.height - maxScrollbarHeight) / this.getMaxScroll() + this.getY();
            if (scrollbarY < this.getY()) {
                scrollbarY = this.getY();
            }

            g.fill(scrollbarX, this.getY(), scrollbarX + scrollbarWidth, this.getY() + this.getHeight(), tertiaryColor);
            g.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + maxScrollbarHeight, secondaryColor);
        }
    }

    protected boolean scrollbarVisible() {
        return this.getMaxScroll() > 0;
    }

    protected void enableScissor(GuiGraphics g) {
        g.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
    }

    protected void ensureVisible(E entry) {
        int i = this.getRowTop(this.children().indexOf(entry));
        int j = i - this.getY() - this.itemHeight;
        if (j < 0) {
            this.scroll(j);
        }

        int k = this.getBottom() - i - this.itemHeight - this.itemHeight;
        if (k < 0) {
            this.scroll(-k);
        }
    }

    private void scroll(int scroll) {
        this.setScrollAmount(this.getScrollAmount() + (double)scroll);
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double scroll) {
        this.scrollAmount = Mth.clamp(scroll, 0.0, this.getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - this.height);
    }

    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarPosition() && mouseX < (double)(this.getScrollbarPosition() + 6);
    }

    protected int getScrollbarPosition() {
        return this.getRight() - scrollbarWidth;
    }

    protected boolean isValidMouseClick(int button) {
        return button == 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isValidMouseClick(button)) {
            return false;
        } else {
            this.updateScrollingState(mouseX, mouseY, button);
            if (!this.isMouseOver(mouseX, mouseY)) {
                return false;
            } else {
                E e = this.getEntryAtPosition(mouseX, mouseY);
                if (e != null) {
                    if (e.mouseClicked(mouseX, mouseY, button)) {
                        E e1 = this.getFocused();
                        if (e1 != e && e1 instanceof ContainerEventHandler containereventhandler) {
                            containereventhandler.setFocused(null);
                        }

                        this.setFocused(e);
                        this.setDragging(true);
                        return true;
                    }
                }

                return this.scrolling;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        } else if (button == 0 && this.scrolling) {
            if (mouseY < (double)this.getY()) {
                this.setScrollAmount(0.0);
            } else if (mouseY > (double)this.getBottom()) {
                this.setScrollAmount(this.getMaxScroll());
            } else {
                double d0 = Math.max(1, this.getMaxScroll());
                int i = this.height;
                int j = Mth.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
                double d1 = Math.max(1.0, d0 / (double)(i - j));
                this.setScrollAmount(this.getScrollAmount() + dragY * d1);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.setScrollAmount(this.getScrollAmount() - scrollY * (double)this.itemHeight / 2.0);
        return true;
    }

    @Nullable
    protected E nextEntry(ScreenDirection direction, Predicate<E> predicate, @Nullable E selected) {
        int i = switch (direction) {
            case RIGHT, LEFT -> 0;
            case UP -> -1;
            case DOWN -> 1;
        };
        if (!this.children().isEmpty() && i != 0) {
            int j;
            if (selected == null) {
                j = i > 0 ? 0 : this.children().size() - 1;
            } else {
                j = this.children().indexOf(selected) + i;
            }

            for (int k = j; k >= 0 && k < this.children.size(); k += i) {
                E e = this.children().get(k);
                if (predicate.test(e)) {
                    return e;
                }
            }
        }

        return null;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= (double)this.getY()
                && mouseY <= (double)this.getBottom()
                && mouseX >= (double)this.getX()
                && mouseX <= (double)this.getRight();
    }

    protected void renderListItems(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int i = this.getRowLeft();
        int j = this.getRowWidth();
        int k = this.itemHeight;
        int l = this.getItemCount();

        for (int i1 = 0; i1 < l; i1++) {
            int j1 = this.getRowTop(i1);
            int k1 = this.getRowBottom(i1);
            if (k1 >= this.getY() && j1 <= this.getBottom()) {
                this.renderItem(g, mouseX, mouseY, partialTick, i1, i, j1, j, k);
            }
        }
    }

    protected void renderItem(GuiGraphics g, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
        E e = this.getEntry(index);
        e.render(g, index, top, left, width, height, mouseX, mouseY, Objects.equals(this.hovered, e), partialTick);
    }

    @Nullable
    protected E getHovered() {
        return this.hovered;
    }

    void bindEntryToSelf(CustomContainerList.Entry<E> entry) {
        entry.list = this;
    }

    protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, E entry) {
        List<E> list = this.children();
        if (list.size() > 1) {
            int i = list.indexOf(entry);
            if (i != -1) {
                narrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.list", i + 1, list.size()));
            }
        }
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
        if (this.getItemCount() == 0) {
            return null;
        } else if (!(event instanceof FocusNavigationEvent.ArrowNavigation focusnavigationevent$arrownavigation)) {
            return super.nextFocusPath(event);
        } else {
            E e = this.getFocused();
            if (focusnavigationevent$arrownavigation.direction().getAxis() == ScreenAxis.HORIZONTAL && e != null) {
                return ComponentPath.path(this, e.nextFocusPath(event));
            } else {
                int i = -1;
                ScreenDirection screendirection = focusnavigationevent$arrownavigation.direction();
                if (e != null) {
                    i = e.children().indexOf(e.getFocused());
                }

                if (i == -1) {
                    switch (screendirection) {
                        case LEFT:
                            i = Integer.MAX_VALUE;
                            screendirection = ScreenDirection.DOWN;
                            break;
                        case RIGHT:
                            i = 0;
                            screendirection = ScreenDirection.DOWN;
                            break;
                        default:
                            i = 0;
                    }
                }

                E e1 = e;

                ComponentPath componentpath;
                do {
                    e1 = this.nextEntry(screendirection, p_351636_ -> !p_351636_.children().isEmpty(), e1);
                    if (e1 == null) {
                        return null;
                    }

                    componentpath = e1.focusPathAtIndex(focusnavigationevent$arrownavigation, i);
                } while (componentpath == null);

                return ComponentPath.path(this, componentpath);
            }
        }
    }

    @Override
    public NarratableEntry.@NotNull NarrationPriority narrationPriority() {
        return this.isFocused() ? NarratableEntry.NarrationPriority.FOCUSED : super.narrationPriority();
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        E e = this.getHovered();
        if (e != null) {
            e.updateNarration(narrationElementOutput.nest());
            this.narrateListElementPosition(narrationElementOutput, e);
        } else {
            E e1 = this.getFocused();
            if (e1 != null) {
                e1.updateNarration(narrationElementOutput.nest());
                this.narrateListElementPosition(narrationElementOutput, e1);
            }
        }

        narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract static class Entry<E extends CustomContainerList.Entry<E>> implements ContainerEventHandler {

        protected CustomContainerList<E> list;

        @Override
        public void setFocused(boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return this.list.getFocused() == this;
        }

        public abstract void render(
                GuiGraphics g,
                int index,
                int top,
                int left,
                int width,
                int height,
                int mouseX,
                int mouseY,
                boolean hovering,
                float partialTick
        );

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(this.list.getEntryAtPosition(mouseX, mouseY), this);
        }

        @Nullable
        private GuiEventListener focused;
        @Nullable
        private NarratableEntry lastNarratable;
        private boolean dragging;

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        /**
         * Sets if the GUI element is dragging or not.
         *
         * @param dragging the dragging state of the GUI element.
         */
        @Override
        public void setDragging(boolean dragging) {
            this.dragging = dragging;
        }

        /**
         * Called when a mouse button is clicked within the GUI element.
         * <p>
         * @return {@code true} if the event is consumed, {@code false} otherwise.
         *
         * @param mouseX the X coordinate of the mouse.
         * @param mouseY the Y coordinate of the mouse.
         * @param button the button that was clicked.
         */
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
        }

        /**
         * Sets the focus state of the GUI element.
         *
         * @param listener the focused GUI element.
         */
        @Override
        public void setFocused(@Nullable GuiEventListener listener) {
            if (this.focused != null) {
                this.focused.setFocused(false);
            }

            if (listener != null) {
                listener.setFocused(true);
            }

            this.focused = listener;
        }

        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return this.focused;
        }

        @Nullable
        public ComponentPath focusPathAtIndex(FocusNavigationEvent event, int index) {
            if (this.children().isEmpty()) {
                return null;
            } else {
                ComponentPath componentpath = this.children().get(Math.min(index, this.children().size() - 1)).nextFocusPath(event);
                return ComponentPath.path(this, componentpath);
            }
        }

        /**
         * Retrieves the next focus path based on the given focus navigation event.
         * <p>
         * @return the next focus path as a ComponentPath, or {@code null} if there is no next focus path.
         *
         * @param event the focus navigation event.
         */
        @Nullable
        @Override
        public ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
            if (event instanceof FocusNavigationEvent.ArrowNavigation(ScreenDirection direction)) {
                int i = switch (direction) {
                    case LEFT -> -1;
                    case RIGHT -> 1;
                    case UP, DOWN -> 0;
                };
                if (i == 0) {
                    return null;
                }

                int j = Mth.clamp(i + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1);

                for (int k = j; k >= 0 && k < this.children().size(); k += i) {
                    GuiEventListener guieventlistener = this.children().get(k);
                    ComponentPath componentpath = guieventlistener.nextFocusPath(event);
                    if (componentpath != null) {
                        return ComponentPath.path(this, componentpath);
                    }
                }
            }

            return ContainerEventHandler.super.nextFocusPath(event);
        }

        public abstract List<? extends NarratableEntry> narratables();

        void updateNarration(NarrationElementOutput narrationElementOutput) {
            List<? extends NarratableEntry> list = this.narratables();
            Screen.NarratableSearchResult screen$narratablesearchresult = Screen.findNarratableWidget(list, this.lastNarratable);
            if (screen$narratablesearchresult != null) {
                if (screen$narratablesearchresult.priority.isTerminal()) {
                    this.lastNarratable = screen$narratablesearchresult.entry;
                }

                if (list.size() > 1) {
                    narrationElementOutput.add(
                            NarratedElementType.POSITION,
                            Component.translatable("narrator.position.object_list", screen$narratablesearchresult.index + 1, list.size())
                    );
                    if (screen$narratablesearchresult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                        narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
                    }
                }

                screen$narratablesearchresult.entry.updateNarration(narrationElementOutput.nest());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class TrackedList extends AbstractList<E> {
        private final List<E> delegate = Lists.newArrayList();

        public E get(int index) {
            return this.delegate.get(index);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        public E set(int index, E entry) {
            E e = this.delegate.set(index, entry);
            CustomContainerList.this.bindEntryToSelf(entry);
            return e;
        }

        public void add(int index, E entry) {
            this.delegate.add(index, entry);
            CustomContainerList.this.bindEntryToSelf(entry);
        }

        public E remove(int index) {
            return this.delegate.remove(index);
        }
    }
}