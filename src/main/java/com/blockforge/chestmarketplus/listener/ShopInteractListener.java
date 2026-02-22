package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.api.ShopTransaction;
import com.blockforge.chestmarketplus.api.ShopType;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.shop.StockManager;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

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

        boolean isOwnerOrTrusted = isOwnerOrTrusted(player, shop);
        boolean isAdmin = player.hasPermission(plugin.getConfigManager().getSettings().getAdminBypassPermission());

        // Always cancel the interact event — prevents the chest or sign editor from opening
        // via the vanilla event pipeline. For chests, owners get access via explicit open below.
        event.setCancelled(true);

        // Owners/admins right-clicking a chest: explicitly open the chest inventory ourselves
        // so they can restock. Using player.openInventory() bypasses the cancelled event.
        if (action == Action.RIGHT_CLICK_BLOCK && block.getState() instanceof Chest chestState
                && (isOwnerOrTrusted || isAdmin)) {
            org.bukkit.inventory.Inventory inv = chestState.getInventory();
            if (inv.getHolder() instanceof org.bukkit.block.DoubleChest dc) {
                player.openInventory(dc.getInventory());
            } else {
                player.openInventory(inv);
            }
            return;
        }

        try {
            if (plugin.getDatabaseManager().getPlayerDataRepository().isFrozen(player.getUniqueId())) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("player-frozen"));
                return;
            }
        } catch (Exception ignored) {}

        StockManager.updateStock(shop);

        // Chest peek: non-owner sneak+right-clicks to view chest contents (read-only snapshot)
        if (action == Action.RIGHT_CLICK_BLOCK && player.isSneaking()
                && !isOwnerOrTrusted && !isAdmin && !shop.isAdmin()
                && plugin.getConfigManager().getSettings().isAllowChestPeek()) {
            plugin.getGuiManager().openChestPeekGui(player, shop);
            return;
        }

        ShopType type = shop.getShopType();

        if (type == ShopType.BUY_SELL) {
            handleBuySellShop(player, shop);
        } else if (type == ShopType.BUY) {
            startBuyFlow(player, shop);
        } else if (type == ShopType.SELL) {
            startSellFlow(player, shop);
        }
    }

    /**
     * Opens the full shop interaction flow for the given shop.
     * Can be called from FavoritesGui, admin commands, etc.
     */
    public void openShopInteraction(Player player, Shop shop) {
        StockManager.updateStock(shop);
        ShopType type = shop.getShopType();
        if (type == ShopType.BUY_SELL) {
            handleBuySellShop(player, shop);
        } else if (type == ShopType.BUY) {
            startBuyFlow(player, shop);
        } else if (type == ShopType.SELL) {
            startSellFlow(player, shop);
        }
    }

    // -------------------------------------------------------------------------
    // BUY_SELL shop: show the main shop dialog (Buy / Sell / Favorite buttons)
    // -------------------------------------------------------------------------

    private void handleBuySellShop(Player player, Shop shop) {
        plugin.getDialogProvider().showShopDialog(player, shop,
                () -> startBuyFlow(player, shop),
                () -> startSellFlow(player, shop),
                () -> {
                    toggleFavorite(player, shop);
                    // Re-open the shop dialog so the favorite state is refreshed
                    handleBuySellShop(player, shop);
                });
    }

    // -------------------------------------------------------------------------
    // Buy flow: quantity selection + confirmation via DialogProvider
    // -------------------------------------------------------------------------

    private void startBuyFlow(Player player, Shop shop) {
        StockManager.updateStock(shop);

        if (!shop.isAdmin() && shop.getCurrentStock() == 0) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("not-enough-stock",
                    "{stock}", "0"));
            return;
        }

        int maxQty = shop.isAdmin() ? 64 : shop.getCurrentStock();

        plugin.getDialogProvider().showBuyQuantityAndConfirm(player, shop, maxQty,
                (qty) -> executeBuyTransaction(player, shop, qty),
                () -> MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled")));
    }

    private void executeBuyTransaction(Player player, Shop shop, int quantity) {
        Settings settings = plugin.getConfigManager().getSettings();

        if (shop.getMaxQuantity() > 0 && quantity > shop.getMaxQuantity()) {
            quantity = shop.getMaxQuantity();
        }

        StockManager.updateStock(shop);
        if (!shop.isAdmin() && shop.getCurrentStock() < quantity) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("not-enough-stock",
                    "{stock}", String.valueOf(shop.getCurrentStock())));
            return;
        }

        double totalPrice = shop.getBuyPrice() * quantity;
        double tax = player.hasPermission("chestmarket.bypass.tax") ? 0 : totalPrice * (settings.getTaxRate() / 100.0);

        if (!plugin.getEconomyProvider().has(player, totalPrice)) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("not-enough-money",
                    "{price}", settings.formatPrice(totalPrice)));
            return;
        }

        if (!plugin.getEconomyProvider().withdraw(player, totalPrice)) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-failed"));
            return;
        }

        double ownerPayment = totalPrice - tax;
        if (!shop.isAdmin()) {
            plugin.getEconomyProvider().deposit(
                    Bukkit.getOfflinePlayer(shop.getOwnerUuid()), ownerPayment);
            var inv = StockManager.getShopInventory(shop);
            if (inv != null) ItemUtils.removeMatchingItemsWithShulkers(inv, shop.getItemTemplate(), quantity);
        }

        ItemUtils.addItems(player.getInventory(), shop.getItemTemplate(), quantity);

        StockManager.updateStock(shop);
        plugin.getDisplayManager().updateDisplay(shop);

        try {
            ShopTransaction tx = new ShopTransaction(
                    shop.getId(), player.getUniqueId(), player.getName(),
                    "BUY", shop.getItemTemplate().getType().name(), quantity, totalPrice, tax);
            plugin.getDatabaseManager().getTransactionRepository().logTransaction(tx);
        } catch (Exception ignored) {}

        try {
            player.playSound(player.getLocation(),
                    Sound.valueOf(settings.getBuySoundName()), 1f, 1f);
        } catch (Exception ignored) {}

        if (settings.isTransactionBurst() && shop.getChestLocation() != null) {
            try {
                var particleType = org.bukkit.Particle.valueOf(settings.getParticleTypeName());
                shop.getChestLocation().getWorld().spawnParticle(
                        particleType, shop.getChestLocation().clone().add(0.5, 1.5, 0.5),
                        settings.getParticleCount(), 0.3, 0.3, 0.3, 0);
            } catch (Exception ignored) {}
        }

        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("shop-buy-success",
                "{quantity}", String.valueOf(quantity), "{item}", itemName,
                "{price}", settings.formatPrice(totalPrice)));

        plugin.getNotificationManager().notifyOwner(shop, player, "BUY", quantity, totalPrice);
    }

    // -------------------------------------------------------------------------
    // Sell flow: quantity selection + confirmation via DialogProvider
    // -------------------------------------------------------------------------

    private void startSellFlow(Player player, Shop shop) {
        int available = ItemUtils.countMatchingItems(player.getInventory(), shop.getItemTemplate());
        if (available == 0) {
            String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("not-enough-items",
                    "{item}", itemName));
            return;
        }

        plugin.getDialogProvider().showSellQuantityAndConfirm(player, shop, available,
                (qty) -> executeSellTransaction(player, shop, qty),
                () -> MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled")));
    }

    private void executeSellTransaction(Player player, Shop shop, int quantity) {
        Settings settings = plugin.getConfigManager().getSettings();

        if (!shop.isAdmin()) {
            int space = StockManager.getAvailableSpace(shop);
            if (space < quantity) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("shop-full"));
                return;
            }
        }

        double totalPrice = shop.getSellPrice() * quantity;
        double tax = player.hasPermission("chestmarket.bypass.tax") ? 0 : totalPrice * (settings.getTaxRate() / 100.0);
        double playerReceives = totalPrice - tax;

        // Re-check owner balance at execution time; handle partial payment if enabled
        if (!shop.isAdmin()) {
            double ownerBal = plugin.getEconomyProvider().getBalance(
                    Bukkit.getOfflinePlayer(shop.getOwnerUuid()));
            if (ownerBal < totalPrice) {
                if (!settings.isPartialSellWhenLowFunds()) {
                    MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("owner-no-funds"));
                    return;
                }
                // Partial payment: pay what the owner actually has
                totalPrice = ownerBal;
                tax = player.hasPermission("chestmarket.bypass.tax") ? 0 : totalPrice * (settings.getTaxRate() / 100.0);
                playerReceives = totalPrice - tax;
            }
        }

        int removed = ItemUtils.removeMatchingItems(player.getInventory(), shop.getItemTemplate(), quantity);
        if (removed < quantity) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-failed"));
            return;
        }

        plugin.getEconomyProvider().deposit(player, playerReceives);

        if (!shop.isAdmin()) {
            plugin.getEconomyProvider().withdraw(
                    Bukkit.getOfflinePlayer(shop.getOwnerUuid()), totalPrice);
            var inv = StockManager.getShopInventory(shop);
            if (inv != null) ItemUtils.addItems(inv, shop.getItemTemplate(), quantity);
        }

        StockManager.updateStock(shop);
        plugin.getDisplayManager().updateDisplay(shop);

        try {
            ShopTransaction tx = new ShopTransaction(
                    shop.getId(), player.getUniqueId(), player.getName(),
                    "SELL", shop.getItemTemplate().getType().name(), quantity, totalPrice, tax);
            plugin.getDatabaseManager().getTransactionRepository().logTransaction(tx);
        } catch (Exception ignored) {}

        try {
            player.playSound(player.getLocation(),
                    Sound.valueOf(settings.getSellSoundName()), 1f, 1f);
        } catch (Exception ignored) {}

        if (settings.isTransactionBurst() && shop.getChestLocation() != null) {
            try {
                var particleType = org.bukkit.Particle.valueOf(settings.getParticleTypeName());
                shop.getChestLocation().getWorld().spawnParticle(
                        particleType, shop.getChestLocation().clone().add(0.5, 1.5, 0.5),
                        settings.getParticleCount(), 0.3, 0.3, 0.3, 0);
            } catch (Exception ignored) {}
        }

        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("shop-sell-success",
                "{quantity}", String.valueOf(quantity), "{item}", itemName,
                "{price}", settings.formatPrice(playerReceives)));

        plugin.getNotificationManager().notifyOwner(shop, player, "SELL", quantity, totalPrice);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void toggleFavorite(Player player, Shop shop) {
        try {
            boolean isFav = plugin.getDatabaseManager().getPlayerDataRepository()
                    .isFavorite(player.getUniqueId(), shop.getId());
            if (isFav) {
                plugin.getDatabaseManager().getPlayerDataRepository()
                        .removeFavorite(player.getUniqueId(), shop.getId());
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("favorite-removed"));
            } else {
                plugin.getDatabaseManager().getPlayerDataRepository()
                        .addFavorite(player.getUniqueId(), shop.getId());
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("favorite-added"));
            }
        } catch (Exception ignored) {}
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
