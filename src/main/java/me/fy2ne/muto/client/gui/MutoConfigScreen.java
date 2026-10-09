package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.MutoMod;
import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.client.ModIconManager;
import me.fy2ne.muto.engine.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MutoConfigScreen extends Screen {
    private static final int TAB_MODS = 0;
    private static final int TAB_HISTORY = 1;
    private static final int TAB_DIAGNOSTICS = 2;

    private final Screen parent;
    private int activeTab = TAB_MODS;

    private EditBox searchBox;
    private ModListWidget modListWidget;
    private HistoryListWidget historyListWidget;
    private String query = "";

    private Button tabModsBtn;
    private Button tabHistoryBtn;
    private Button tabDiagBtn;

    public MutoConfigScreen(Screen parent) {
        super(Component.literal("Muto Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int sidebarWidth = 114;
        int topY = 40;
        int bottomY = this.height - 36;
        int panelHeight = bottomY - topY;
        int rightPanelX = sidebarWidth + 12;
        int rightPanelWidth = this.width - rightPanelX - 12;

        // Left Category Sidebar Tabs
        tabModsBtn = Button.builder(Component.literal("Loaded Mods"), btn -> setTab(TAB_MODS))
                .bounds(10, topY, sidebarWidth, 22)
                .build();
        this.addRenderableWidget(tabModsBtn);

        tabHistoryBtn = Button.builder(Component.literal("Reload History"), btn -> setTab(TAB_HISTORY))
                .bounds(10, topY + 26, sidebarWidth, 22)
                .build();
        this.addRenderableWidget(tabHistoryBtn);

        tabDiagBtn = Button.builder(Component.literal("Diagnostics"), btn -> setTab(TAB_DIAGNOSTICS))
                .bounds(10, topY + 52, sidebarWidth, 22)
                .build();
        this.addRenderableWidget(tabDiagBtn);

        // Search Box (only for Mods tab)
        searchBox = new EditBox(this.font, rightPanelX, topY, rightPanelWidth, 18, Component.literal("Search"));
        searchBox.setHint(Component.literal("Search mods by name or id..."));
        searchBox.setResponder(this::onSearchQueryChanged);
        searchBox.setValue(query);
        this.addRenderableWidget(searchBox);

        // Mod List Widget
        modListWidget = new ModListWidget(this.minecraft, rightPanelWidth, panelHeight - 24, topY + 24, 38, rightPanelX);
        this.addRenderableWidget(modListWidget);

        // History List Widget
        historyListWidget = new HistoryListWidget(this.minecraft, rightPanelWidth, panelHeight, topY, 48, rightPanelX);
        this.addRenderableWidget(historyListWidget);

        // Bottom Action Buttons
        int btnW = 110;
        int gap = 10;
        int totalBtnW = btnW * 2 + gap;
        int btnStartX = (this.width - totalBtnW) / 2;
        int btnY = this.height - 28;

        this.addRenderableWidget(
                Button.builder(Component.literal("Done"), btn -> this.onClose())
                        .bounds(btnStartX, btnY, btnW, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Reload Mods (↻)"), btn -> promptReload())
                        .bounds(btnStartX + btnW + gap, btnY, btnW, 20)
                        .tooltip(Tooltip.create(Component.literal("Scan /mods and apply hot-reloads")))
                        .build()
        );

        setTab(activeTab);
        refreshEntries();
    }

    private void setTab(int tab) {
        this.activeTab = tab;
        boolean isMods = (tab == TAB_MODS);
        boolean isHistory = (tab == TAB_HISTORY);

        if (searchBox != null) searchBox.visible = isMods;
        if (modListWidget != null) modListWidget.visible = isMods;
        if (historyListWidget != null) {
            historyListWidget.visible = isHistory;
            if (isHistory) refreshHistory();
        }
    }

    private void promptReload() {
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
    }

    private void onSearchQueryChanged(String text) {
        this.query = text.trim().toLowerCase(Locale.ROOT);
        refreshEntries();
    }

    private void refreshEntries() {
        if (modListWidget == null) return;
        modListWidget.clear();

        ModSnapshot snap = ReloadEngine.INSTANCE.currentSnapshot();
        List<ScannedMod> allMods = new ArrayList<>(snap.mods().values());

        for (ScannedMod mod : allMods) {
            if (!query.isEmpty() && !mod.id().toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            ModTier tier = snap.tier(mod.id());
            modListWidget.addMod(new ModEntry(mod, tier));
        }
    }

    private void refreshHistory() {
        if (historyListWidget == null) return;
        historyListWidget.clear();

        List<ReloadHistoryEntry> history = ReloadEngine.INSTANCE.history();
        for (int i = history.size() - 1; i >= 0; i--) {
            historyListWidget.addHistory(new HistoryEntryWidget(history.get(i)));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float dt) {
        super.extractRenderState(g, mx, my, dt);

        int midX = this.width / 2;

        // Title Header
        g.centeredText(this.font, Component.literal("§f§lMuto Configuration"), midX, 12, 0xFFFFFFFF);

        // Header and Footer horizontal divider lines
        g.fill(10, 32, this.width - 10, 33, 0x30FFFFFF);
        g.fill(10, this.height - 34, this.width - 10, this.height - 33, 0x30FFFFFF);

        // Vertical divider separating category sidebar from right panel
        int sidebarSepX = 128;
        g.fill(sidebarSepX, 36, sidebarSepX + 1, this.height - 36, 0x30FFFFFF);

        // Highlight indicator on the active category tab
        int activeTabY = 40 + activeTab * 26;
        g.fill(8, activeTabY + 2, 10, activeTabY + 20, 0xFF38BDF8);

        // Header runtime tags in top right corner
        String jbrTag = MutoMod.isJbr() ? "§aJBR✓" : "§7JBR✗";
        String dcevmTag = MutoMod.hasDcevm() ? "§aDCEVM✓" : "§7DCEVM✗";
        g.text(this.font, Component.literal(jbrTag + " " + dcevmTag), this.width - 92, 12, 0xFFFFFFFF);

        // Render Diagnostics if tab is active
        if (activeTab == TAB_DIAGNOSTICS) {
            renderDiagnostics(g, sidebarSepX + 14, 44, this.width - sidebarSepX - 26);
        }

        // Summary stats on bottom left
        ModSnapshot snap = ReloadEngine.INSTANCE.currentSnapshot();
        int total = snap.mods().size();
        int live = snap.reloadableMods().size();
        int stubborn = snap.stubbornMods().size();
        g.text(this.font, Component.literal("§7" + total + " mods  §a" + live + " live  §7" + stubborn + " core"), 14, this.height - 22, 0xFF888888);
    }

    private void renderDiagnostics(GuiGraphicsExtractor g, int x, int y, int w) {
        int cardH = 26;
        int curY = y;

        // Card 1: Runtime Engine
        g.fill(x, curY, x + w, curY + cardH, 0x241A202C);
        g.fill(x, curY, x + 3, curY + cardH, 0xFF38BDF8);
        g.text(font, Component.literal("§f§lJVM Runtime: §7" + System.getProperty("java.vm.name") + " (" + System.getProperty("java.version") + ")"), x + 8, curY + 8, 0xFFFFFFFF);
        curY += cardH + 8;

        // Card 2: HotSwap Support
        boolean dcevm = MutoMod.hasDcevm();
        int col = dcevm ? 0xFF22C55E : 0xFFEAB308;
        String status = dcevm ? "§aEnhanced DCEVM Dynamic Class Redefinition Active" : "§eStandard JVM ClassLoader Isolation (Add/Remove supported)";
        g.fill(x, curY, x + w, curY + cardH, 0x241A202C);
        g.fill(x, curY, x + 3, curY + cardH, col);
        g.text(font, Component.literal("§f§lHotSwap Capability: " + status), x + 8, curY + 8, 0xFFFFFFFF);
        curY += cardH + 8;

        // Card 3: File Handle Virtualization (Shadow Cache)
        g.fill(x, curY, x + w, curY + cardH, 0x241A202C);
        g.fill(x, curY, x + 3, curY + cardH, 0xFF22C55E);
        g.text(font, Component.literal("§f§lWindows File Sandbox: §aShadow Cache Active §7(mods folder unlocked)"), x + 8, curY + 8, 0xFFFFFFFF);
        curY += cardH + 8;

        // Card 4: Registry Freezing
        g.fill(x, curY, x + w, curY + cardH, 0x241A202C);
        g.fill(x, curY, x + 3, curY + cardH, 0xFF22C55E);
        g.text(font, Component.literal("§f§lRegistry Unfreeze Bridge: §aFully Operational §7(in-memory registry sync)"), x + 8, curY + 8, 0xFFFFFFFF);
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
        private final int customX;

        public ModListWidget(Minecraft mc, int width, int height, int y, int itemHeight, int customX) {
            super(mc, width, height, y, itemHeight);
            this.customX = customX;
            this.updateSizeAndPosition(width, height, customX, y);
        }

        public void clear() {
            this.clearEntries();
        }

        public void addMod(ModEntry entry) {
            this.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return this.width - 16;
        }

        @Override
        public int getRowLeft() {
            return this.customX + 4;
        }

        @Override
        protected int scrollBarX() {
            return this.customX + this.width - 6;
        }

        @Override
        protected boolean entriesCanBeSelected() {
            return false;
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor g) {
            g.fill(this.customX, this.getY(), this.customX + this.width, this.getBottom(), 0x18000000);
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor g) {}
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
            int bg = hovered ? 0x442D3748 : 0x241A202C;
            g.fill(x, y, x + w, y + h, bg);

            // Left accent indicator
            int accent = switch (tier) {
                case CLEAN -> 0xFF22C55E;
                case STANDARD -> 0xFF38BDF8;
                case STUBBORN -> 0xFF64748B;
            };
            g.fill(x, y, x + 3, y + h, hovered ? 0xFFFFFFFF : accent);

            // Mod Icon (24x24)
            Identifier iconId = ModIconManager.getIcon(mod);
            if (iconId != null) {
                try {
                    g.blit(RenderPipelines.GUI_TEXTURED, iconId, x + 8, y + 7, 0f, 0f, 24, 24, 24, 24);
                } catch (Throwable ignored) {}
            }

            // Mod ID & Version
            String idStr = mod.id();
            g.text(font, Component.literal("§f§l" + idStr), x + 38, y + 6, 0xFFFFFFFF);
            g.text(font, Component.literal("§7v" + mod.version()), x + 42 + font.width(idStr), y + 7, 0xFFA0AEC0);

            // Jar filename
            String fileStr = mod.jarPath().getFileName().toString();
            if (fileStr.length() > 42) fileStr = fileStr.substring(0, 40) + "…";
            g.text(font, Component.literal("§8" + fileStr), x + 38, y + 20, 0xFF718096);

            // Status Badge Pill
            String badge = switch (tier) {
                case CLEAN -> "§a[ Dynamic Live ]";
                case STANDARD -> "§b[ Live Standard ]";
                case STUBBORN -> "§7[ Engine Bound ]";
            };
            int badgeW = font.width(badge);
            g.text(font, Component.literal(badge), x + w - badgeW - 8, y + 13, 0xFFFFFFFF);

            // Tooltip on hover
            if (hovered) {
                List<Component> tooltip = List.of(
                        Component.literal("§e" + mod.id() + " §7v" + mod.version()),
                        Component.literal("§7" + tier.desc()),
                        Component.literal("§8File: " + mod.jarPath().getFileName()),
                        tier.isReloadable()
                                ? Component.literal("§a✓ Safe for dynamic in-game reload")
                                : Component.literal("§c✗ Core engine bound (requires client restart)")
                );
                g.setComponentTooltipForNextFrame(font, tooltip, mx, my);
            }
        }

        @Override
        public Component getNarration() {
            return Component.literal(mod.id());
        }
    }

    public final class HistoryListWidget extends ObjectSelectionList<HistoryEntryWidget> {
        private final int customX;

        public HistoryListWidget(Minecraft mc, int width, int height, int y, int itemHeight, int customX) {
            super(mc, width, height, y, itemHeight);
            this.customX = customX;
            this.updateSizeAndPosition(width, height, customX, y);
        }

        public void clear() {
            this.clearEntries();
        }

        public void addHistory(HistoryEntryWidget entry) {
            this.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return this.width - 16;
        }

        @Override
        public int getRowLeft() {
            return this.customX + 4;
        }

        @Override
        protected int scrollBarX() {
            return this.customX + this.width - 6;
        }

        @Override
        protected boolean entriesCanBeSelected() {
            return false;
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor g) {
            g.fill(this.customX, this.getY(), this.customX + this.width, this.getBottom(), 0x18000000);
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor g) {}
    }

    public final class HistoryEntryWidget extends ObjectSelectionList.Entry<HistoryEntryWidget> {
        private final ReloadHistoryEntry entry;

        public HistoryEntryWidget(ReloadHistoryEntry entry) {
            this.entry = entry;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float dt) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            int h = this.getContentHeight();

            int bg = hovered ? 0x442D3748 : 0x241A202C;
            g.fill(x, y, x + w, y + h, bg);

            int accent = entry.success() ? 0xFF22C55E : 0xFFEF4444;
            g.fill(x, y, x + 3, y + h, accent);

            Instant instant = Instant.ofEpochMilli(entry.timestamp());
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            String timeStr = ldt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            String statusStr = entry.success() ? "§a✓ Success" : "§c✗ Failed";
            g.text(font, Component.literal("§f§lReload at " + timeStr + " §7(" + entry.durationMs() + "ms)  " + statusStr), x + 8, y + 6, 0xFFFFFFFF);

            ModDiff d = entry.diff();
            String diffStr = String.format("§7Changes: §a+%d added §7• §c-%d removed §7• §e~%d modified",
                    d.added().size(), d.removed().size(), d.updated().size());
            g.text(font, Component.literal(diffStr), x + 8, y + 20, 0xFFCCCCCC);

            if (entry.logs() != null && !entry.logs().isEmpty()) {
                String lastLog = entry.logs().get(entry.logs().size() - 1);
                if (lastLog.length() > 55) lastLog = lastLog.substring(0, 53) + "…";
                g.text(font, Component.literal("§8> " + lastLog), x + 8, y + 33, 0xFF888888);
            }

            if (hovered && entry.logs() != null && !entry.logs().isEmpty()) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.literal("§eReload Details (" + timeStr + "):"));
                for (int i = 0; i < Math.min(10, entry.logs().size()); i++) {
                    tooltip.add(Component.literal("§7" + entry.logs().get(i)));
                }
                if (entry.logs().size() > 10) {
                    tooltip.add(Component.literal("§8... (" + (entry.logs().size() - 10) + " more lines)"));
                }
                g.setComponentTooltipForNextFrame(font, tooltip, mx, my);
            }
        }

        @Override
        public Component getNarration() {
            return Component.literal("Reload History Entry");
        }
    }
}
