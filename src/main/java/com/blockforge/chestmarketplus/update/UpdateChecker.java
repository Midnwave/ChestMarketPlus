package com.blockforge.chestmarketplus.update;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.entity.Player;

public class UpdateChecker {

    private final ChestMarketPlus plugin;
    private String latestVersion = null;
    private boolean updateAvailable = false;

    public UpdateChecker(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        if (!plugin.getConfigManager().getSettings().isUpdateCheckerEnabled()) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getLogger().info("Update checker active. Running version: "
                    + plugin.getDescription().getVersion());
        });
    }

    public void notifyPlayer(Player player) {
        if (!updateAvailable || latestVersion == null) return;
        if (!plugin.getConfigManager().getSettings().isUpdateNotifyInGame()) return;

        MessageUtils.sendMessage(player,
                plugin.getLocaleManager().getPrefixedMessage("update-available",
                        "{version}", latestVersion));
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
