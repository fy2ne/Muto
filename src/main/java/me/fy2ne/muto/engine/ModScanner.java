package me.fy2ne.muto.engine;

import me.fy2ne.muto.MutoLog;

import java.io.InputStream;
import java.nio.file.*;
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
        try (JarFile jf = new JarFile(jarPath.toFile())) {
            JarEntry fmj = jf.getJarEntry("fabric.mod.json");
            if (fmj == null) return null;

            String hash = sha256(jarPath);
            String modId;
            String version;
            boolean hasMixins;
            Map<String, List<String>> entrypoints = new LinkedHashMap<>();

            try (InputStream in = jf.getInputStream(fmj)) {
                String raw = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                modId = extractJsonString(raw, "id");
                version = extractJsonString(raw, "version");
                hasMixins = raw.contains("\"mixins\"") && !raw.contains("\"mixins\": []");
                parseEntrypoints(raw, entrypoints);
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
        } catch (Exception ex) {
            MutoLog.warn("skipping jar {}: {}", jarPath.getFileName(), ex.getMessage());
            return null;
        }
    }

    private static void parseEntrypoints(String raw, Map<String, List<String>> out) {
        int epStart = raw.indexOf("\"entrypoints\"");
        if (epStart < 0) return;

        int open = raw.indexOf('{', epStart);
        if (open < 0) return;
        int close = findMatchingBrace(raw, open);
        if (close <= open) return;

        String block = raw.substring(open + 1, close);
        int cur = 0;
        while (cur < block.length()) {
            int q1 = block.indexOf('"', cur);
            if (q1 < 0) break;
            int q2 = block.indexOf('"', q1 + 1);
            if (q2 < 0) break;

            String epKey = block.substring(q1 + 1, q2);
            int colon = block.indexOf(':', q2 + 1);
            if (colon < 0) break;

            int valStart = colon + 1;
            while (valStart < block.length() && Character.isWhitespace(block.charAt(valStart))) {
                valStart++;
            }

            if (valStart >= block.length()) break;
            char firstChar = block.charAt(valStart);

            List<String> classes = new ArrayList<>();
            int nextPos;

            if (firstChar == '[') {
                int bracketEnd = findMatchingBracket(block, valStart);
                if (bracketEnd > valStart) {
                    String arr = block.substring(valStart + 1, bracketEnd);
                    extractClassesFromArray(arr, classes);
                    nextPos = bracketEnd + 1;
                } else {
                    nextPos = valStart + 1;
                }
            } else if (firstChar == '"') {
                int endQuote = block.indexOf('"', valStart + 1);
                if (endQuote > valStart) {
                    classes.add(block.substring(valStart + 1, endQuote));
                    nextPos = endQuote + 1;
                } else {
                    nextPos = valStart + 1;
                }
            } else {
                nextPos = valStart + 1;
            }

            out.put(epKey, classes);
            cur = nextPos;
        }
    }

    private static void extractClassesFromArray(String arr, List<String> out) {
        int idx = 0;
        while (idx < arr.length()) {
            int q1 = arr.indexOf('"', idx);
            if (q1 < 0) break;
            int q2 = arr.indexOf('"', q1 + 1);
            if (q2 < 0) break;

            String token = arr.substring(q1 + 1, q2);
            if ("value".equals(token) || "adapter".equals(token)) {
                idx = q2 + 1;
                continue;
            }

            if (token.contains(".") && !token.endsWith(".json")) {
                out.add(token);
            }
            idx = q2 + 1;
        }
    }

    private static int findMatchingBracket(String s, int openPos) {
        int depth = 0;
        for (int i = openPos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buf = Files.readAllBytes(file);
        byte[] digest = md.digest(buf);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static String extractJsonString(String json, String key) {
        String needle = "\"" + key + "\"";
        int kIdx = json.indexOf(needle);
        if (kIdx < 0) return null;
        int colon = json.indexOf(':', kIdx + needle.length());
        if (colon < 0) return null;
        int qOpen = json.indexOf('"', colon + 1);
        if (qOpen < 0) return null;
        int qClose = json.indexOf('"', qOpen + 1);
        if (qClose < 0) return null;
        return json.substring(qOpen + 1, qClose);
    }

    static int findMatchingBrace(String s, int openPos) {
        if (openPos < 0 || openPos >= s.length()) return -1;
        int depth = 0;
        for (int i = openPos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }
}
