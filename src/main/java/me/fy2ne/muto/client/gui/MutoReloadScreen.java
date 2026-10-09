package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.MutoMod;
import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ReloadResult;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class MutoReloadScreen extends Screen {
    private static final int CYAN  = 0xFF00E5FF;
    private static final int DIM   = 0xFF718096;
    private static final int DARK  = 0xFF1A202C;
    private static final int RED   = 0xFFFF5555;
    private static final int BG    = 0xF20D1117;

    private static final String[] STAGE_LABELS = {
        "Scanning mod directory…",
        "Building candidate classloader…",
        "Tearing down old runtime contexts…",
        "Unfreezing registries…",
        "Invoking entrypoints…",
        "Finalizing…"
    };

    private final Screen origin;
    private final long startMs;
    private float displayProg;
    private volatile float targetProg = 0.05f;
    private volatile String stageMsg = STAGE_LABELS[0];
    private final AtomicReference<ReloadResult> result = new AtomicReference<>();
    private boolean done;
    private Button returnBtn;
    // collected log lines for the detail panel
    private final List<String> log = new ArrayList<>();
    private long dotTimer;
    private int dotCount;

    public MutoReloadScreen(Screen origin) {
        super(Component.literal("Muto — Reloading Mods"));
        this.origin = origin;
        this.startMs = System.currentTimeMillis();
        this.done = false;

        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");

        ReloadEngine.INSTANCE.reloadAsync(modsDir, msg -> {
            stageMsg = msg;
            log.add(msg);
            // map stage message to progress bucket
            if (msg.startsWith("Scanning"))          targetProg = 0.18f;
            else if (msg.startsWith("Constructing") || msg.startsWith("Building")) targetProg = 0.38f;
            else if (msg.startsWith("Tearing"))      targetProg = 0.58f;
            else if (msg.startsWith("Unfreez"))      targetProg = 0.72f;
            else if (msg.startsWith("Re-invoking") || msg.startsWith("Invoking")) targetProg = 0.88f;
            else if (msg.startsWith("Finaliz"))      targetProg = 0.97f;
        }).thenAccept(res -> {
            result.set(res);
            targetProg = 1.0f;
        });
    }

    @Override
    protected void init() {
        super.init();
        returnBtn = Button.builder(
                Component.literal("Return to Title Screen"),
                btn -> this.minecraft.setScreenAndShow(new TitleScreen())
        ).bounds(this.width / 2 - 100, this.height / 2 + 58, 200, 20).build();
        returnBtn.visible = false;
        this.addRenderableWidget(returnBtn);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return returnBtn != null && returnBtn.visible;
    }

    @Override
    public void tick() {
        super.tick();
        // smooth progress interpolation — 15% per tick toward target
        float gap = targetProg - displayProg;
        displayProg += gap * 0.18f;
        if (Math.abs(gap) < 0.005f) displayProg = targetProg;

        // animated dots
        dotTimer++;
        if (dotTimer % 10 == 0) dotCount = (dotCount + 1) % 4;

        ReloadResult res = result.get();
        if (res != null && !done) {
            if (res.success()) {
                if (displayProg >= 0.99f) {
                    done = true;
                    long elapsed = System.currentTimeMillis() - startMs;
                    MutoLog.info("UI reload complete in {}ms, triggering resource reload", elapsed);
                    try {
                        this.minecraft.reloadResourcePacks().thenRun(() ->
                                this.minecraft.execute(() ->
                                        this.minecraft.setScreenAndShow(new TitleScreen())
                                )
                        );
                    } catch (Exception ex) {
                        MutoLog.warn("resource reload error: {}", ex.getMessage());
                        this.minecraft.setScreenAndShow(new TitleScreen());
                    }
                }
            } else {
                done = true;
                stageMsg = res.error() != null ? "Failed: " + res.error() : "Reload failed — check muto.log";
                if (returnBtn != null) returnBtn.visible = true;
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float dt) {
        int w = this.width;
        int h = this.height;
        int midX = w / 2;
        int midY = h / 2;

        // Full dark background with subtle gradient
        g.fill(0, 0, w, h, BG);
        g.fillGradient(0, 0, w, h / 3, 0x28001020, 0x00000000);

        // Title
        g.centeredText(this.font, Component.literal("§b§lMUTO"), midX, midY - 56, CYAN);
        g.centeredText(this.font, Component.literal("§8RELOAD ENGINE"), midX, midY - 42, 0xFF4A5568);

        // Separator
        int sep = midX - 120;
        g.fill(sep, midY - 28, sep + 240, midY - 27, 0xFF2D3748);

        // Stage label with animated dots
        ReloadResult res = result.get();
        boolean failed = res != null && !res.success();
        String dots = ".".repeat(dotCount);
        String label = stageMsg + (failed ? "" : dots);
        int stageColor = failed ? RED : DIM;
        g.centeredText(this.font, Component.literal(label), midX, midY - 14, stageColor);

        // Progress bar track
        int barW = 260;
        int barH = 5;
        int barX = midX - barW / 2;
        int barY = midY + 6;

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF2D3748);
        g.fill(barX, barY, barX + barW, barY + barH, DARK);

        // Fill — glow effect via second slightly wider bright fill
        float clampedProg = Math.min(1.0f, displayProg);
        int fillW = (int) (barW * clampedProg);
        if (fillW > 0) {
            int fillColor = failed ? RED : CYAN;
            g.fill(barX, barY, barX + fillW, barY + barH, fillColor);
            // inner bright stripe (glow line)
            if (!failed && fillW > 2) {
                g.fill(barX, barY, barX + fillW, barY + 1, 0x80FFFFFF);
            }
        }

        // Percentage
        int pct = (int) (clampedProg * 100);
        g.centeredText(this.font, Component.literal("§b" + pct + "%"), midX, midY + 20, CYAN);

        // Elapsed time
        long elapsed = System.currentTimeMillis() - startMs;
        g.centeredText(this.font, Component.literal("§8" + (elapsed / 1000) + "s"), midX, midY + 34, 0xFF4A5568);

        // Mod diff summary if finished
        if (res != null && res.diff() != null && res.diff().hasChanges()) {
            int dy = midY + 46;
            String summary = "§a+" + res.diff().added().size()
                    + " §c-" + res.diff().removed().size()
                    + " §e~" + res.diff().updated().size();
            g.centeredText(this.font, Component.literal(summary), midX, dy, 0xFFFFFFFF);
        }

        // Runtime capability hint if no JBR
        if (!MutoMod.isJbr()) {
            g.centeredText(this.font,
                    Component.literal("§8Tip: use JetBrains Runtime for deeper Tier 1 hotswap"),
                    midX, h - 18, 0xFF4A5568
            );
        }
    }
}
