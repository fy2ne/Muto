package me.fy2ne.muto.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.config.MutoConfig;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public final class MutoUpdateChecker {
    public static final String CURRENT_VERSION = "0.1.0-beta.1";
    public static final String MODRINTH_PROJECT_URL = "https://modrinth.com/project/muto";
    private static final String API_URL = "https://api.modrinth.com/v2/project/muto/version";

    private static volatile boolean updateAvailable = false;
    private static volatile String latestVersion = "";
    private static volatile String updateUrl = MODRINTH_PROJECT_URL;

    private MutoUpdateChecker() {}

    public static void checkAsync() {
        CompletableFuture.runAsync(() -> {
            if (MutoConfig.get().simulateUpdateAvailable) {
                updateAvailable = true;
                latestVersion = "0.2.0-beta.1";
                MutoLog.info("simulated update active: v{}", latestVersion);
                return;
            }

            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(API_URL).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "fy2ne/muto/" + CURRENT_VERSION + " (contact@fy2ne.me)");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                        JsonElement parsed = JsonParser.parseReader(reader);
                        if (parsed.isJsonArray()) {
                            JsonArray array = parsed.getAsJsonArray();
                            if (!array.isEmpty()) {
                                JsonObject first = array.get(0).getAsJsonObject();
                                String remoteVer = first.has("version_number") ? first.get("version_number").getAsString() : "";
                                if (!remoteVer.isBlank() && isNewerVersion(remoteVer, CURRENT_VERSION)) {
                                    updateAvailable = true;
                                    latestVersion = remoteVer;
                                    MutoLog.info("new Muto version detected on Modrinth: v{}", remoteVer);
                                }
                            }
                        }
                    }
                } else {
                    MutoLog.info("modrinth version check responded with HTTP {}", code);
                }
            } catch (Exception ex) {
                MutoLog.info("modrinth version check skipped: {}", ex.getMessage());
            }
        });
    }

    public static boolean isUpdateAvailable() {
        if (MutoConfig.get().simulateUpdateAvailable) {
            return true;
        }
        return updateAvailable;
    }

    public static String getLatestVersion() {
        if (MutoConfig.get().simulateUpdateAvailable && latestVersion.isEmpty()) {
            return "0.2.0-beta.1";
        }
        return latestVersion;
    }

    public static String getUpdateUrl() {
        return updateUrl;
    }

    private static boolean isNewerVersion(String remote, String current) {
        if (remote == null || current == null) return false;
        String cleanRemote = remote.replaceAll("[^0-9.]", " ").trim();
        String cleanCur = current.replaceAll("[^0-9.]", " ").trim();
        if (cleanRemote.equals(cleanCur)) {
            return !remote.equals(current);
        }
        String[] rParts = cleanRemote.split("\\s+");
        String[] cParts = cleanCur.split("\\s+");
        int len = Math.max(rParts.length, cParts.length);
        for (int i = 0; i < len; i++) {
            int r = i < rParts.length ? parseOrZero(rParts[i]) : 0;
            int c = i < cParts.length ? parseOrZero(cParts[i]) : 0;
            if (r > c) return true;
            if (r < c) return false;
        }
        return false;
    }

    private static int parseOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
