package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

public class ModListWidget extends ObjectSelectionList<ModListEntry> {
    public ModListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }
    public void reloadFilters() {}
    public int addEntry(ModListEntry entry) { return 0; }
}
