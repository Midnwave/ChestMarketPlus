package com.blockforge.chestmarketplus.shop;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.database.ShopRepository;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ShopManager {

    private final ChestMarketPlus plugin;
    private final Map<String, Shop> shopsByLocation = new ConcurrentHashMap<>();
    private final Map<String, Shop> shopsBySignLocation = new ConcurrentHashMap<>();
    private final Map<Integer, Shop> shopsById = new ConcurrentHashMap<>();

    public ShopManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void loadShops() {
        shopsByLocation.clear();
        shopsBySignLocation.clear();
        shopsById.clear();

        try {
            List<Shop> shops = getRepository().getAllActiveShops();
            for (Shop shop : shops) {
                indexShop(shop);
                StockManager.updateStock(shop);
            }
            plugin.getLogger().info("Loaded " + shops.size() + " active shops.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load shops from database", e);
        }
    }

    public Shop createShop(Shop shop) {
        try {
            shop = getRepository().createShop(shop);
            indexShop(shop);
            StockManager.updateStock(shop);
            return shop;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create shop", e);
            return null;
        }
    }

    public boolean deleteShop(int shopId) {
        Shop shop = shopsById.get(shopId);
        if (shop == null) return false;

        try {
            getRepository().deleteShop(shopId);
            removeIndex(shop);
            plugin.getDisplayManager().removeDisplay(shop);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete shop " + shopId, e);
            return false;
        }
    }

    public boolean updateShop(Shop shop) {
        try {
            getRepository().updateShop(shop);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update shop " + shop.getId(), e);
            return false;
        }
    }

    public void deactivateShop(Shop shop) {
        shop.setActive(false);
        updateShop(shop);
        removeIndex(shop);
        plugin.getDisplayManager().removeDisplay(shop);
    }

    public Shop getShopByLocation(org.bukkit.Location location) {
        String key = location.getWorld().getName() + ":" + location.getBlockX() + ":"
                + location.getBlockY() + ":" + location.getBlockZ();
        Shop shop = shopsByLocation.get(key);
        if (shop != null) return shop;

        // Check paired half of a double chest
        org.bukkit.Location paired = getPairedChestLocation(location);
        if (paired != null) {
            String pairedKey = paired.getWorld().getName() + ":" + paired.getBlockX() + ":"
                    + paired.getBlockY() + ":" + paired.getBlockZ();
            shop = shopsByLocation.get(pairedKey);
        }
        return shop;
    }

    public org.bukkit.Location getPairedChestLocation(org.bukkit.Location location) {
        if (location.getWorld() == null) return null;
        Block block = location.getBlock();
        if (!(block.getBlockData() instanceof Chest chestData)) return null;
        if (chestData.getType() == Chest.Type.SINGLE) return null;

        BlockFace pairedFace = switch (chestData.getFacing()) {
            case NORTH, SOUTH -> chestData.getType() == Chest.Type.LEFT ? BlockFace.WEST : BlockFace.EAST;
            case EAST, WEST -> chestData.getType() == Chest.Type.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
            default -> null;
        };
        if (pairedFace == null) return null;
        return block.getRelative(pairedFace).getLocation();
    }

    public Shop getShopBySignLocation(org.bukkit.Location location) {
        String key = location.getWorld().getName() + ":" + location.getBlockX() + ":"
                + location.getBlockY() + ":" + location.getBlockZ();
        return shopsBySignLocation.get(key);
    }

    public Shop getShopById(int id) {
        return shopsById.get(id);
    }

    public List<Shop> getShopsByOwner(UUID ownerUuid) {
        try {
            return getRepository().getShopsByOwner(ownerUuid);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get shops for " + ownerUuid, e);
            return Collections.emptyList();
        }
    }

    public int getShopCountByOwner(UUID ownerUuid) {
        try {
            return getRepository().getShopCountByOwner(ownerUuid);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to count shops for " + ownerUuid, e);
            return 0;
        }
    }

    public int getMaxShopsForPlayer(org.bukkit.entity.Player player) {
        // Check permission-based limits: chestmarket.limit.<number>
        for (int i = 1000; i >= 1; i--) {
            if (player.hasPermission("chestmarket.limit." + i)) {
                return i;
            }
        }
        return plugin.getConfigManager().getSettings().getDefaultMaxShops();
    }

    public int getShopCount() {
        return shopsById.size();
    }

    public Collection<Shop> getAllLoadedShops() {
        return Collections.unmodifiableCollection(shopsById.values());
    }

    private void indexShop(Shop shop) {
        String chestKey = shop.getWorld() + ":" + shop.getX() + ":" + shop.getY() + ":" + shop.getZ();
        String signKey = shop.getWorld() + ":" + shop.getSignX() + ":" + shop.getSignY() + ":" + shop.getSignZ();
        shopsByLocation.put(chestKey, shop);
        shopsBySignLocation.put(signKey, shop);
        shopsById.put(shop.getId(), shop);
    }

    private void removeIndex(Shop shop) {
        String chestKey = shop.getWorld() + ":" + shop.getX() + ":" + shop.getY() + ":" + shop.getZ();
        String signKey = shop.getWorld() + ":" + shop.getSignX() + ":" + shop.getSignY() + ":" + shop.getSignZ();
        shopsByLocation.remove(chestKey);
        shopsBySignLocation.remove(signKey);
        shopsById.remove(shop.getId());
    }

    private ShopRepository getRepository() {
        return plugin.getDatabaseManager().getShopRepository();
    }
}
