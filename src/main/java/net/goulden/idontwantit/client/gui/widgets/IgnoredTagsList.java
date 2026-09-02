package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.widgets.custom.CustomButton;
import net.goulden.idontwantit.client.gui.widgets.custom.CustomContainerList;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.goulden.idontwantit.util.GUIVariables.*;
import static net.goulden.idontwantit.util.GUIVariables.fontHeight;
import static net.goulden.idontwantit.util.GUIVariables.secondaryColor;

public class IgnoredTagsList extends CustomContainerList<IgnoredTagsList.EntryBase> {

    String profileName;

    public IgnoredTagsList(String profileName, int x, int y, int w, int h, int itemHeight) {
        super(x, y, w, h, itemHeight);
        this.profileName = profileName;
        refreshList();
    }

    abstract static class EntryBase extends CustomContainerList.Entry<IgnoredTagsList.EntryBase> { }

    class TagEntry extends IgnoredTagsList.EntryBase {

        private final CustomButton removeTagButton;

        private final TagKey<Item> tag;

        TagEntry(TagKey<Item> tag) {

            this.tag = tag;

// REMOVE TAG BUTTON
            removeTagButton = new CustomButton(
                    b -> {
                        ProfileManager.removeIgnoredTag(profileName, tag);
                        refreshList();
                    },
                    "x",
                    true
            );
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, float partialTick) {

// TAG BACKGROUND
            g.fill(left,
                    top,
                    left + width - height - spaceBetweenButtons,
                    top + height,
                    tertiaryColor
            );

// TAG NAME
            g.drawString(
                    font,
                    tag.toString(),
                    left + spacedText,
                    top + spacedText,
                    linesColor
            );

// REMOVE TAG BUTTON
            removeTagButton.setPosition(
                    left + width - height,
                    top
            );
            removeTagButton.setSize(
                    height,
                    height
            );
            removeTagButton.setBackgroundColor(secondaryColor);
            removeTagButton.render(g, mouseX, mouseY, partialTick);
            g.drawCenteredString(font,
                    "x",
                    removeTagButton.getX() + removeTagButton.getWidth() / 2,
                    removeTagButton.getY() + (removeTagButton.getHeight() - fontHeight) / 2,
                    linesColor
            );
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(removeTagButton);
        }
    }

    class AddTagEntry extends IgnoredTagsList.EntryBase {

        private final CustomButton addTagButton;

        public AddTagEntry() {

// ADD TAG BUTTON
            addTagButton = new CustomButton(
                    b -> {
                        refreshList();
                    }
            );
        }

        @Override
        public void render(@NotNull GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, float partialTick) {

// ADD TAG BUTTON
            addTagButton.setPosition(
                    left,
                    top
            );
            addTagButton.setSize(
                    width,
                    height
            );
            addTagButton.setBackgroundColor(secondaryColor);
            addTagButton.render(g, mouseX, mouseY, partialTick);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(addTagButton);
        }
    }

    public void refreshList() {
        clearEntries();

        ProfileManager.ItemProfile profile = ProfileManager.getProfile(profileName);

        if (profile == null) return;

        List<TagKey<Item>> tags = new ArrayList<>();

        for (String idString : profile.ignoredTags) {
            ResourceLocation id = ResourceLocation.tryParse(idString);
            if (id == null) continue;
            tags.add(TagKey.create(Registries.ITEM, id));
        }

        tags.sort(Comparator.comparing(tag -> tag.location().toString()));

        for (TagKey<Item> tag : tags) addEntry(new TagEntry(tag));
        addEntry(new AddTagEntry());
    }
}
