package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public class ModBadgeRenderer {
    protected int startX;
    protected int startY;
    protected int badgeX;
    protected int badgeY;
    protected int badgeMax;
    protected Mod mod;
    protected Minecraft client;
    protected ModsScreen screen;

    public ModBadgeRenderer(int startX, int startY, int badgeMax, Mod mod, ModsScreen screen) {}

    public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    public void drawBadge(GuiGraphicsExtractor graphics, FormattedCharSequence text, int outlineColor, int fillColor, int mouseX, int mouseY) {}
}
