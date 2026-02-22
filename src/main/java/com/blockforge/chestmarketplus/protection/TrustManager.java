package com.blockforge.chestmarketplus.protection;

import com.blockforge.chestmarketplus.ChestMarketPlus;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TrustManager {

    private final ChestMarketPlus plugin;

    public TrustManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isTrusted(int shopId, UUID playerUuid) {
        try {
            return plugin.getDatabaseManager().getPlayerDataRepository().isTrusted(shopId, playerUuid);
        } catch (SQLException e) {
            return false;
        }
    }

    public void addTrusted(int shopId, UUID playerUuid) throws SQLException {
        plugin.getDatabaseManager().getPlayerDataRepository().addTrusted(shopId, playerUuid);
    }

    public void removeTrusted(int shopId, UUID playerUuid) throws SQLException {
        plugin.getDatabaseManager().getPlayerDataRepository().removeTrusted(shopId, playerUuid);
    }

    public List<UUID> getTrustedPlayers(int shopId) {
        try {
            return plugin.getDatabaseManager().getPlayerDataRepository().getTrustedPlayers(shopId);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
