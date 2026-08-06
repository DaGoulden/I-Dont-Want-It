package net.goulden.idontwantit.client.gui.widgets;

import net.goulden.idontwantit.client.gui.MainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public class CustomList extends ContainerObjectSelectionList<CustomList.EntryBase> {
    public CustomList(Minecraft mc, int width, int height, int y, int x) {
        super(mc, width, height, y, 32);
        setX(x);
    }

    abstract static class EntryBase extends ContainerObjectSelectionList.Entry<CustomList.EntryBase> {}

}
