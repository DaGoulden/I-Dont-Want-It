package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.widgets.custom.CustomContainerList;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomEditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import static net.goulden.idontwantit.util.GUIVariables.*;

public class SettingsWidget extends AbstractContainerWidget {

    Runnable onClose;

    public SettingsWidget(int x, int y, int width, int height, Runnable onClose) {
        super(x, y, width, height, Component.empty());
        this.onClose = onClose;
        init();
    }

    private SettingsListWidget settingsList;

    private void init() {

        settingsList = new SettingsListWidget();

    }

    @Override
    protected void renderWidget(GuiGraphics g, int i, int i1, float v) {
        g.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), secondaryColor);
        settingsList.setPosition(getX(), getY());
        settingsList.setSize(getWidth(), getHeight());
        settingsList.setItemHeight(customHeight);
        settingsList.renderWidget(g, i, i1, v);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < getX() || mouseY < getY() || mouseX > getX() + getWidth() || mouseY > getY() + getHeight()) {
            saveAndClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            saveAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void saveAndClose() {
        onClose.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(settingsList);
    }

    private class SettingsListWidget extends CustomContainerList<SettingsListWidget.EntryBase> {

        public SettingsListWidget() {
            refreshList();
        }

        abstract static class EntryBase extends CustomContainerList.Entry<EntryBase> {}

        class ColorSettingEntry extends EntryBase {
            private final String label;
            private final IntSupplier getter;
            private final Consumer<Integer> setter;
            private final CustomEditBox hexBox;

            ColorSettingEntry(String label, IntSupplier getter, Consumer<Integer> setter) {
                this.label = label;
                this.getter = getter;
                this.setter = setter;

                this.hexBox = new CustomEditBox(
                        8,
                        String.format("%08X", getter.getAsInt())
                );
                this.hexBox.setResponder(this::onTextChanged);
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, float partialTick) {

                g.drawString(font,
                        label,
                        left + spacedText,
                        top + (height - font.lineHeight) / 2,
                        linesColor
                );

                int swatchSize = height - 8;
                int swatchX = left + width - 90 - spacedText - swatchSize - spacedText;
                int swatchY = top + 4;
                g.fill(swatchX, swatchY, swatchX + swatchSize, swatchY + swatchSize, getter.getAsInt());
                g.renderOutline(swatchX, swatchY, swatchSize, swatchSize, 0xFF000000 | linesColor);

                hexBox.setPosition(
                        left + width - spacedText - 75,
                        top
                );
                hexBox.setSize(
                        75,
                        height
                );
                hexBox.render(g, mouseX, mouseY, partialTick);
            }

            private void onTextChanged(String text) {
                if (text.length() != 8) return; // esperar a que completen los 8 dígitos
                try {
                    int parsed = (int) Long.parseLong(text, 16);
                    setter.accept(parsed);
                } catch (NumberFormatException ignored) {
                    // Texto inválido, no actualizar todavía
                }
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() {
                return List.of(hexBox);
            }
        }

        class IntSettingEntry extends EntryBase {
            private final String label;
            private final Consumer<Integer> setter;
            private final int min, max;

            private final CustomEditBox numberBox;

            IntSettingEntry(String label, IntSupplier getter, Consumer<Integer> setter, int min, int max) {
                this.label = label;
                this.setter = setter;
                this.min = min;
                this.max = max;

                this.numberBox = new CustomEditBox(
                        5,
                        String.valueOf(getter.getAsInt())
                );
                this.numberBox.setResponder(this::onTextChanged);
                this.numberBox.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
            }

            @Override
            public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, float partialTick) {

                g.drawString(font,
                        label,
                        left + spacedText,
                        top + spacedText,
                        linesColor
                );
                numberBox.setPosition(
                        left + width - spacedText - 75,
                        top
                );
                numberBox.setSize(
                        75,
                        height
                );
                numberBox.render(g, mouseX, mouseY, partialTick);
            }

            private void onTextChanged(String text) {
                if (text.isEmpty()) return;
                try {
                    int parsed = Integer.parseInt(text);
                    parsed = Math.max(min, Math.min(max, parsed));
                    setter.accept(parsed);
                } catch (NumberFormatException ignored) {}
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() {
                return List.of(numberBox);
            }
        }

        public void refreshList() {
            clearEntries();

            // Colors
            addEntry(new ColorSettingEntry(
                    "primaryColor",
                    () -> primaryColor,
                    v -> primaryColor = v
            ));
            addEntry(new ColorSettingEntry(
                    "secondaryColor",
                    () -> secondaryColor,
                    v -> secondaryColor = v
            ));
            addEntry(new ColorSettingEntry(
                    "tertiaryColor",
                    () -> tertiaryColor,
                    v -> tertiaryColor = v
            ));
            addEntry(new ColorSettingEntry(
                    "hoveredAdditiveColor",
                    () -> hoveredAdditiveColor,
                    v -> hoveredAdditiveColor = v
            ));
            addEntry(new ColorSettingEntry(
                    "linesColor",
                    () -> linesColor,
                    v -> linesColor = v
            ));
            addEntry(new ColorSettingEntry(
                    "goodMeaningColor",
                    () -> goodMeaningColor,
                    v -> goodMeaningColor = v
            ));
            addEntry(new ColorSettingEntry(
                    "badMeaningColor",
                    () -> badMeaningColor,
                    v -> badMeaningColor = v
            ));

            // Pixels
            addEntry(new IntSettingEntry(
                    "spacedText",
                    () -> spacedText,
                    v -> spacedText = v, 0, 20
            ));
            addEntry(new IntSettingEntry(
                    "spaceBetweenButtons",
                    () -> spaceBetweenButtons,
                    v -> spaceBetweenButtons = v, 0, 20
            ));

            // Animations
            addEntry(new IntSettingEntry(
                    "stateButtonDuration",
                    () -> stateButtonDuration,
                    v -> stateButtonDuration = v, 1, 100
            ));
            addEntry(new IntSettingEntry(
                    "deleteConfirmationDuration",
                    () -> deleteConfirmationDuration,
                    v -> deleteConfirmationDuration = v, 1, 200
            ));
        }
    }
}
