package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.mods.DiskMod;
import me.fy2ne.muto.mods.ModStateManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public final class MutoModChangesScreen extends Screen {
    private final Screen parent;
    private final Runnable onConfirmed;
    private ChangeListWidget listWidget;

    public MutoModChangesScreen(Screen parent, Runnable onConfirmed) {
        super(Component.literal("Pending Mod Changes"));
        this.parent = parent;
        this.onConfirmed = onConfirmed;
    }

    @Override
    protected void init() {
        int listY = 40;
        int listH = this.height - listY - 40;
        listWidget = new ChangeListWidget(this.minecraft, this.width, listH, listY, 22);
        this.addRenderableWidget(listWidget);

        populateChanges();

        int btnW = 120;
        int gap = 12;
        int totalW = btnW * 2 + gap;
        int startX = (this.width - totalW) / 2;
        int btnY = this.height - 30;

        this.addRenderableWidget(
                Button.builder(Component.literal("Cancel"), btn -> {
                    if (this.minecraft != null) this.minecraft.setScreenAndShow(parent);
                }).bounds(startX, btnY, btnW, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Apply & Reload"), btn -> {
                    ModStateManager.INSTANCE.applyFileRenames();
                    if (onConfirmed != null) {
                        onConfirmed.run();
                    } else if (this.minecraft != null) {
                        me.fy2ne.muto.engine.boundary.ReloadBoundaryManager.triggerReload(this.minecraft, parent);
                    }
                }).bounds(startX + btnW + gap, btnY, btnW, 20).build()
        );
    }

    private void populateChanges() {
        if (listWidget == null) return;
        listWidget.clear();

        ModStateManager mgr = ModStateManager.INSTANCE;
        List<DiskMod> all = mgr.scanDiskMods();
        Map<String, DiskMod> byId = new HashMap<>();
        for (DiskMod m : all) byId.put(m.id(), m);

        Set<String> toDisable = mgr.getStagedDisables();
        Set<String> toEnable = mgr.getStagedEnables();

        List<DiskMod> newMods = new ArrayList<>();
        for (DiskMod m : all) {
            if (m.isNew() && !toDisable.contains(m.id())) {
                newMods.add(m);
            }
        }

        if (!toDisable.isEmpty()) {
            listWidget.addHeader("MODS TO DISABLE (.jar -> .jar.disabled)");
            for (String id : toDisable) {
                DiskMod m = byId.get(id);
                String name = m != null ? m.name() + " (" + id + ")" : id;
                listWidget.addItem("§c− " + name, 0xFFFF6B6B);
            }
        }

        if (!toEnable.isEmpty()) {
            listWidget.addHeader("MODS TO ENABLE (.jar.disabled -> .jar)");
            for (String id : toEnable) {
                DiskMod m = byId.get(id);
                String name = m != null ? m.name() + " (" + id + ")" : id;
                listWidget.addItem("§a+ " + name, 0xFF51CF66);
            }
        }

        if (!newMods.isEmpty()) {
            listWidget.addHeader("NEW MODS FOUND ON DISK (WILL BE LOADED)");
            for (DiskMod m : newMods) {
                listWidget.addItem("§b★ " + m.name() + " (" + m.id() + " v" + m.version() + ")", 0xFF339AF0);
            }
        }

        if (toDisable.isEmpty() && toEnable.isEmpty() && newMods.isEmpty()) {
            listWidget.addItem("§7No pending changes or new mods detected.", 0xFFA0AEC0);
        }
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, this.width, this.height, 0xD810141D);
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.text(this.font, Component.literal("§f§lReview Mod Changes Before Reload"), this.width / 2 - this.font.width("Review Mod Changes Before Reload") / 2, 16, 0xFFFFFFFF);
    }

    private final class ChangeListWidget extends ObjectSelectionList<ChangeEntry> {
        public ChangeListWidget(net.minecraft.client.Minecraft mc, int width, int height, int y, int itemHeight) {
            super(mc, width, height, y, itemHeight);
            this.updateSizeAndPosition(width, height, 0, y);
        }

        public void clear() {
            this.clearEntries();
        }

        public void addHeader(String text) {
            this.addEntry(new ChangeEntry(text, 0xFFFCC419, true));
        }

        public void addItem(String text, int color) {
            this.addEntry(new ChangeEntry(text, color, false));
        }

        @Override
        public int getRowWidth() {
            return Math.min(480, this.width - 40);
        }

        @Override
        protected boolean entriesCanBeSelected() {
            return false;
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor g) {
            g.fill(0, this.getY(), this.width, this.getBottom(), 0x14000000);
        }
    }

    private final class ChangeEntry extends ObjectSelectionList.Entry<ChangeEntry> {
        private final String text;
        private final int color;
        private final boolean isHeader;

        public ChangeEntry(String text, int color, boolean isHeader) {
            this.text = text;
            this.color = color;
            this.isHeader = isHeader;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float dt) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getContentWidth();
            int h = this.getContentHeight();

            if (isHeader) {
                g.fill(x, y + h - 1, x + w, y + h, 0x33FFFFFF);
                g.text(font, Component.literal("§l" + text), x + 4, y + 4, color);
            } else {
                if (hovered) {
                    g.fill(x, y, x + w, y + h, 0x22FFFFFF);
                }
                g.text(font, Component.literal(text), x + 8, y + 5, color);
            }
        }

        @Override
        public Component getNarration() {
            return Component.literal(text);
        }
    }
}
