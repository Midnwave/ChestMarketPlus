package com.blockforge.chestmarketplus.rating;

import com.blockforge.chestmarketplus.ChestMarketPlus;

import java.sql.SQLException;
import java.util.UUID;

public class RatingManager {

    private final ChestMarketPlus plugin;

    public RatingManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getSettings().isRatingsEnabled()
                && "thumbs".equalsIgnoreCase(plugin.getConfigManager().getSettings().getRatingsMode());
    }

    public void thumbsUp(UUID playerUuid, int shopId) throws SQLException {
        plugin.getDatabaseManager().getPlayerDataRepository().setRating(playerUuid, shopId, 1);
    }

    public void thumbsDown(UUID playerUuid, int shopId) throws SQLException {
        plugin.getDatabaseManager().getPlayerDataRepository().setRating(playerUuid, shopId, -1);
    }

    public int getThumbsUp(int shopId) throws SQLException {
        return plugin.getDatabaseManager().getPlayerDataRepository().getThumbsUp(shopId);
    }

    public int getThumbsDown(int shopId) throws SQLException {
        return plugin.getDatabaseManager().getPlayerDataRepository().getThumbsDown(shopId);
    }

    public Integer getPlayerRating(UUID playerUuid, int shopId) throws SQLException {
        return plugin.getDatabaseManager().getPlayerDataRepository().getPlayerRating(playerUuid, shopId);
    }
}
