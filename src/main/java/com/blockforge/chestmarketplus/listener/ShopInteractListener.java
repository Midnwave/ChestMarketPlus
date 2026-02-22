package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.api.ShopType;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.shop.StockManager;
import com.blockforge.chestmarketplus.util.ItemUtils;
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
            plugin.getGuiManager().openShopGui(player, shop);
        } else if (type == ShopType.BUY) {
            startBuyFlow(player, shop);
        } else if (type == ShopType.SELL) {
            startSellFlow(player, shop);
        }
    }

    private void startBuyFlow(Player player, Shop shop) {
        Settings settings = plugin.getConfigManager().getSettings();
        StockManager.updateStock(shop);

        if (!shop.isAdmin() && shop.getCurrentStock() == 0) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("not-enough-stock",
                    "{stock}", "0"));
            return;
        }

        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        int maxQty = shop.isAdmin() ? 64 : shop.getCurrentStock();

        boolean allowAll = settings.isAllowAllQuantity();
        String allHint = allowAll ? ", or <white>all<gray>" : "";
        MessageUtils.sendMessage(player, MessageUtils.colorize(
                "<yellow>Buying: <white>" + itemName + " <gray>@ <green>"
                + settings.formatPrice(shop.getBuyPrice()) + " <gray>each | Stock: <white>" + maxQty
                + "\n<yellow>Enter quantity <gray>(1-" + maxQty + allHint + ") or type <white>cancel<gray>:"));

        plugin.getChatInputListener().awaitInput(player, input -> {
            if ("cancel".equalsIgnoreCase(input)) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled"));
                return;
            }
            int qty;
            if (allowAll && "all".equalsIgnoreCase(input)) {
                qty = maxQty;
            } else {
                try {
                    qty = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("invalid-price"));
                    return;
                }
            }
            if (qty <= 0 || qty > maxQty) {
                MessageUtils.sendMessage(player, MessageUtils.colorize(
                        "<red>Quantity must be between 1 and " + maxQty + "."));
                return;
            }
            executeBuyConfirm(player, shop, qty);
        });
    }

    private void executeBuyConfirm(Player player, Shop shop, int quantity) {
        Settings settings = plugin.getConfigManager().getSettings();
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

        Runnable onConfirm = () -> {
            StockManager.updateStock(shop);
            if (!shop.isAdmin() && shop.getCurrentStock() < quantity) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("not-enough-stock",
                        "{stock}", String.valueOf(shop.getCurrentStock())));
                return;
            }
            if (!plugin.getEconomyProvider().withdraw(player, totalPrice)) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-failed"));
                return;
            }
            double ownerPayment = totalPrice - tax;
            if (!shop.isAdmin()) {
                plugin.getEconomyProvider().deposit(
                        org.bukkit.Bukkit.getOfflinePlayer(shop.getOwnerUuid()), ownerPayment);
                var inv = StockManager.getShopInventory(shop);
                if (inv != null) ItemUtils.removeMatchingItemsWithShulkers(inv, shop.getItemTemplate(), quantity);
            }
            ItemUtils.addItems(player.getInventory(), shop.getItemTemplate(), quantity);
            StockManager.updateStock(shop);
            plugin.getDisplayManager().updateDisplay(shop);
            try {
                com.blockforge.chestmarketplus.api.ShopTransaction tx =
                        new com.blockforge.chestmarketplus.api.ShopTransaction(
                                shop.getId(), player.getUniqueId(), player.getName(),
                                "BUY", shop.getItemTemplate().getType().name(), quantity, totalPrice, tax);
                plugin.getDatabaseManager().getTransactionRepository().logTransaction(tx);
            } catch (Exception ignored) {}
            try {
                player.playSound(player.getLocation(),
                        org.bukkit.Sound.valueOf(settings.getBuySoundName()), 1f, 1f);
            } catch (Exception ignored) {}
            String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("shop-buy-success",
                    "{quantity}", String.valueOf(quantity), "{item}", itemName,
                    "{price}", settings.formatPrice(totalPrice)));
            plugin.getNotificationManager().notifyOwner(shop, player, "BUY", quantity, totalPrice);
        };

        Runnable onCancel = () -> MessageUtils.sendMessage(player,
                plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled"));

        plugin.getDialogProvider().showBuyConfirmation(player, shop, quantity, onConfirm, onCancel);
    }

    private void startSellFlow(Player player, Shop shop) {
        Settings settings = plugin.getConfigManager().getSettings();

        int available = ItemUtils.countMatchingItems(player.getInventory(), shop.getItemTemplate());
        if (available == 0) {
            String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("not-enough-items",
                    "{item}", itemName));
            return;
        }

        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        boolean allowAll = settings.isAllowAllQuantity();
        String allHint = allowAll ? ", or <white>all<gray>" : "";
        MessageUtils.sendMessage(player, MessageUtils.colorize(
                "<yellow>Selling: <white>" + itemName + " <gray>@ <red>"
                + settings.formatPrice(shop.getSellPrice()) + " <gray>each | You have: <white>" + available
                + "\n<yellow>Enter quantity <gray>(1-" + available + allHint + ") or type <white>cancel<gray>:"));

        plugin.getChatInputListener().awaitInput(player, input -> {
            if ("cancel".equalsIgnoreCase(input)) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled"));
                return;
            }
            int qty;
            if (allowAll && "all".equalsIgnoreCase(input)) {
                qty = available;
            } else {
                try {
                    qty = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("invalid-price"));
                    return;
                }
            }
            if (qty <= 0 || qty > available) {
                MessageUtils.sendMessage(player, MessageUtils.colorize(
                        "<red>Quantity must be between 1 and " + available + "."));
                return;
            }
            executeSellConfirm(player, shop, qty);
        });
    }

    private void executeSellConfirm(Player player, Shop shop, int quantity) {
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

        // Block only if partial-sell is disabled and owner can't pay
        if (!shop.isAdmin() && !settings.isPartialSellWhenLowFunds()
                && !plugin.getEconomyProvider().has(org.bukkit.Bukkit.getOfflinePlayer(shop.getOwnerUuid()), totalPrice)) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("owner-no-funds"));
            return;
        }

        Runnable onConfirm = () -> {
            // Re-check owner balance at execution time; apply partial payment if configured
            double actualTotal = totalPrice;
            double actualTax = tax;
            double actualReceives = playerReceives;
            if (!shop.isAdmin()) {
                double ownerBal = plugin.getEconomyProvider().getBalance(
                        org.bukkit.Bukkit.getOfflinePlayer(shop.getOwnerUuid()));
                if (ownerBal < actualTotal) {
                    if (!settings.isPartialSellWhenLowFunds()) {
                        MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("owner-no-funds"));
                        return;
                    }
                    actualTotal = ownerBal;
                    actualTax = player.hasPermission("chestmarket.bypass.tax") ? 0 : actualTotal * (settings.getTaxRate() / 100.0);
                    actualReceives = actualTotal - actualTax;
                }
            }

            int removed = ItemUtils.removeMatchingItems(player.getInventory(), shop.getItemTemplate(), quantity);
            if (removed < quantity) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-failed"));
                return;
            }
            plugin.getEconomyProvider().deposit(player, actualReceives);
            if (!shop.isAdmin()) {
                plugin.getEconomyProvider().withdraw(
                        org.bukkit.Bukkit.getOfflinePlayer(shop.getOwnerUuid()), actualTotal);
                var inv = StockManager.getShopInventory(shop);
                if (inv != null) ItemUtils.addItems(inv, shop.getItemTemplate(), quantity);
            }
            StockManager.updateStock(shop);
            plugin.getDisplayManager().updateDisplay(shop);
            try {
                com.blockforge.chestmarketplus.api.ShopTransaction tx =
                        new com.blockforge.chestmarketplus.api.ShopTransaction(
                                shop.getId(), player.getUniqueId(), player.getName(),
                                "SELL", shop.getItemTemplate().getType().name(), quantity, actualTotal, actualTax);
                plugin.getDatabaseManager().getTransactionRepository().logTransaction(tx);
            } catch (Exception ignored) {}
            try {
                player.playSound(player.getLocation(),
                        org.bukkit.Sound.valueOf(settings.getSellSoundName()), 1f, 1f);
            } catch (Exception ignored) {}
            String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("shop-sell-success",
                    "{quantity}", String.valueOf(quantity), "{item}", itemName,
                    "{price}", settings.formatPrice(actualReceives)));
            plugin.getNotificationManager().notifyOwner(shop, player, "SELL", quantity, actualTotal);
        };

        Runnable onCancel = () -> MessageUtils.sendMessage(player,
                plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled"));

        plugin.getDialogProvider().showSellConfirmation(player, shop, quantity, onConfirm, onCancel);
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
