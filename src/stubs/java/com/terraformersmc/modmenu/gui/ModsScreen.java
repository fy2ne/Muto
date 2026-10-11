package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModsScreen extends Screen {
    protected ModListEntry selected;
    protected int rightPaneX;
    protected int paneWidth;
    protected ModListWidget modList;
    protected int searchBoxX;
    protected EditBox searchBox;
    protected SpriteIconButton filtersButton;
    protected SpriteIconButton configureButton;

    protected ModsScreen(Component title) {
        super(title);
    }

    public boolean getModHasConfigScreen(String modId) {
        return false;
    }

    public void safelyOpenConfigScreen(String modId) {}
}
