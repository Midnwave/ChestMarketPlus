package com.blockforge.chestmarketplus.shop;

import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.util.ItemUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;

public final class StockManager {

    private StockManager() {}

    public static void updateStock(Shop shop) {
        if (shop.isAdmin()) {
            shop.setCurrentStock(Integer.MAX_VALUE);
            return;
        }

        Inventory inv = getShopInventory(shop);
        if (inv == null) {
            shop.setCurrentStock(0);
            return;
        }

        shop.setCurrentStock(ItemUtils.countMatchingItems(inv, shop.getItemTemplate()));
    }

    public static Inventory getShopInventory(Shop shop) {
        Location loc = shop.getChestLocation();
        if (loc == null) return null;

        Block block = loc.getBlock();
        if (block.getState() instanceof Chest chest) {
            Inventory inv = chest.getInventory();
            // handle double chests by returning the full inventory
            if (inv.getHolder() instanceof DoubleChest doubleChest) {
                return doubleChest.getInventory();
            }
            return inv;
        }
        return null;
    }

    public static int getAvailableSpace(Shop shop) {
        if (shop.isAdmin()) return Integer.MAX_VALUE;

        Inventory inv = getShopInventory(shop);
        if (inv == null) return 0;

        int space = 0;
        for (org.bukkit.inventory.ItemStack item : inv.getContents()) {
            if (item == null || item.getType().isAir()) {
                space += shop.getItemTemplate().getMaxStackSize();
            } else if (ItemUtils.isSimilarExact(item, shop.getItemTemplate())) {
                space += shop.getItemTemplate().getMaxStackSize() - item.getAmount();
            }
        }
        return space;
    }
}
