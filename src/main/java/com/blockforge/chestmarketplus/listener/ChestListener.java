package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.shop.StockManager;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Chest chest)) return;

        Shop shop = plugin.getShopManager().getShopByLocation(chest.getLocation());
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
        if (!(event.getPlayer() instanceof Player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Chest chest)) return;

        Shop shop = plugin.getShopManager().getShopByLocation(chest.getLocation());
        if (shop == null) return;

        StockManager.updateStock(shop);
        plugin.getDisplayManager().updateDisplay(shop);
    }
}
