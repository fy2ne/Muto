package me.fy2ne.muto.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.fy2ne.muto.MutoLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ModScanner {

    public static List<ScannedMod> scan(Path modsDir) {
        if (!Files.isDirectory(modsDir)) {
            MutoLog.warn("mods dir missing: {}", modsDir);
            return Collections.emptyList();
        }

        List<ScannedMod> found = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsDir, "*.jar")) {
            for (Path jar : stream) {
                ScannedMod mod = probe(jar);
                if (mod != null) found.add(mod);
            }
        } catch (Exception ex) {
            MutoLog.error("scan failed", ex);
        }

        MutoLog.info("scanned {} mod jars in {}", found.size(), modsDir);
        return found;
    }

    public static ScannedMod probe(Path jarPath) {
        int attempts = 0;
        while (attempts < 4) {
            try (JarFile jf = new JarFile(jarPath.toFile())) {
                JarEntry fmj = jf.getJarEntry("fabric.mod.json");
                if (fmj == null) return null;

                String hash = sha256(jarPath);
                String modId;
                String version;
                boolean hasMixins = false;
                Map<String, List<String>> entrypoints = new LinkedHashMap<>();

                try (InputStream in = jf.getInputStream(fmj);
                     InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!root.has("id")) return null;
                    modId = root.get("id").getAsString();
                    version = root.has("version") ? root.get("version").getAsString() : "?";

                    if (root.has("mixins")) {
                        JsonElement m = root.get("mixins");
                        if (m.isJsonArray()) {
                            hasMixins = !m.getAsJsonArray().isEmpty();
                        } else if (m.isJsonPrimitive()) {
                            hasMixins = true;
                        }
                    }

                    if (root.has("entrypoints") && root.get("entrypoints").isJsonObject()) {
                        JsonObject epObj = root.getAsJsonObject("entrypoints");
                        for (Map.Entry<String, JsonElement> entry : epObj.entrySet()) {
                            List<String> list = new ArrayList<>();
                            JsonElement el = entry.getValue();
                            if (el.isJsonArray()) {
                                for (JsonElement item : el.getAsJsonArray()) {
                                    if (item.isJsonPrimitive()) {
                                        list.add(item.getAsString());
                                    } else if (item.isJsonObject() && item.getAsJsonObject().has("value")) {
                                        list.add(item.getAsJsonObject().get("value").getAsString());
                                    }
                                }
                            } else if (el.isJsonPrimitive()) {
                                list.add(el.getAsString());
                            } else if (el.isJsonObject() && el.getAsJsonObject().has("value")) {
                                list.add(el.getAsJsonObject().get("value").getAsString());
                            }
                            entrypoints.put(entry.getKey(), list);
                        }
                    }
                }

                if (modId == null || modId.isEmpty()) return null;

                return new ScannedMod(
                        modId,
                        version != null ? version : "?",
                        jarPath,
                        hash,
                        hasMixins,
                        entrypoints,
                        Files.size(jarPath)
                );
            } catch (IOException ex) {
                attempts++;
                if (attempts < 4) {
                    try {
                        Thread.sleep(120);
                    } catch (InterruptedException ignored) {}
                    continue;
                }
                MutoLog.warn("skipping jar {}: {}", jarPath.getFileName(), ex.getMessage());
                return null;
            } catch (Exception ex) {
                MutoLog.warn("skipping jar {}: {}", jarPath.getFileName(), ex.getMessage());
                return null;
            }
        }
        return null;
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buf = Files.readAllBytes(file);
        byte[] digest = md.digest(buf);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
