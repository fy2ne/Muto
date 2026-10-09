package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.MutoMod;
import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.client.ModIconManager;
import me.fy2ne.muto.config.MutoConfig;
import me.fy2ne.muto.engine.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MutoConfigScreen extends Screen {
    private static final int TAB_MODS = 0;
    private static final int TAB_SETTINGS = 1;
    private static final int TAB_DEVELOPER = 2;
    private static final int TAB_HISTORY = 3;
    private static final int TAB_DIAGNOSTICS = 4;

    private final Screen parent;
    private int activeTab = TAB_MODS;

    private EditBox searchBox;
    private ModListWidget modListWidget;
    private SettingsListWidget settingsListWidget;
    private SettingsListWidget devListWidget;
    private HistoryListWidget historyListWidget;
    private String query = "";

    private Button tabModsBtn;
    private Button tabSettingsBtn;
    private Button tabDevBtn;
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
        int rightPanelX = sidebarWidth + 14;
        int rightPanelWidth = this.width - rightPanelX - 12;

        // Category Sidebar Buttons
        tabModsBtn = Button.builder(Component.literal("Loaded Mods"), btn -> setTab(TAB_MODS))
                .bounds(10, topY, sidebarWidth, 20)
                .build();
        this.addRenderableWidget(tabModsBtn);

        tabSettingsBtn = Button.builder(Component.literal("Settings"), btn -> setTab(TAB_SETTINGS))
                .bounds(10, topY + 24, sidebarWidth, 20)
                .build();
        this.addRenderableWidget(tabSettingsBtn);

        tabDevBtn = Button.builder(Component.literal("Developer"), btn -> setTab(TAB_DEVELOPER))
                .bounds(10, topY + 48, sidebarWidth, 20)
                .build();
        this.addRenderableWidget(tabDevBtn);

        tabHistoryBtn = Button.builder(Component.literal("Reload History"), btn -> setTab(TAB_HISTORY))
                .bounds(10, topY + 72, sidebarWidth, 20)
                .build();
        this.addRenderableWidget(tabHistoryBtn);

        tabDiagBtn = Button.builder(Component.literal("Diagnostics"), btn -> setTab(TAB_DIAGNOSTICS))
                .bounds(10, topY + 96, sidebarWidth, 20)
                .build();
        this.addRenderableWidget(tabDiagBtn);

        // Search Box (only for Loaded Mods tab)
        searchBox = new EditBox(this.font, rightPanelX, topY, rightPanelWidth, 18, Component.literal("Search"));
        searchBox.setHint(Component.literal("Search mods by name or id..."));
        searchBox.setResponder(this::onSearchQueryChanged);
        searchBox.setValue(query);
        this.addRenderableWidget(searchBox);

        // Mod List Widget
        modListWidget = new ModListWidget(this.minecraft, rightPanelWidth, panelHeight - 24, topY + 24, 38, rightPanelX);
        this.addRenderableWidget(modListWidget);

        // General Settings List Widget
        settingsListWidget = new SettingsListWidget(this.minecraft, rightPanelWidth, panelHeight, topY, 36, rightPanelX);
        buildGeneralSettings();
        this.addRenderableWidget(settingsListWidget);

        // Developer Settings List Widget
        devListWidget = new SettingsListWidget(this.minecraft, rightPanelWidth, panelHeight, topY, 36, rightPanelX);
        buildDeveloperSettings();
        this.addRenderableWidget(devListWidget);

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

    private void buildGeneralSettings() {
        if (settingsListWidget == null) return;
        settingsListWidget.clear();

        MutoConfig cfg = MutoConfig.get();

        settingsListWidget.addSetting(new HeaderEntry("GENERAL PREFERENCES"));

        settingsListWidget.addSetting(new ToggleSettingEntry(
                "Confirm Before Reload",
                "Display confirmation dialog before starting hot-reload scan",
                () -> cfg.confirmBeforeReload,
                v -> cfg.confirmBeforeReload = v
        ));

        settingsListWidget.addSetting(new ToggleSettingEntry(
                "Auto-Reload Textures & Assets",
                "Flush client textures and re-sync resources after mod reload",
                () -> cfg.autoReloadResources,
                v -> cfg.autoReloadResources = v
        ));

        settingsListWidget.addSetting(new ToggleSettingEntry(
                "In-Game Notifications",
                "Display notification toasts upon reload completion or error",
                () -> cfg.showToasts,
                v -> cfg.showToasts = v
        ));

        settingsListWidget.addSetting(new ToggleSettingEntry(
                "Clean Shadow Cache on Exit",
                "Purge temporary sandboxed shadow jars when shutting down",
                () -> cfg.autoPruneShadowCache,
                v -> cfg.autoPruneShadowCache = v
        ));

        settingsListWidget.addSetting(new ToggleSettingEntry(
                "Title Screen Quick Button",
                "Render official 20x20 reload icon button on main title screen",
                () -> cfg.showTitleScreenButton,
                v -> cfg.showTitleScreenButton = v
        ));
    }

    private void buildDeveloperSettings() {
        if (devListWidget == null) return;
        devListWidget.clear();

        MutoConfig cfg = MutoConfig.get();

        devListWidget.addSetting(new HeaderEntry("DEVELOPER SUITE & INTERNALS"));

        devListWidget.addSetting(new ToggleSettingEntry(
                "Master Developer Mode",
                "Unlock experimental bytecode controls, memory metrics, and debug tools",
                () -> cfg.developerMode,
                v -> {
                    cfg.developerMode = v;
                    buildDeveloperSettings();
                }
        ));

        devListWidget.addSetting(new ToggleSettingEntry(
                "Verbose Classloader Logs",
                "Log granular class definition traces and ASM transforms to muto.log",
                () -> cfg.verboseLogging,
                v -> cfg.verboseLogging = v,
                () -> cfg.developerMode
        ));

        devListWidget.addSetting(new ToggleSettingEntry(
                "Force Core/Stubborn Reload",
                "Experimental: Attempt live reload on engine-bound boot mods",
                () -> cfg.allowStubbornReload,
                v -> cfg.allowStubbornReload = v,
                () -> cfg.developerMode
        ));

        devListWidget.addSetting(new ToggleSettingEntry(
                "Bypass Preflight Checks",
                "Mount dynamic classloaders without pre-verifying entrypoint classes",
                () -> cfg.skipPreflightCheck,
                v -> cfg.skipPreflightCheck = v,
                () -> cfg.developerMode
        ));

        devListWidget.addSetting(new ToggleSettingEntry(
                "Track Heap & GC Delta",
                "Measure JVM memory consumption and GC pressure during reload cycles",
                () -> cfg.trackAllocationMetrics,
                v -> cfg.trackAllocationMetrics = v,
                () -> cfg.developerMode
        ));

        devListWidget.addSetting(new HeaderEntry("DEVELOPER ACTIONS"));

        // Action 1: Purge Shadow Cache
        ActionSettingEntry purgeEntry = new ActionSettingEntry(
                "Purge Shadow Cache",
                "Delete cached shadow jars in temp folder to free disk space",
                "Clear Cache",
                () -> {}
        );
        purgeEntry.setAction(() -> {
            Path cacheDir = Path.of(System.getProperty("java.io.tmpdir"), "muto_shadow_cache");
            long bytes = 0;
            int count = 0;
            if (Files.exists(cacheDir)) {
                try (var s = Files.walk(cacheDir)) {
                    for (Path p : (Iterable<Path>) s::iterator) {
                        if (Files.isRegularFile(p)) {
                            bytes += Files.size(p);
                            try { Files.delete(p); count++; } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception ignored) {}
            }
            double mb = bytes / (1024.0 * 1024.0);
            purgeEntry.setStatus(String.format("Purged %d shadow jars (%.2f MB freed)", count, mb));
        });
        devListWidget.addSetting(purgeEntry);

        // Action 2: Dump ClassLoader Hierarchy
        ActionSettingEntry dumpEntry = new ActionSettingEntry(
                "Dump ClassLoader Tree",
                "Write active classloader hierarchy and jar catalog to logs file",
                "Dump Tree",
                () -> {}
        );
        dumpEntry.setAction(() -> {
            try {
                Path dumpPath = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("muto-classloader-dump.txt");
                Files.createDirectories(dumpPath.getParent());
                List<String> dump = new ArrayList<>();
                dump.add("=== MUTO CLASSLOADER DUMP ===");
                dump.add("Timestamp: " + Instant.now());
                dump.add("JVM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version"));
                dump.add("DCEVM: " + MutoMod.hasDcevm() + " | JBR: " + MutoMod.isJbr());
                dump.add("");
                dump.add("Active Dynamic Mod Registry:");
                for (var e : ReloadEngine.INSTANCE.currentSnapshot().mods().entrySet()) {
                    dump.add(String.format("  - [%s] v%s -> %s (Tier: %s)",
                            e.getKey(), e.getValue().version(), e.getValue().jarPath().getFileName(),
                            ReloadEngine.INSTANCE.currentSnapshot().tier(e.getKey())));
                }
                Files.write(dumpPath, dump);
                dumpEntry.setStatus("Dump written to logs/muto-classloader-dump.txt");
            } catch (Exception e) {
                dumpEntry.setStatus("Dump error: " + e.getMessage());
            }
        });
        devListWidget.addSetting(dumpEntry);

        // Action 3: Dry-Run Scan
        ActionSettingEntry scanEntry = new ActionSettingEntry(
                "Simulate Scan (Dry-Run)",
                "Scan /mods folder and compute diffs without touching running JVM",
                "Dry Run",
                () -> {}
        );
        scanEntry.setAction(() -> {
            try {
                Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
                ReloadPlan plan = ReloadEngine.INSTANCE.plan(modsDir);
                ModDiff diff = plan.diff();
                scanEntry.setStatus(String.format("Dry run: +%d added, -%d removed, ~%d changed",
                        diff.added().size(), diff.removed().size(), diff.updated().size()));
            } catch (Exception e) {
                scanEntry.setStatus("Scan failed: " + e.getMessage());
            }
        });
        devListWidget.addSetting(scanEntry);
    }

    private void setTab(int tab) {
        this.activeTab = tab;
        boolean isMods = (tab == TAB_MODS);
        boolean isSettings = (tab == TAB_SETTINGS);
        boolean isDev = (tab == TAB_DEVELOPER);
        boolean isHistory = (tab == TAB_HISTORY);

        if (searchBox != null) searchBox.visible = isMods;
        if (modListWidget != null) modListWidget.visible = isMods;
        if (settingsListWidget != null) settingsListWidget.visible = isSettings;
        if (devListWidget != null) devListWidget.visible = isDev;
        if (historyListWidget != null) {
            historyListWidget.visible = isHistory;
            if (isHistory) refreshHistory();
        }
    }

    private void promptReload() {
        if (this.minecraft != null) {
            if (!MutoConfig.get().confirmBeforeReload) {
                this.minecraft.setScreenAndShow(new MutoReloadScreen(this));
                return;
            }
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
        int activeTabY = 40 + activeTab * 24;
        g.fill(8, activeTabY + 2, 10, activeTabY + 18, 0xFF38BDF8);

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

    public final class SettingsListWidget extends ObjectSelectionList<SettingsBaseEntry> {
        private final int customX;

        public SettingsListWidget(Minecraft mc, int width, int height, int y, int itemHeight, int customX) {
            super(mc, width, height, y, itemHeight);
            this.customX = customX;
            this.updateSizeAndPosition(width, height, customX, y);
        }

        public void clear() {
            this.clearEntries();
        }

        public void addSetting(SettingsBaseEntry entry) {
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

    public abstract static class SettingsBaseEntry extends ObjectSelectionList.Entry<SettingsBaseEntry> {
        @Override
        public Component getNarration() {
            return Component.empty();
        }
    }

    public static final class HeaderEntry extends SettingsBaseEntry {
        private final String title;

        public HeaderEntry(String title) {
            this.title = title;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float dt) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            var font = Minecraft.getInstance().font;

            g.text(font, Component.literal("§6§l" + title), x + 6, y + 8, 0xFFFFFFFF);
            g.fill(x + 4, y + 22, x + w - 4, y + 23, 0x30FFFFFF);
        }
    }

    public static final class ToggleSettingEntry extends SettingsBaseEntry {
        private final String title;
        private final String description;
        private final java.util.function.Supplier<Boolean> getter;
        private final java.util.function.Consumer<Boolean> setter;
        private final java.util.function.Supplier<Boolean> enabledCheck;

        public ToggleSettingEntry(String title, String description,
                                  java.util.function.Supplier<Boolean> getter,
                                  java.util.function.Consumer<Boolean> setter) {
            this(title, description, getter, setter, () -> true);
        }

        public ToggleSettingEntry(String title, String description,
                                  java.util.function.Supplier<Boolean> getter,
                                  java.util.function.Consumer<Boolean> setter,
                                  java.util.function.Supplier<Boolean> enabledCheck) {
            this.title = title;
            this.description = description;
            this.getter = getter;
            this.setter = setter;
            this.enabledCheck = enabledCheck;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float dt) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            int h = this.getContentHeight();
            var font = Minecraft.getInstance().font;

            g.fill(x, y, x + w, y + h, hovered ? 0x351E293B : 0x200F172A);

            boolean enabled = enabledCheck.get();
            boolean val = getter.get();

            int accent = !enabled ? 0xFF475569 : (val ? 0xFF22C55E : 0xFF64748B);
            g.fill(x, y, x + 3, y + h, accent);

            String titleColor = enabled ? "§f§l" : "§7§l";
            g.text(font, Component.literal(titleColor + title), x + 10, y + 6, 0xFFFFFFFF);
            g.text(font, Component.literal("§8" + description), x + 10, y + 20, 0xFF888888);

            int btnW = 56;
            int btnH = 20;
            int btnX = x + w - btnW - 8;
            int btnY = y + 7;

            boolean btnHover = enabled && mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
            int btnBg = !enabled ? 0x20334155 : (btnHover ? 0x50334155 : 0x301E293B);
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);

            int borderCol = !enabled ? 0x4064748B : (val ? 0xFF22C55E : 0xFF94A3B8);
            g.fill(btnX, btnY, btnX + btnW, btnY + 1, borderCol);
            g.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, borderCol);
            g.fill(btnX, btnY, btnX + 1, btnY + btnH, borderCol);
            g.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, borderCol);

            String label = !enabled ? "§8LOCKED" : (val ? "§aTRUE" : "§cFALSE");
            int labelW = font.width(label);
            g.text(font, Component.literal(label), btnX + (btnW - labelW) / 2, btnY + 6, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() != 0 || !enabledCheck.get()) return false;
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            int btnW = 56;
            int btnH = 20;
            int btnX = x + w - btnW - 8;
            int btnY = y + 7;

            if (event.x() >= btnX && event.x() <= btnX + btnW && event.y() >= btnY && event.y() <= btnY + btnH) {
                setter.accept(!getter.get());
                MutoConfig.save();
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return false;
        }
    }

    public static final class ActionSettingEntry extends SettingsBaseEntry {
        private final String title;
        private final String description;
        private final String buttonText;
        private Runnable action;
        private String status = "";

        public ActionSettingEntry(String title, String description, String buttonText, Runnable action) {
            this.title = title;
            this.description = description;
            this.buttonText = buttonText;
            this.action = action;
        }

        public void setAction(Runnable r) {
            this.action = r;
        }

        public void setStatus(String s) {
            this.status = s;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float dt) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            int h = this.getContentHeight();
            var font = Minecraft.getInstance().font;

            g.fill(x, y, x + w, y + h, hovered ? 0x351E293B : 0x200F172A);
            g.fill(x, y, x + 3, y + h, 0xFF38BDF8);

            g.text(font, Component.literal("§f§l" + title), x + 10, y + 6, 0xFFFFFFFF);
            String desc = status.isEmpty() ? ("§8" + description) : ("§e" + status);
            g.text(font, Component.literal(desc), x + 10, y + 20, 0xFF888888);

            int btnW = 82;
            int btnH = 20;
            int btnX = x + w - btnW - 8;
            int btnY = y + 7;

            boolean btnHover = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
            int btnBg = btnHover ? 0x60334155 : 0x351E293B;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);

            int borderCol = btnHover ? 0xFF38BDF8 : 0xFF64748B;
            g.fill(btnX, btnY, btnX + btnW, btnY + 1, borderCol);
            g.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, borderCol);
            g.fill(btnX, btnY, btnX + 1, btnY + btnH, borderCol);
            g.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, borderCol);

            int labelW = font.width(buttonText);
            g.text(font, Component.literal(buttonText), btnX + (btnW - labelW) / 2, btnY + 6, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() != 0) return false;
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            int btnW = 82;
            int btnH = 20;
            int btnX = x + w - btnW - 8;
            int btnY = y + 7;

            if (event.x() >= btnX && event.x() <= btnX + btnW && event.y() >= btnY && event.y() <= btnY + btnH) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                if (action != null) action.run();
                return true;
            }
            return false;
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
