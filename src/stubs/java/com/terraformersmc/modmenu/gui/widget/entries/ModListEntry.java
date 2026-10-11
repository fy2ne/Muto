package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class ModListEntry extends ObjectSelectionList.Entry<ModListEntry> {
    public Mod mod;
    protected ModListWidget list;
    public Mod getMod() { return mod; }

    @Override
    public Component getNarration() {
        return Component.empty();
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovered, float delta) {
    }
}
