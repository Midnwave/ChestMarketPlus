package com.blockforge.chestmarketplus.shop;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.api.ShopType;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class ShopCreator {

    private final ChestMarketPlus plugin;

    public ShopCreator(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public CreateResult createFromCommand(Player player, ShopType type, double buyPrice, double sellPrice,
                                          Location chestLoc, Location signLoc) {
        return createShop(player, type, buyPrice, sellPrice, chestLoc, signLoc);
    }

    public CreateResult createFromSign(Player player, ShopType type, double buyPrice, double sellPrice,
                                       Location chestLoc, Location signLoc) {
        return createShop(player, type, buyPrice, sellPrice, chestLoc, signLoc);
    }

    private CreateResult createShop(Player player, ShopType type, double buyPrice, double sellPrice,
                                     Location chestLoc, Location signLoc) {
        Settings settings = plugin.getConfigManager().getSettings();

        if (!player.hasPermission("chestmarket.create")) {
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("no-permission"));
        }

        try {
            if (plugin.getDatabaseManager().getPlayerDataRepository().isFrozen(player.getUniqueId())) {
                return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("player-frozen"));
            }
        } catch (SQLException e) {
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("error-generic"));
        }

        if (!isWorldAllowed(chestLoc)) {
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("world-restricted"));
        }

        if (plugin.getHookManager().isWorldGuardAvailable()) {
            if (!plugin.getHookManager().getWorldGuardHook().canCreateShop(chestLoc)) {
                return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("region-restricted"));
            }
        }

        int current = plugin.getShopManager().getShopCountByOwner(player.getUniqueId());
        int max = plugin.getShopManager().getMaxShopsForPlayer(player);
        if (current >= max) {
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("shop-limit-reached",
                    "{current}", String.valueOf(current), "{max}", String.valueOf(max)));
        }

        if (plugin.getShopManager().getShopByLocation(chestLoc) != null) {
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("shop-already-exists"));
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("hold-item"));
        }

        if (ItemMatcher.isBlacklisted(itemInHand.getType(), settings.getItemBlacklist())
                && !player.hasPermission(settings.getBlacklistBypassPermission())) {
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("item-blacklisted"));
        }

        if (type.canBuy() && (buyPrice < settings.getGlobalMinPrice() || buyPrice > settings.getGlobalMaxPrice())) {
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("price-out-of-range",
                    "{min}", settings.formatPrice(settings.getGlobalMinPrice()),
                    "{max}", settings.formatPrice(settings.getGlobalMaxPrice())));
        }
        if (type.canSell() && (sellPrice < settings.getGlobalMinPrice() || sellPrice > settings.getGlobalMaxPrice())) {
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("price-out-of-range",
                    "{min}", settings.formatPrice(settings.getGlobalMinPrice()),
                    "{max}", settings.formatPrice(settings.getGlobalMaxPrice())));
        }

        double fee = settings.getCreationFee();
        if (fee > 0 && !player.hasPermission("chestmarket.bypass.fee")) {
            if (!plugin.getEconomyProvider().has(player, fee)) {
                return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("not-enough-money-fee",
                        "{fee}", settings.formatPrice(fee)));
            }
            plugin.getEconomyProvider().withdraw(player, fee);
        }

        ItemStack template = itemInHand.clone();
        template.setAmount(1);

        Shop shop = new Shop();
        shop.setOwnerUuid(player.getUniqueId());
        shop.setOwnerName(player.getName());
        shop.setWorld(chestLoc.getWorld().getName());
        shop.setX(chestLoc.getBlockX());
        shop.setY(chestLoc.getBlockY());
        shop.setZ(chestLoc.getBlockZ());
        shop.setSignX(signLoc.getBlockX());
        shop.setSignY(signLoc.getBlockY());
        shop.setSignZ(signLoc.getBlockZ());
        shop.setShopType(type);
        shop.setItemTemplate(template);
        shop.setBuyPrice(type.canBuy() ? buyPrice : null);
        shop.setSellPrice(type.canSell() ? sellPrice : null);
        shop.setMaxQuantity(0);
        shop.setAdmin(player.hasPermission("chestmarket.create.admin") && false);
        shop.setActive(true);
        shop.setCreatedAt(System.currentTimeMillis() / 1000);

        if (settings.isExpiryEnabled()) {
            long expiryTime = (System.currentTimeMillis() / 1000) + (settings.getExpiryDurationDays() * 86400L);
            shop.setExpiresAt(expiryTime);
        }

        Shop created = plugin.getShopManager().createShop(shop);
        if (created == null) {
            if (fee > 0 && !player.hasPermission("chestmarket.bypass.fee")) {
                plugin.getEconomyProvider().deposit(player, fee);
            }
            return CreateResult.error(plugin.getLocaleManager().getPrefixedMessage("error-generic"));
        }

        plugin.getDisplayManager().createDisplay(created);

        return CreateResult.success(created);
    }

    private boolean isWorldAllowed(Location location) {
        Settings settings = plugin.getConfigManager().getSettings();
        String worldName = location.getWorld().getName();
        String mode = settings.getWorldRestrictionMode();
        java.util.List<String> list = settings.getWorldRestrictionList();

        if (list == null || list.isEmpty()) return true;

        if ("whitelist".equalsIgnoreCase(mode)) {
            return list.stream().anyMatch(w -> w.equalsIgnoreCase(worldName));
        } else {
            return list.stream().noneMatch(w -> w.equalsIgnoreCase(worldName));
        }
    }

    public static class CreateResult {
        private final boolean success;
        private final Shop shop;
        private final String errorMessage;

        private CreateResult(boolean success, Shop shop, String errorMessage) {
            this.success = success;
            this.shop = shop;
            this.errorMessage = errorMessage;
        }

        public static CreateResult success(Shop shop) {
            return new CreateResult(true, shop, null);
        }

        public static CreateResult error(String message) {
            return new CreateResult(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public Shop getShop() { return shop; }
        public String getErrorMessage() { return errorMessage; }
    }
}
