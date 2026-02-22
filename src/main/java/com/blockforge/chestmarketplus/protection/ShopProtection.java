package com.blockforge.chestmarketplus.protection;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ShopProtection {

    private final ChestMarketPlus plugin;

    public ShopProtection(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public boolean canAccessChest(Player player, Shop shop) {
        if (player.getUniqueId().equals(shop.getOwnerUuid())) return true;

        if (player.hasPermission(plugin.getConfigManager().getSettings().getAdminBypassPermission())) return true;

        try {
            return plugin.getDatabaseManager().getPlayerDataRepository()
                    .isTrusted(shop.getId(), player.getUniqueId());
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean canModifyShop(Player player, Shop shop) {
        return player.getUniqueId().equals(shop.getOwnerUuid())
                || player.hasPermission("chestmarket.admin.edit");
    }

    public boolean canDeleteShop(Player player, Shop shop) {
        return player.getUniqueId().equals(shop.getOwnerUuid())
                || player.hasPermission("chestmarket.admin.delete");
    }
}
