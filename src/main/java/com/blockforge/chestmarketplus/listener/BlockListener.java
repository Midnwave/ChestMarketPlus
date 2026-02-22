package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class BlockListener implements Listener {

    private final ChestMarketPlus plugin;

    public BlockListener(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        Shop shop = null;
        boolean isSign = false;

        if (block.getState() instanceof Chest) {
            shop = plugin.getShopManager().getShopByLocation(block.getLocation());
        } else if (block.getState() instanceof Sign) {
            shop = plugin.getShopManager().getShopBySignLocation(block.getLocation());
            isSign = true;
        }

        if (shop == null) return;

        event.setCancelled(true);

        // When a break is cancelled the client visually removes the block but Bukkit does NOT
        // automatically resend the tile-entity (sign text) data. Force it next tick via both
        // sign.update() (all nearby players) and sendSignChange (the breaking player directly).
        if (isSign) {
            final Block signBlock = block;
            final Player signingPlayer = player;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (signBlock.getState() instanceof Sign sign) {
                    sign.update(true, false);
                    try {
                        signingPlayer.sendSignChange(sign.getLocation(), sign.getLines());
                    } catch (Exception ignored) {}
                }
            }, 3L);
        }

        if (!player.getUniqueId().equals(shop.getOwnerUuid())
                && !player.hasPermission("chestmarket.admin.delete")) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("cannot-break-shop"));
            return;
        }

        final Shop targetShop = shop;
        plugin.getGuiManager().openConfirmationGui(player,
                MessageUtils.colorize("&cDelete this shop?"), shop, 0,
                () -> {
                    plugin.getShopManager().deleteShop(targetShop.getId());
                    MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("shop-deleted"));
                },
                () -> MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled")));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        protectFromExplosion(event.blockList().iterator());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        protectFromExplosion(event.blockList().iterator());
    }

    private void protectFromExplosion(Iterator<Block> blockIterator) {
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            if (block.getState() instanceof Chest) {
                if (plugin.getShopManager().getShopByLocation(block.getLocation()) != null) {
                    blockIterator.remove();
                }
            } else if (block.getState() instanceof Sign) {
                if (plugin.getShopManager().getShopBySignLocation(block.getLocation()) != null) {
                    blockIterator.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent event) {
        Block block = event.getBlock();
        if (block.getState() instanceof Sign) {
            if (plugin.getShopManager().getShopBySignLocation(block.getLocation()) != null) {
                event.setCancelled(true);
            }
        }
    }
}
