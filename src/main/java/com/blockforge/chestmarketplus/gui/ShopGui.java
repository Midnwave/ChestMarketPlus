package com.blockforge.chestmarketplus.gui;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.api.ShopTransaction;
import com.blockforge.chestmarketplus.api.ShopType;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.shop.StockManager;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGui {

    private final ChestMarketPlus plugin;
    private final Player player;
    private final Shop shop;
    private Inventory inventory;

    public ShopGui(ChestMarketPlus plugin, Player player, Shop shop) {
        this.plugin = plugin;
        this.player = player;
        this.shop = shop;
    }

    public void open() {
        Settings settings = plugin.getConfigManager().getSettings();
        String title = MessageUtils.colorize("<dark_gray>" + shop.getOwnerName() + "'s Shop");
        inventory = Bukkit.createInventory(null, 27, title);

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        ItemStack displayItem = shop.getItemTemplate().clone();
        ItemMeta meta = displayItem.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (meta.hasLore()) {
            lore.addAll(meta.getLore());
            lore.add("");
        }
        lore.add(MessageUtils.colorize("<gray>Owner: <white>" + shop.getOwnerName()));
        lore.add(MessageUtils.colorize("<gray>Stock: <white>" + (shop.isAdmin() ? "Unlimited" : shop.getCurrentStock())));
        if (shop.getBuyPrice() != null)
            lore.add(MessageUtils.colorize("<gray>Buy Price: <green>" + settings.formatPrice(shop.getBuyPrice())));
        if (shop.getSellPrice() != null)
            lore.add(MessageUtils.colorize("<gray>Sell Price: <red>" + settings.formatPrice(shop.getSellPrice())));
        meta.setLore(lore);
        displayItem.setItemMeta(meta);
        inventory.setItem(13, displayItem);

        if (shop.getShopType().canBuy()) {
            ItemStack buyBtn = createItem(Material.LIME_STAINED_GLASS_PANE,
                    MessageUtils.colorize("<green><bold>BUY"),
                    MessageUtils.colorize("<gray>Price: <green>" + settings.formatPrice(shop.getBuyPrice())),
                    MessageUtils.colorize("<gray>Left-click: Buy 1"),
                    MessageUtils.colorize("<gray>Shift-click: Buy Stack"),
                    MessageUtils.colorize("<gray>Sneak+Right: Choose Amount"));
            inventory.setItem(11, buyBtn);
        }

        if (shop.getShopType().canSell()) {
            ItemStack sellBtn = createItem(Material.RED_STAINED_GLASS_PANE,
                    MessageUtils.colorize("<red><bold>SELL"),
                    MessageUtils.colorize("<gray>Price: <red>" + settings.formatPrice(shop.getSellPrice())),
                    MessageUtils.colorize("<gray>Left-click: Sell 1"),
                    MessageUtils.colorize("<gray>Shift-click: Sell All"),
                    MessageUtils.colorize("<gray>Sneak+Right: Choose Amount"));
            inventory.setItem(15, sellBtn);
        }

        ItemStack infoBtn = createItem(Material.OAK_SIGN,
                MessageUtils.colorize("<yellow><bold>Shop Info"),
                MessageUtils.colorize("<gray>Type: <white>" + shop.getShopType().name()),
                MessageUtils.colorize("<gray>ID: <white>#" + shop.getId()));
        inventory.setItem(4, infoBtn);

        try {
            boolean isFav = plugin.getDatabaseManager().getPlayerDataRepository()
                    .isFavorite(player.getUniqueId(), shop.getId());
            ItemStack favBtn = createItem(isFav ? Material.GOLD_INGOT : Material.IRON_INGOT,
                    MessageUtils.colorize(isFav ? "<gold><bold>Unfavorite" : "<gray>Favorite"),
                    MessageUtils.colorize(isFav ? "<yellow>Click to remove from favorites" : "<gray>Click to add to favorites"));
            inventory.setItem(22, favBtn);
        } catch (Exception ignored) {}

        player.openInventory(inventory);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();
        Settings settings = plugin.getConfigManager().getSettings();

        switch (slot) {
            case 11 -> { // Buy
                if (!shop.getShopType().canBuy()) return;
                if (event.isShiftClick()) {
                    processBuy(player, shop.getItemTemplate().getMaxStackSize());
                } else if (player.isSneaking() && event.isRightClick()) {
                    player.closeInventory();
                    plugin.getGuiManager().openQuantitySelector(player, shop, true, (p, qty) -> processBuy(p, qty));
                } else {
                    processBuy(player, 1);
                }
            }
            case 15 -> { // Sell
                if (!shop.getShopType().canSell()) return;
                if (event.isShiftClick()) {
                    // Sell all matching items
                    int count = ItemUtils.countMatchingItems(player.getInventory(), shop.getItemTemplate());
                    if (count > 0) processSell(player, count);
                } else if (player.isSneaking() && event.isRightClick()) {
                    player.closeInventory();
                    plugin.getGuiManager().openQuantitySelector(player, shop, false, (p, qty) -> processSell(p, qty));
                } else {
                    processSell(player, 1);
                }
            }
            case 22 -> { // Favorite toggle
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
                    player.closeInventory();
                    plugin.getGuiManager().openShopGui(player, shop); // Refresh
                } catch (Exception ignored) {}
            }
        }
    }

    private void processBuy(Player player, int quantity) {
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

        int finalQuantity = quantity;
        Runnable onConfirm = () -> executeBuy(player, finalQuantity, totalPrice, tax);
        Runnable onCancel = () -> MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled"));

        player.closeInventory();

        if (plugin.getPlatformDetector().hasDialogAPI()) {
            plugin.getGuiManager().handleClose(player);
            try {
                var dialogProvider = new com.blockforge.chestmarketplus.dialog.PaperDialogProvider(plugin);
                dialogProvider.showBuyConfirmation(player, shop, finalQuantity, onConfirm, onCancel);
            } catch (Exception e) {
                plugin.getGuiManager().openConfirmationGui(player,
                        "Confirm Purchase", shop, finalQuantity, onConfirm, onCancel);
            }
        } else {
            plugin.getGuiManager().openConfirmationGui(player,
                    "Confirm Purchase", shop, finalQuantity, onConfirm, onCancel);
        }
    }

    private void executeBuy(Player player, int quantity, double totalPrice, double tax) {
        Settings settings = plugin.getConfigManager().getSettings();

        if (!plugin.getEconomyProvider().withdraw(player, totalPrice)) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-failed"));
            return;
        }

        double ownerPayment = totalPrice - tax;
        if (!shop.isAdmin()) {
            plugin.getEconomyProvider().deposit(
                    Bukkit.getOfflinePlayer(shop.getOwnerUuid()), ownerPayment);

            var inv = StockManager.getShopInventory(shop);
            if (inv != null) {
                ItemUtils.removeMatchingItems(inv, shop.getItemTemplate(), quantity);
            }
        }

        ItemUtils.addItems(player.getInventory(), shop.getItemTemplate(), quantity);

        StockManager.updateStock(shop);
        plugin.getDisplayManager().updateDisplay(shop);

        try {
            ShopTransaction tx = new ShopTransaction(shop.getId(), player.getUniqueId(), player.getName(),
                    "BUY", shop.getItemTemplate().getType().name(), quantity, totalPrice, tax);
            plugin.getDatabaseManager().getTransactionRepository().logTransaction(tx);
        } catch (Exception ignored) {}

        try {
            Sound sound = Sound.valueOf(settings.getBuySoundName());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
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
                "{quantity}", String.valueOf(quantity),
                "{item}", itemName,
                "{price}", settings.formatPrice(totalPrice)));

        plugin.getNotificationManager().notifyOwner(shop, player, "BUY", quantity, totalPrice);
    }

    private void processSell(Player player, int quantity) {
        Settings settings = plugin.getConfigManager().getSettings();

        int available = ItemUtils.countMatchingItems(player.getInventory(), shop.getItemTemplate());
        if (available < quantity) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("not-enough-items",
                    "{item}", ItemUtils.getDisplayName(shop.getItemTemplate())));
            return;
        }

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

        if (!shop.isAdmin() && !plugin.getEconomyProvider().has(
                Bukkit.getOfflinePlayer(shop.getOwnerUuid()), totalPrice)) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("owner-no-funds"));
            return;
        }

        int finalQuantity = quantity;
        Runnable onConfirm = () -> executeSell(player, finalQuantity, totalPrice, tax, playerReceives);
        Runnable onCancel = () -> MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled"));

        player.closeInventory();

        if (plugin.getPlatformDetector().hasDialogAPI()) {
            plugin.getGuiManager().handleClose(player);
            try {
                var dialogProvider = new com.blockforge.chestmarketplus.dialog.PaperDialogProvider(plugin);
                dialogProvider.showSellConfirmation(player, shop, finalQuantity, onConfirm, onCancel);
            } catch (Exception e) {
                plugin.getGuiManager().openConfirmationGui(player,
                        "Confirm Sale", shop, finalQuantity, onConfirm, onCancel);
            }
        } else {
            plugin.getGuiManager().openConfirmationGui(player,
                    "Confirm Sale", shop, finalQuantity, onConfirm, onCancel);
        }
    }

    private void executeSell(Player player, int quantity, double totalPrice, double tax, double playerReceives) {
        Settings settings = plugin.getConfigManager().getSettings();

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
            if (inv != null) {
                ItemUtils.addItems(inv, shop.getItemTemplate(), quantity);
            }
        }

        StockManager.updateStock(shop);
        plugin.getDisplayManager().updateDisplay(shop);

        try {
            ShopTransaction tx = new ShopTransaction(shop.getId(), player.getUniqueId(), player.getName(),
                    "SELL", shop.getItemTemplate().getType().name(), quantity, totalPrice, tax);
            plugin.getDatabaseManager().getTransactionRepository().logTransaction(tx);
        } catch (Exception ignored) {}

        try {
            Sound sound = Sound.valueOf(settings.getSellSoundName());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
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
                "{quantity}", String.valueOf(quantity),
                "{item}", itemName,
                "{price}", settings.formatPrice(playerReceives)));

        plugin.getNotificationManager().notifyOwner(shop, player, "SELL", quantity, totalPrice);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(List.of(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
}
