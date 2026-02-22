package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.shop.StockManager;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public class ChestListener implements Listener {

    private final ChestMarketPlus plugin;

    public ChestListener(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Shop shop = findShopFromHolder(event.getInventory().getHolder());
        if (shop == null) return;

        if (!plugin.getConfigManager().getSettings().isChestProtection()) return;

        if (player.getUniqueId().equals(shop.getOwnerUuid())) return;

        if (player.hasPermission(plugin.getConfigManager().getSettings().getAdminBypassPermission())) return;

        try {
            if (plugin.getDatabaseManager().getPlayerDataRepository()
                    .isTrusted(shop.getId(), player.getUniqueId())) {
                return;
            }
        } catch (Exception ignored) {}

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Shop shop = findShopFromHolder(event.getInventory().getHolder());
        if (shop == null) return;

        int stockBefore = shop.getCurrentStock();
        StockManager.updateStock(shop);
        plugin.getDisplayManager().updateDisplay(shop);

        // Notify followers if stock increased (owner/trusted manually restocked)
        int stockAfter = shop.getCurrentStock();
        if (stockAfter > stockBefore && player.getUniqueId().equals(shop.getOwnerUuid())) {
            plugin.getNotificationManager().notifyFollowersRestock(shop);
        }
    }

    /**
     * Resolves the shop registered for a chest inventory holder.
     * Handles both single chests and double chests (tries both halves).
     */
    private Shop findShopFromHolder(InventoryHolder holder) {
        if (holder instanceof Chest chest) {
            return plugin.getShopManager().getShopByLocation(chest.getLocation());
        }
        if (holder instanceof DoubleChest dc) {
            // Try left side
            if (dc.getLeftSide() instanceof Chest left) {
                Shop shop = plugin.getShopManager().getShopByLocation(left.getLocation());
                if (shop != null) return shop;
            }
            // Try right side
            if (dc.getRightSide() instanceof Chest right) {
                return plugin.getShopManager().getShopByLocation(right.getLocation());
            }
        }
        return null;
    }
}
