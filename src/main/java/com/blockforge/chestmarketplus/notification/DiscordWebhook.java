package com.blockforge.chestmarketplus.notification;

import com.blockforge.chestmarketplus.ChestMarketPlus;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    private final ChestMarketPlus plugin;

    public DiscordWebhook(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void sendAdminEvent(String title, String description, int color) {
        String webhookUrl = plugin.getConfigManager().getSettings().getDiscordWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty()) return;
        if (!plugin.getConfigManager().getSettings().isDiscordAdminEvents()) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String json = """
                    {
                        "embeds": [{
                            "title": "%s",
                            "description": "%s",
                            "color": %d,
                            "footer": {"text": "ChestMarket+"},
                            "timestamp": "%s"
                        }]
                    }
                    """.formatted(
                        escapeJson(title),
                        escapeJson(description),
                        color,
                        java.time.Instant.now().toString()
                );

                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    plugin.getLogger().warning("Discord webhook returned status " + responseCode);
                }
                conn.disconnect();

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }

    public void sendShopCreated(String ownerName, String itemName, String location) {
        sendAdminEvent("Shop Created",
                "**Owner:** " + ownerName + "\n**Item:** " + itemName + "\n**Location:** " + location,
                0x00FF00);
    }

    public void sendShopDeleted(String ownerName, String itemName, String deletedBy) {
        sendAdminEvent("Shop Deleted",
                "**Owner:** " + ownerName + "\n**Item:** " + itemName + "\n**Deleted By:** " + deletedBy,
                0xFF0000);
    }

    public void sendPlayerFrozen(String playerName, String frozenBy) {
        sendAdminEvent("Player Frozen",
                "**Player:** " + playerName + "\n**Frozen By:** " + frozenBy,
                0xFFA500);
    }

    public void sendPlayerUnfrozen(String playerName, String unfrozenBy) {
        sendAdminEvent("Player Unfrozen",
                "**Player:** " + playerName + "\n**Unfrozen By:** " + unfrozenBy,
                0x00FF00);
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
