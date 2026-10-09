package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.MutoMod;
import me.fy2ne.muto.engine.ModSnapshot;
import me.fy2ne.muto.engine.ModTier;
import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ScannedMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MutoConfigScreen extends Screen {
    private final Screen parent;
    private int scrollOffset;
    private static final int ROW_H = 22;
    private static final int LIST_Y = 90;

    public MutoConfigScreen(Screen parent) {
        super(Component.literal("Muto — Mod Reload Manager"));
        this.parent = parent;
        this.scrollOffset = 0;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(
                Button.builder(Component.literal("← Back"), btn -> this.minecraft.setScreenAndShow(parent))
                        .bounds(8, 8, 60, 20)
                        .build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float dt) {
        // Background
        g.fill(0, 0, this.width, this.height, 0xFF0A0D14);
        g.fillGradient(0, 0, this.width, 60, 0xFF0D1B2A, 0xFF0A0D14);

        int midX = this.width / 2;

        g.centeredText(this.font, Component.literal("§b§lMUTO"), midX, 20, 0xFF00E5FF);
        g.centeredText(this.font, Component.literal("§8Mod Reload Manager"), midX, 34, 0xFF4A5568);

        // Runtime flags row
        String jbrTag = MutoMod.isJbr() ? "§aJBR §7✓" : "§cJBR §7✗";
        String dcevmTag = MutoMod.hasDcevm() ? "§aDCEVM §7✓" : "§cDCEVM §7✗";
        g.centeredText(this.font, Component.literal(jbrTag + "   " + dcevmTag), midX, 48, 0xFFFFFFFF);

        // Separator
        g.fill(24, 68, this.width - 24, 69, 0xFF2D3748);

        // Column headers
        g.text(this.font, Component.literal("§7Mod ID"), 30, 75, 0xFFFFFFFF);
        g.text(this.font, Component.literal("§7Version"), midX - 60, 75, 0xFFFFFFFF);
        g.text(this.font, Component.literal("§7Tier"), midX + 60, 75, 0xFFFFFFFF);
        g.text(this.font, Component.literal("§7Status"), this.width - 90, 75, 0xFFFFFFFF);
        g.fill(24, 85, this.width - 24, 86, 0xFF2D3748);

        // Mod list
        ModSnapshot snap = ReloadEngine.INSTANCE.currentSnapshot();
        Map<String, ScannedMod> mods = snap.mods();

        List<Map.Entry<String, ScannedMod>> entries = new ArrayList<>(mods.entrySet());
        int listH = this.height - LIST_Y - 30;
        int maxVisible = listH / ROW_H;
        int startIdx = Math.min(scrollOffset, Math.max(0, entries.size() - maxVisible));

        g.enableScissor(0, LIST_Y, this.width, LIST_Y + listH);
        for (int i = startIdx; i < Math.min(entries.size(), startIdx + maxVisible + 1); i++) {
            var entry = entries.get(i);
            ScannedMod mod = entry.getValue();
            ModTier tier = snap.tier(mod.id());
            int rowY = LIST_Y + (i - startIdx) * ROW_H;

            // Alternating row background
            if (i % 2 == 0) {
                g.fill(24, rowY, this.width - 24, rowY + ROW_H - 2, 0x18FFFFFF);
            }

            // Mod ID
            String idLabel = mod.id().length() > 20 ? mod.id().substring(0, 18) + "…" : mod.id();
            g.text(this.font, Component.literal("§f" + idLabel), 30, rowY + 7, 0xFFFFFFFF);

            // Version
            g.text(this.font, Component.literal("§7" + mod.version()), midX - 60, rowY + 7, 0xFFFFFFFF);

            // Tier badge
            String tierLabel = switch (tier) {
                case CLEAN -> "§a✦ Clean";
                case STANDARD -> "§e● Standard";
                case STUBBORN -> "§c■ Stubborn";
            };
            g.text(this.font, Component.literal(tierLabel), midX + 60, rowY + 7, 0xFFFFFFFF);

            // Status
            String status = tier.isReloadable() ? "§a✓ Live" : "§7— Locked";
            g.text(this.font, Component.literal(status), this.width - 90, rowY + 7, 0xFFFFFFFF);
        }
        g.disableScissor();

        // Scroll hint
        if (entries.size() > maxVisible) {
            int barH = (int) ((float) maxVisible / entries.size() * listH);
            int barY = LIST_Y + (int) ((float) startIdx / entries.size() * listH);
            g.fill(this.width - 6, LIST_Y, this.width - 4, LIST_Y + listH, 0xFF1A202C);
            g.fill(this.width - 6, barY, this.width - 4, barY + barH, 0xFF00E5FF);
        }

        // Footer: total counts
        int reloadable = snap.reloadableMods().size();
        int stubborn = snap.stubbornMods().size();
        g.centeredText(this.font, Component.literal(
                "§7" + mods.size() + " total  §a" + reloadable + " live  §c" + stubborn + " locked"
        ), midX, this.height - 18, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        ModSnapshot snap = ReloadEngine.INSTANCE.currentSnapshot();
        int total = snap.mods().size();
        int listH = this.height - LIST_Y - 30;
        int maxVisible = listH / ROW_H;
        int maxOffset = Math.max(0, total - maxVisible);
        scrollOffset = (int) Math.max(0, Math.min(maxOffset, scrollOffset - dy));
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }
}
