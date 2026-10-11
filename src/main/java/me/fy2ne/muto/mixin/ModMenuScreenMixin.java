package me.fy2ne.muto.mixin;

import me.fy2ne.muto.client.gui.MutoModChangesScreen;
import me.fy2ne.muto.client.gui.MutoModFilter;
import me.fy2ne.muto.engine.ModDependencyAnalyzer;
import me.fy2ne.muto.mods.DisabledModsRegistry;
import me.fy2ne.muto.mods.ModStateManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.gui.ModsScreen")
public abstract class ModMenuScreenMixin extends Screen {
    protected ModMenuScreenMixin(Component title) {
        super(title);
    }

    private static final Identifier SPRITE_DISABLE = Identifier.fromNamespaceAndPath("muto", "icon/disable");
    private static final Identifier SPRITE_ENABLE = Identifier.fromNamespaceAndPath("muto", "icon/enable");
    private static final Identifier SPRITE_LOAD = Identifier.fromNamespaceAndPath("muto", "icon/load");
    private static final Identifier SPRITE_REVERT = Identifier.fromNamespaceAndPath("muto", "icon/revert");

    private static final Identifier SPRITE_FILTER_ALL = Identifier.fromNamespaceAndPath("muto", "icon/filter_all");
    private static final Identifier SPRITE_FILTER_ACTIVE = Identifier.fromNamespaceAndPath("muto", "icon/filter_active");
    private static final Identifier SPRITE_FILTER_DISABLED = Identifier.fromNamespaceAndPath("muto", "icon/filter_disabled");
    private static final Identifier SPRITE_FILTER_UNLOADED = Identifier.fromNamespaceAndPath("muto", "icon/filter_unloaded");

    @Shadow
    private com.terraformersmc.modmenu.gui.widget.entries.ModListEntry selected;

    @Shadow
    private com.terraformersmc.modmenu.gui.widget.ModListWidget modList;

    @Shadow
    private EditBox searchBox;

    @Shadow
    private SpriteIconButton filtersButton;

    @Shadow
    private SpriteIconButton configureButton;

    @Shadow
    public abstract boolean getModHasConfigScreen(String modId);

    // Action buttons (native SpriteIconButton)
    private SpriteIconButton muto$actionDisableBtn;
    private SpriteIconButton muto$actionEnableBtn;
    private SpriteIconButton muto$actionLoadBtn;
    private SpriteIconButton muto$actionRevertBtn;

    // Filter buttons (native SpriteIconButton)
    private SpriteIconButton muto$filterAllBtn;
    private SpriteIconButton muto$filterActiveBtn;
    private SpriteIconButton muto$filterDisabledBtn;
    private SpriteIconButton muto$filterUnloadedBtn;

    // Bottom save button
    private Button muto$saveChangesBtn;

    @Inject(method = "safelyOpenConfigScreen", at = @At("HEAD"), cancellable = true, require = 0)
    private void muto$blockConfigForDisabled(String modId, CallbackInfo ci) {
        if (modId != null && DisabledModsRegistry.isDisabled(modId)) {
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void muto$injectModMenuUi(CallbackInfo ci) {
        // 1. Filter toggle SpriteIconButtons placed next to ModMenu's filter funnel
        int fx = (filtersButton != null) ? filtersButton.getX() - 22 : 12;
        int fy = (filtersButton != null) ? filtersButton.getY() : 10;
        if (searchBox != null && filtersButton != null) {
            searchBox.setWidth(Math.max(40, searchBox.getWidth() - 22));
        }

        muto$filterAllBtn = SpriteIconButton.builder(Component.literal("Filter: All"), btn -> muto$cycleFilter(), true)
                .size(20, 20)
                .sprite(SPRITE_FILTER_ALL, 16, 16)
                .tooltip(Component.literal("Filter: All Mods (Click to cycle)"))
                .build();
        muto$filterAllBtn.setPosition(fx, fy);

        muto$filterActiveBtn = SpriteIconButton.builder(Component.literal("Filter: Active"), btn -> muto$cycleFilter(), true)
                .size(20, 20)
                .sprite(SPRITE_FILTER_ACTIVE, 16, 16)
                .tooltip(Component.literal("Filter: Active Mods (Click to cycle)"))
                .build();
        muto$filterActiveBtn.setPosition(fx, fy);

        muto$filterDisabledBtn = SpriteIconButton.builder(Component.literal("Filter: Disabled"), btn -> muto$cycleFilter(), true)
                .size(20, 20)
                .sprite(SPRITE_FILTER_DISABLED, 16, 16)
                .tooltip(Component.literal("Filter: Disabled Mods (Click to cycle)"))
                .build();
        muto$filterDisabledBtn.setPosition(fx, fy);

        muto$filterUnloadedBtn = SpriteIconButton.builder(Component.literal("Filter: Unloaded"), btn -> muto$cycleFilter(), true)
                .size(20, 20)
                .sprite(SPRITE_FILTER_UNLOADED, 16, 16)
                .tooltip(Component.literal("Filter: Unloaded Mods (Click to cycle)"))
                .build();
        muto$filterUnloadedBtn.setPosition(fx, fy);

        this.addRenderableWidget(muto$filterAllBtn);
        this.addRenderableWidget(muto$filterActiveBtn);
        this.addRenderableWidget(muto$filterDisabledBtn);
        this.addRenderableWidget(muto$filterUnloadedBtn);
        muto$updateFilterButtons();

        // 2. Action SpriteIconButtons (Disable, Enable, Load, Revert)
        int rightEdgeX = (configureButton != null) ? configureButton.getX() : (this.width - 24);
        int ay = (configureButton != null) ? configureButton.getY() : 48;

        muto$actionDisableBtn = SpriteIconButton.builder(Component.literal("Disable Mod"), btn -> muto$triggerToggle(), true)
                .size(20, 20)
                .sprite(SPRITE_DISABLE, 16, 16)
                .tooltip(Component.literal("Disable Mod"))
                .build();
        muto$actionDisableBtn.setPosition(rightEdgeX - 22, ay);

        muto$actionEnableBtn = SpriteIconButton.builder(Component.literal("Enable Mod"), btn -> muto$triggerToggle(), true)
                .size(20, 20)
                .sprite(SPRITE_ENABLE, 16, 16)
                .tooltip(Component.literal("Enable Mod"))
                .build();
        muto$actionEnableBtn.setPosition(rightEdgeX - 22, ay);

        muto$actionLoadBtn = SpriteIconButton.builder(Component.literal("Load Mod"), btn -> muto$triggerToggle(), true)
                .size(20, 20)
                .sprite(SPRITE_LOAD, 16, 16)
                .tooltip(Component.literal("Load Mod"))
                .build();
        muto$actionLoadBtn.setPosition(rightEdgeX, ay);

        muto$actionRevertBtn = SpriteIconButton.builder(Component.literal("Revert"), btn -> muto$triggerToggle(), true)
                .size(20, 20)
                .sprite(SPRITE_REVERT, 16, 16)
                .tooltip(Component.literal("Pending Change (Click to revert)"))
                .build();
        muto$actionRevertBtn.setPosition(rightEdgeX - 22, ay);

        muto$actionDisableBtn.visible = false;
        muto$actionEnableBtn.visible = false;
        muto$actionLoadBtn.visible = false;
        muto$actionRevertBtn.visible = false;

        this.addRenderableWidget(muto$actionDisableBtn);
        this.addRenderableWidget(muto$actionEnableBtn);
        this.addRenderableWidget(muto$actionLoadBtn);
        this.addRenderableWidget(muto$actionRevertBtn);

        // 3. Bottom row: 3 equal buttons: Open Mods Folder (100), Save (100), Done (100)
        AbstractWidget folderBtn = null;
        AbstractWidget doneBtn = null;
        for (var child : this.children()) {
            if (child instanceof AbstractWidget w) {
                if (w != muto$filterAllBtn && w != muto$filterActiveBtn && w != muto$filterDisabledBtn && w != muto$filterUnloadedBtn &&
                    w != muto$actionDisableBtn && w != muto$actionEnableBtn && w != muto$actionLoadBtn && w != muto$actionRevertBtn) {
                    if (w.getY() >= this.height - 38 && w.getHeight() <= 24) {
                        String label = w.getMessage().getString().toLowerCase();
                        if (label.contains("done") || label.contains("terminer") || label.contains("fertig")) {
                            doneBtn = w;
                        } else if (label.contains("folder") || label.contains("dossier") || label.contains("ordner")) {
                            folderBtn = w;
                        }
                    }
                }
            }
        }

        muto$saveChangesBtn = Button.builder(Component.literal("Save"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreenAndShow(new MutoModChangesScreen(this, () -> {
                    if (this.minecraft != null) {
                        me.fy2ne.muto.engine.boundary.ReloadBoundaryManager.triggerReload(this.minecraft, this);
                    }
                }));
            }
        })
        .tooltip(Tooltip.create(Component.literal("Review staged mod changes and apply")))
        .build();

        int targetY = this.height - 28;
        if (folderBtn != null) targetY = folderBtn.getY();

        int btnW = 100;
        int x1 = this.width / 2 - 154;
        int x2 = this.width / 2 - 50;
        int x3 = this.width / 2 + 54;

        if (folderBtn != null) {
            folderBtn.setX(x1);
            folderBtn.setWidth(btnW);
            folderBtn.setY(targetY);
        }

        muto$saveChangesBtn.setX(x2);
        muto$saveChangesBtn.setWidth(btnW);
        muto$saveChangesBtn.setY(targetY);

        if (doneBtn != null) {
            doneBtn.setX(x3);
            doneBtn.setWidth(btnW);
            doneBtn.setY(targetY);
        }

        muto$updateSaveBtn();
        muto$updateActionButtons();

        this.addRenderableWidget(muto$saveChangesBtn);
    }

    private void muto$cycleFilter() {
        MutoModFilter.CURRENT = MutoModFilter.CURRENT.next();
        muto$updateFilterButtons();
        if (modList != null) {
            modList.reloadFilters();
        }
    }

    private void muto$triggerToggle() {
        if (this.selected != null && this.selected.getMod() != null) {
            String modId = this.selected.getMod().getId();
            if (!ModStateManager.INSTANCE.isProtectedMod(modId)) {
                ModStateManager.INSTANCE.stageToggle(modId);
                muto$updateActionButtons();
                muto$updateSaveBtn();
                if (modList != null) {
                    modList.reloadFilters();
                }
            }
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), require = 0)
    private void muto$onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        muto$updateSaveBtn();
        muto$updateActionButtons();
    }

    private void muto$updateFilterButtons() {
        if (muto$filterAllBtn == null) return;
        MutoModFilter f = MutoModFilter.CURRENT;
        muto$filterAllBtn.visible = (f == MutoModFilter.ALL);
        muto$filterActiveBtn.visible = (f == MutoModFilter.ACTIVE);
        muto$filterDisabledBtn.visible = (f == MutoModFilter.DISABLED);
        muto$filterUnloadedBtn.visible = (f == MutoModFilter.UNLOADED);
    }

    private void muto$updateSaveBtn() {
        if (muto$saveChangesBtn == null) return;
        int count = ModStateManager.INSTANCE.getStagedCount();
        if (count > 0) {
            muto$saveChangesBtn.setMessage(Component.literal("§aSave (" + count + ")"));
            muto$saveChangesBtn.active = true;
        } else {
            muto$saveChangesBtn.setMessage(Component.literal("Save"));
            muto$saveChangesBtn.active = false;
        }
    }

    private void muto$updateActionButtons() {
        if (muto$actionDisableBtn == null || muto$actionEnableBtn == null || muto$actionLoadBtn == null || muto$actionRevertBtn == null) return;

        if (this.selected == null || this.selected.getMod() == null) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = false;
            return;
        }

        String modId = this.selected.getMod().getId();
        if (modId == null) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = false;
            return;
        }

        boolean isProtected = ModStateManager.INSTANCE.isProtectedMod(modId);
        boolean isLoaded = FabricLoader.getInstance().getModContainer(modId).isPresent();
        boolean isDisabled = DisabledModsRegistry.isDisabled(modId);
        boolean isUnloaded = !isLoaded && !isDisabled;
        boolean isStaged = ModStateManager.INSTANCE.isStaged(modId);

        // Handle configure button for disabled mods: keep visible if mod supports config, but disable it with tooltip
        boolean supportsConfig = this.getModHasConfigScreen(modId);
        if (this.configureButton != null) {
            if (isUnloaded) {
                this.configureButton.visible = false;
            } else if (isDisabled || (isStaged && !ModStateManager.INSTANCE.getStagedState(modId))) {
                if (supportsConfig) {
                    this.configureButton.visible = true;
                    this.configureButton.active = false;
                    this.configureButton.setTooltip(Tooltip.create(Component.literal("Cannot configure disabled mod. Enable the mod first.")));
                } else {
                    this.configureButton.visible = false;
                }
            }
        }

        // Positioning: if configureButton is visible, position action button to its left (-22px)
        // If configureButton is NOT visible, place action button right on the edge in its place!
        int rightEdgeX = (this.configureButton != null) ? this.configureButton.getX() : (this.width - 24);
        int ay = (this.configureButton != null) ? this.configureButton.getY() : 48;
        boolean configShown = (this.configureButton != null && this.configureButton.visible);
        int ax = configShown ? (rightEdgeX - 22) : rightEdgeX;

        muto$actionDisableBtn.setPosition(ax, ay);
        muto$actionEnableBtn.setPosition(ax, ay);
        muto$actionLoadBtn.setPosition(ax, ay);
        muto$actionRevertBtn.setPosition(ax, ay);

        List<String> missing = ModDependencyAnalyzer.getMissing(modId);
        String missingNote = !missing.isEmpty() ? " [Missing: " + String.join(", ", missing) + "]" : "";

        if (isStaged) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = true;
            muto$actionRevertBtn.active = true;

            if (isUnloaded) {
                muto$actionRevertBtn.setTooltip(Tooltip.create(Component.literal("Pending: Load (Click to revert)" + missingNote)));
            } else {
                boolean stagedState = ModStateManager.INSTANCE.getStagedState(modId);
                muto$actionRevertBtn.setTooltip(Tooltip.create(Component.literal(
                        (stagedState ? "Pending: Enable (Click to revert)" : "Pending: Disable (Click to revert)") + missingNote
                )));
            }
        } else if (isUnloaded) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = true;
            muto$actionRevertBtn.visible = false;
            muto$actionLoadBtn.active = true;
            muto$actionLoadBtn.setTooltip(Tooltip.create(Component.literal("Load Mod (Inject into client)" + missingNote)));
        } else if (isDisabled) {
            muto$actionDisableBtn.visible = false;
            muto$actionEnableBtn.visible = true;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = false;
            muto$actionEnableBtn.active = true;
            muto$actionEnableBtn.setTooltip(Tooltip.create(Component.literal("Enable Mod" + missingNote)));
        } else {
            muto$actionDisableBtn.visible = true;
            muto$actionEnableBtn.visible = false;
            muto$actionLoadBtn.visible = false;
            muto$actionRevertBtn.visible = false;
            muto$actionDisableBtn.active = !isProtected;
            muto$actionDisableBtn.setTooltip(Tooltip.create(Component.literal(
                    isProtected ? "Protected Mod (Cannot be disabled)" : "Disable Mod"
            )));
        }
    }
}
