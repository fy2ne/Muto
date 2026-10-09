package me.fy2ne.muto.client;

import com.mojang.blaze3d.platform.NativeImage;
import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.engine.ScannedMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ModIconManager {
    private static final Map<String, Identifier> ICON_CACHE = new ConcurrentHashMap<>();
    private static final Identifier FALLBACK_ICON = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/icon/language.png");

    public static Identifier getIcon(ScannedMod mod) {
        if (mod == null) return FALLBACK_ICON;
        return ICON_CACHE.computeIfAbsent(mod.id(), id -> loadIconTexture(mod));
    }

    private static Identifier loadIconTexture(ScannedMod mod) {
        if (!Files.exists(mod.jarPath())) return FALLBACK_ICON;

        try (JarFile jf = new JarFile(mod.jarPath().toFile())) {
            JarEntry entry = jf.getJarEntry("assets/" + mod.id() + "/icon.png");
            if (entry == null) {
                entry = jf.getJarEntry("icon.png");
            }
            if (entry == null) {
                var entries = jf.entries();
                while (entries.hasMoreElements()) {
                    var e = entries.nextElement();
                    String name = e.getName().toLowerCase();
                    if (name.endsWith("icon.png") && !e.isDirectory()) {
                        entry = e;
                        break;
                    }
                }
            }

            if (entry != null) {
                try (InputStream is = jf.getInputStream(entry)) {
                    NativeImage nativeImg = NativeImage.read(is);
                    DynamicTexture dynTex = new DynamicTexture(() -> "muto_mod_icon_" + mod.id(), nativeImg);
                    Identifier id = Identifier.fromNamespaceAndPath("muto", "dynamic_icon/" + mod.id().toLowerCase());
                    Minecraft.getInstance().getTextureManager().register(id, dynTex);
                    return id;
                }
            }
        } catch (Exception ex) {
            MutoLog.warn("failed to load icon for mod {}: {}", mod.id(), ex.getMessage());
        }

        return FALLBACK_ICON;
    }

    public static void clear() {
        ICON_CACHE.clear();
    }
}
