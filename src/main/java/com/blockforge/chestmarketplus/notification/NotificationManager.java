package com.blockforge.chestmarketplus.notification;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class NotificationManager {

    private final ChestMarketPlus plugin;

    public NotificationManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void notifyOwner(Shop shop, Player buyer, String action, int quantity, double totalPrice) {
        if (shop.isAdmin()) return;

        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        String msg = plugin.getLocaleManager().getMessage("owner-notification",
                "{player}", buyer.getName(),
                "{quantity}", String.valueOf(quantity),
                "{item}", itemName,
                "{price}", plugin.getConfigManager().getSettings().formatPrice(totalPrice),
                "{action}", action);

        Player owner = Bukkit.getPlayer(shop.getOwnerUuid());

        if (owner != null && owner.isOnline()) {
            try {
                if (plugin.getDatabaseManager().getPlayerDataRepository().isNotifyEnabled(shop.getOwnerUuid())) {
                    MessageUtils.sendActionBar(owner, plugin.getLocaleManager().getPrefix() + msg);
                }
            } catch (SQLException e) {
                MessageUtils.sendActionBar(owner, plugin.getLocaleManager().getPrefix() + msg);
            }
        } else {
            try {
                plugin.getDatabaseManager().getPlayerDataRepository()
                        .addPendingNotification(shop.getOwnerUuid(), msg);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to queue notification for " + shop.getOwnerName(), e);
            }
        }

        if ("SELL".equals(action)) {
            notifyFollowers(shop);
        }
    }

    public void notifyFollowersRestock(Shop shop) {
        notifyFollowers(shop);
    }

    private void notifyFollowers(Shop shop) {
        try {
            List<UUID> followers = plugin.getDatabaseManager().getPlayerDataRepository()
                    .getFollowers(shop.getId());

            String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
            String msg = plugin.getLocaleManager().getMessage("shop-restocked",
                    "{item}", itemName,
                    "{owner}", shop.getOwnerName());

            for (UUID followerUuid : followers) {
                Player follower = Bukkit.getPlayer(followerUuid);
                if (follower != null && follower.isOnline()) {
                    MessageUtils.sendMessage(follower, plugin.getLocaleManager().getPrefix() + msg);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to notify followers for shop #" + shop.getId(), e);
        }
    }

    public void saveQueuedNotifications() {
        plugin.getLogger().info("Notification queue persisted.");
    }
}
