package com.blockforge.chestmarketplus.shop;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.util.MessageUtils;
import com.blockforge.chestmarketplus.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class ShopExpiry {

    private final ChestMarketPlus plugin;
    private BukkitTask expiryTask;

    public ShopExpiry(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void startExpiryTask() {
        if (!plugin.getConfigManager().getSettings().isExpiryEnabled()) return;

        // run every 5 minutes 6000 ticks
        expiryTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                checkExpiredShops();
                checkExpiringShops();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error in expiry check", e);
            }
        }, 6000L, 6000L);
    }

    public void stopExpiryTask() {
        if (expiryTask != null) {
            expiryTask.cancel();
            expiryTask = null;
        }
    }

    private void checkExpiredShops() throws SQLException {
        List<Shop> expired = plugin.getDatabaseManager().getShopRepository().getExpiredShops();
        for (Shop shop : expired) {
            if (plugin.getConfigManager().getSettings().isAutoDeleteExpired()) {
                // run on main thread since it involves world operations
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getShopManager().deleteShop(shop.getId());
                    plugin.getLogger().info("Auto-deleted expired shop #" + shop.getId()
                            + " owned by " + shop.getOwnerName());
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getShopManager().deactivateShop(shop);
                    plugin.getLogger().info("Deactivated expired shop #" + shop.getId()
                            + " owned by " + shop.getOwnerName());
                });
            }

            notifyExpiry(shop);
        }
    }

    private void checkExpiringShops() throws SQLException {
        int warnDays = plugin.getConfigManager().getSettings().getExpiryWarnDaysBefore();
        if (warnDays <= 0) return;

        List<Shop> expiring = plugin.getDatabaseManager().getShopRepository().getExpiringShops(warnDays);
        for (Shop shop : expiring) {
            Player owner = Bukkit.getPlayer(shop.getOwnerUuid());
            if (owner != null && owner.isOnline()) {
                String timeLeft = TimeUtils.formatDuration(shop.getTimeUntilExpiry());
                String msg = plugin.getLocaleManager().getPrefixedMessage("shop-expiring-warning",
                        "{time}", timeLeft, "{id}", String.valueOf(shop.getId()));
                Bukkit.getScheduler().runTask(plugin, () -> MessageUtils.sendMessage(owner, msg));
            }
        }
    }

    private void notifyExpiry(Shop shop) {
        Player owner = Bukkit.getPlayer(shop.getOwnerUuid());
        String msg = plugin.getLocaleManager().getMessage("shop-expired",
                "{id}", String.valueOf(shop.getId()));

        if (owner != null && owner.isOnline()) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    MessageUtils.sendMessage(owner, plugin.getLocaleManager().getPrefix() + msg));
        } else {
            // queue for offline delivery
            try {
                plugin.getDatabaseManager().getPlayerDataRepository()
                        .addPendingNotification(shop.getOwnerUuid(), msg);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to queue expiry notification", e);
            }
        }
    }
}
