package me.fy2ne.muto.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import me.fy2ne.muto.MutoLog;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.network.chat.Component;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class MutoUpdateChecker implements UpdateChecker {
    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/muto/version?loaders=%5B%22fabric%22%5D";
    private static final String DOWNLOAD_URL = "https://modrinth.com/mod/muto";

    @Override
    public UpdateInfo checkForUpdates() {
        String currentVersionStr = FabricLoader.getInstance()
                .getModContainer("muto")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("0.2.0-beta");

        try {
            URI uri = URI.create(MODRINTH_API);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "fy2ne/muto/" + currentVersionStr + " (hello@fy2ne.me)");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            if (conn.getResponseCode() != 200) {
                return noUpdate();
            }

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonArray()) return noUpdate();
                JsonArray versions = root.getAsJsonArray();
                if (versions.isEmpty()) return noUpdate();

                JsonObject latestObj = versions.get(0).getAsJsonObject();
                String latestVersionStr = latestObj.get("version_number").getAsString();
                String versionType = latestObj.has("version_type") ? latestObj.get("version_type").getAsString() : "release";

                UpdateChannel channel = switch (versionType.toLowerCase(Locale.ROOT)) {
                    case "alpha" -> UpdateChannel.ALPHA;
                    case "beta" -> UpdateChannel.BETA;
                    default -> UpdateChannel.RELEASE;
                };

                if (isNewer(latestVersionStr, currentVersionStr)) {
                    MutoLog.info("newer muto version available: {} (current: {})", latestVersionStr, currentVersionStr);
                    return new UpdateInfo() {
                        @Override
                        public boolean isUpdateAvailable() {
                            return true;
                        }

                        @Override
                        public Component getUpdateMessage() {
                            return Component.literal("Muto " + latestVersionStr + " is available!");
                        }

                        @Override
                        public String getDownloadLink() {
                            return DOWNLOAD_URL;
                        }

                        @Override
                        public UpdateChannel getUpdateChannel() {
                            return channel;
                        }
                    };
                }
            }
        } catch (Exception ignored) {}

        return noUpdate();
    }

    private boolean isNewer(String remote, String current) {
        try {
            Version vRemote = SemanticVersion.parse(remote);
            Version vCurrent = SemanticVersion.parse(current);
            return vRemote.compareTo(vCurrent) > 0;
        } catch (VersionParsingException e) {
            return !remote.equalsIgnoreCase(current);
        }
    }

    private UpdateInfo noUpdate() {
        return new UpdateInfo() {
            @Override
            public boolean isUpdateAvailable() {
                return false;
            }

            @Override
            public String getDownloadLink() {
                return DOWNLOAD_URL;
            }

            @Override
            public UpdateChannel getUpdateChannel() {
                return UpdateChannel.RELEASE;
            }
        };
    }
}
