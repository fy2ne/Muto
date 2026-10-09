package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.MutoMod;
import me.fy2ne.muto.engine.ModSnapshot;
import me.fy2ne.muto.engine.ModTier;
import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ScannedMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MutoConfigScreen extends Screen {
    private final Screen parent;
    private EditBox searchBox;
    private ModListWidget listWidget;
    private String query = "";

    public MutoConfigScreen(Screen parent) {
        super(Component.literal("Muto — Mod Reload Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int listTop = 50;
        int listBottom = this.height - 36;
        int listHeight = listBottom - listTop;

        int searchW = Math.min(340, this.width - 40);
        int searchX = (this.width - searchW) / 2;
        searchBox = new EditBox(this.font, searchX, 24, searchW, 18, Component.literal("Search"));
        searchBox.setHint(Component.literal("Search mods by id..."));
        searchBox.setResponder(this::onSearchQueryChanged);
        searchBox.setValue(query);
        this.addRenderableWidget(searchBox);

        listWidget = new ModListWidget(this.minecraft, this.width, listHeight, listTop, 26);
        this.addRenderableWidget(listWidget);
        refreshEntries();

        // Bottom action buttons
        int btnW = 120;
        int gap = 12;
        int totalBtnW = btnW * 2 + gap;
        int btnStartX = (this.width - totalBtnW) / 2;
        int btnY = this.height - 28;

        this.addRenderableWidget(
                Button.builder(Component.literal("Done"), btn -> this.onClose())
                        .bounds(btnStartX, btnY, btnW, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Reload Mods (↻)"), btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new ConfirmScreen(
                                accepted -> {
                                    if (accepted && this.minecraft != null) {
                                        this.minecraft.setScreenAndShow(new MutoReloadScreen(this));
                                    } else if (this.minecraft != null) {
                                        this.minecraft.setScreenAndShow(this);
                                    }
                                },
                                Component.literal("§f§lReload Mods?"),
                                Component.literal("§7Muto will scan /mods, update active classes, and synchronize loader registries.\n\n§eUnsaved world state should be saved before proceeding."),
                                Component.literal("Yes"),
                                Component.literal("No")
                        ));
                    }
                })
                .bounds(btnStartX + btnW + gap, btnY, btnW, 20)
                .tooltip(Tooltip.create(Component.literal("Scan /mods and apply hot-reloads")))
                .build()
        );
    }

    private void onSearchQueryChanged(String text) {
        this.query = text.trim().toLowerCase(Locale.ROOT);
        refreshEntries();
    }

    private void refreshEntries() {
        if (listWidget == null) return;
        listWidget.clear();

        ModSnapshot snap = ReloadEngine.INSTANCE.currentSnapshot();
        List<ScannedMod> allMods = new ArrayList<>(snap.mods().values());

        for (ScannedMod mod : allMods) {
            if (!query.isEmpty() && !mod.id().toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            ModTier tier = snap.tier(mod.id());
            listWidget.addMod(new ModEntry(mod, tier));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float dt) {
        super.extractRenderState(g, mx, my, dt);

        int midX = this.width / 2;

        // Header Title
        g.centeredText(this.font, Component.literal("§f§lMuto Mod Manager"), midX, 10, 0xFFFFFFFF);

        // Header and Footer subtle divider lines
        g.fill(10, 46, this.width - 10, 47, 0x40FFFFFF);
        g.fill(10, this.height - 34, this.width - 10, this.height - 33, 0x40FFFFFF);

        // Header runtime tags in top right corner
        String jbrTag = MutoMod.isJbr() ? "§aJBR✓" : "§7JBR✗";
        String dcevmTag = MutoMod.hasDcevm() ? "§aDCEVM✓" : "§7DCEVM✗";
        g.text(this.font, Component.literal(jbrTag + " " + dcevmTag), this.width - 90, 10, 0xFFFFFFFF);

        // Summary stats on bottom left
        ModSnapshot snap = ReloadEngine.INSTANCE.currentSnapshot();
        int total = snap.mods().size();
        int live = snap.reloadableMods().size();
        int stubborn = snap.stubbornMods().size();
        g.text(this.font, Component.literal("§7" + total + " mods  §a" + live + " live  §7" + stubborn + " core"), 14, this.height - 22, 0xFF888888);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }

    public final class ModListWidget extends ObjectSelectionList<ModEntry> {
        public ModListWidget(Minecraft mc, int width, int height, int y, int itemHeight) {
            super(mc, width, height, y, itemHeight);
        }

        public void clear() {
            this.clearEntries();
        }

        public void addMod(ModEntry entry) {
            this.addEntry(entry);
        }

        @Override
        protected boolean entriesCanBeSelected() {
            return false;
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor g) {
            // soft translucent fill instead of solid dirt tiling
            g.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0x33000000);
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor g) {
            // omit thick vanilla dirt separators
        }

        @Override
        public int getRowWidth() {
            return Math.min(440, MutoConfigScreen.this.width - 32);
        }

        @Override
        protected int scrollBarX() {
            return (MutoConfigScreen.this.width + getRowWidth()) / 2 + 6;
        }
    }

    public final class ModEntry extends ObjectSelectionList.Entry<ModEntry> {
        private final ScannedMod mod;
        private final ModTier tier;

        public ModEntry(ScannedMod mod, ModTier tier) {
            this.mod = mod;
            this.tier = tier;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float dt) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            int h = this.getContentHeight();

            // Card background
            int bg = hovered ? 0x401E293B : 0x220B132B;
            g.fill(x, y, x + w, y + h, bg);

            // Left indicator bar
            int accent = switch (tier) {
                case CLEAN -> 0xFF22C55E;
                case STANDARD -> 0xFF00E5FF;
                case STUBBORN -> 0xFF94A3B8;
            };
            g.fill(x, y, x + 3, y + h, hovered ? 0xFFFFFFFF : accent);

            // Mod name / ID
            String idStr = mod.id().length() > 22 ? mod.id().substring(0, 20) + "…" : mod.id();
            g.text(font, Component.literal("§f§l" + idStr), x + 8, y + 4, 0xFFFFFFFF);

            // Version
            g.text(font, Component.literal("§7v" + mod.version()), x + 155, y + 5, 0xFFA0AEC0);

            // Tier & Status Badge
            String badge = switch (tier) {
                case CLEAN -> "§a[ Clean ]";
                case STANDARD -> "§b[ Live ]";
                case STUBBORN -> "§7[ Core ]";
            };
            int badgeW = font.width(badge);
            g.text(font, Component.literal(badge), x + w - badgeW - 8, y + 5, 0xFFFFFFFF);

            // Tooltip on hover
            if (hovered) {
                List<Component> tooltip = List.of(
                        Component.literal("§e" + mod.id() + " §7v" + mod.version()),
                        Component.literal("§7" + tier.desc()),
                        Component.literal("§8Jar: " + mod.jarPath().getFileName()),
                        tier.isReloadable()
                                ? Component.literal("§a✓ Dynamic reloadable")
                                : Component.literal("§c✗ Engine bound (requires restart)")
                );
                g.setComponentTooltipForNextFrame(font, tooltip, mx, my);
            }
        }

        @Override
        public Component getNarration() {
            return Component.literal(mod.id());
        }
    }
}
