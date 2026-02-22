package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.shop.StockManager;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ShopInteractListener implements Listener {

    private final ChestMarketPlus plugin;

    public ShopInteractListener(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        Shop shop = null;

        if (block.getState() instanceof Chest) {
            shop = plugin.getShopManager().getShopByLocation(block.getLocation());
        } else if (block.getState() instanceof Sign) {
            shop = plugin.getShopManager().getShopBySignLocation(block.getLocation());
        }

        if (shop == null) return;

        if (block.getState() instanceof Chest) {
            if (isOwnerOrTrusted(player, shop)) {
                return;
            }

            if (player.hasPermission(plugin.getConfigManager().getSettings().getAdminBypassPermission())) {
                return;
            }

            event.setCancelled(true);
        }

        try {
            if (plugin.getDatabaseManager().getPlayerDataRepository().isFrozen(player.getUniqueId())) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("player-frozen"));
                return;
            }
        } catch (Exception e) {
        }

        StockManager.updateStock(shop);

        plugin.getGuiManager().openShopGui(player, shop);
    }

    private boolean isOwnerOrTrusted(Player player, Shop shop) {
        if (player.getUniqueId().equals(shop.getOwnerUuid())) return true;

        try {
            return plugin.getDatabaseManager().getPlayerDataRepository()
                    .isTrusted(shop.getId(), player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }
}
