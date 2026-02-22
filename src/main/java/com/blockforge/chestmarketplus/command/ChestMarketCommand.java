package com.blockforge.chestmarketplus.command;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.api.ShopType;
import com.blockforge.chestmarketplus.shop.ShopCreator;
import com.blockforge.chestmarketplus.shop.StockManager;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.LocationUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import com.blockforge.chestmarketplus.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ChestMarketCommand implements CommandExecutor, TabCompleter {

    private final ChestMarketPlus plugin;

    public ChestMarketCommand(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, 1);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> {
                int page = args.length > 1 ? parseIntOr(args[1], 1) : 1;
                sendHelp(sender, page);
            }
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender);
            case "setprice" -> handleSetPrice(sender, args);
            case "transfer" -> handleTransfer(sender, args);
            case "trust" -> handleTrust(sender, args);
            case "untrust" -> handleUntrust(sender, args);
            case "info" -> handleInfo(sender);
            case "log" -> handleLog(sender, args);
            case "favorites" -> handleFavorites(sender);
            case "follow" -> handleFollow(sender);
            case "unfollow" -> handleUnfollow(sender);
            case "notify" -> handleNotify(sender, args);
            case "holograms" -> handleHolograms(sender, args);
            case "setitem" -> handleSetItem(sender);
            case "admin" -> handleAdmin(sender, args);
            case "reload" -> handleReload(sender);
            case "config" -> handleConfig(sender);
            default -> sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("unknown-command"));
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-only"));
            return;
        }
        if (!player.hasPermission("chestmarket.create")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("usage-create"));
            return;
        }

        ShopType type = ShopType.fromString(args[1]);
        double buyPrice = 0, sellPrice = 0;

        try {
            if (type == ShopType.BUY || type == ShopType.BUY_SELL) {
                buyPrice = Double.parseDouble(args[2]);
            }
            if (type == ShopType.SELL) {
                sellPrice = Double.parseDouble(args[2]);
            }
            if (type == ShopType.BUY_SELL && args.length > 3) {
                sellPrice = Double.parseDouble(args[3]);
            }
        } catch (NumberFormatException e) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("invalid-price"));
            return;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof Chest)) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("look-at-chest"));
            return;
        }

        Location chestLoc = target.getLocation();
        Location signLoc = findAttachedSign(target);
        if (signLoc == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-sign-attached"));
            return;
        }

        ShopCreator creator = new ShopCreator(plugin);
        ShopCreator.CreateResult result = creator.createFromCommand(player, type, buyPrice, sellPrice, chestLoc, signLoc);

        if (result.isSuccess()) {
            Shop shop = result.getShop();
            updateSign(signLoc, shop);
            String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-created",
                    "{item}", itemName,
                    "{price}", shop.getBuyPrice() != null
                            ? plugin.getConfigManager().getSettings().formatPrice(shop.getBuyPrice())
                            : plugin.getConfigManager().getSettings().formatPrice(shop.getSellPrice())));
        } else {
            sendMessage(sender, result.getErrorMessage());
        }
    }

    private void handleDelete(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-only"));
            return;
        }

        Shop shop = getTargetShop(player);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-looking-at-shop"));
            return;
        }

        if (!player.getUniqueId().equals(shop.getOwnerUuid()) && !player.hasPermission("chestmarket.admin.delete")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-shop-owner"));
            return;
        }

        plugin.getShopManager().deleteShop(shop.getId());
        sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-deleted"));
    }

    private void handleSetPrice(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 3) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("usage-setprice"));
            return;
        }

        Shop shop = getTargetShop(player);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-looking-at-shop"));
            return;
        }
        if (!player.getUniqueId().equals(shop.getOwnerUuid())) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-shop-owner"));
            return;
        }

        String priceType = args[1].toLowerCase();
        double price;
        try {
            price = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("invalid-price"));
            return;
        }

        if ("buy".equals(priceType)) shop.setBuyPrice(price);
        else if ("sell".equals(priceType)) shop.setSellPrice(price);
        else {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("usage-setprice"));
            return;
        }

        plugin.getShopManager().updateShop(shop);
        plugin.getDisplayManager().updateDisplay(shop);
        sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("price-updated",
                "{type}", priceType, "{price}", plugin.getConfigManager().getSettings().formatPrice(price)));
    }

    private void handleTransfer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 2) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("usage-transfer"));
            return;
        }

        Shop shop = getTargetShop(player);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-looking-at-shop"));
            return;
        }
        if (!player.getUniqueId().equals(shop.getOwnerUuid())) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-shop-owner"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-not-found"));
            return;
        }

        shop.setOwnerUuid(target.getUniqueId());
        shop.setOwnerName(target.getName());
        plugin.getShopManager().updateShop(shop);
        plugin.getDisplayManager().updateDisplay(shop);
        sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-transferred",
                "{player}", target.getName()));
    }

    private void handleTrust(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 2) return;

        Shop shop = getTargetShop(player);
        if (shop == null || !player.getUniqueId().equals(shop.getOwnerUuid())) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-shop-owner"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-not-found"));
            return;
        }

        try {
            plugin.getDatabaseManager().getPlayerDataRepository().addTrusted(shop.getId(), target.getUniqueId());
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-trusted",
                    "{player}", target.getName()));
        } catch (SQLException e) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("error-generic"));
        }
    }

    private void handleUntrust(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 2) return;

        Shop shop = getTargetShop(player);
        if (shop == null || !player.getUniqueId().equals(shop.getOwnerUuid())) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-shop-owner"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-not-found"));
            return;
        }

        try {
            plugin.getDatabaseManager().getPlayerDataRepository().removeTrusted(shop.getId(), target.getUniqueId());
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-untrusted",
                    "{player}", target.getName()));
        } catch (SQLException e) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("error-generic"));
        }
    }

    private void handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) return;

        Shop shop = getTargetShop(player);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-looking-at-shop"));
            return;
        }

        StockManager.updateStock(shop);
        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        var s = plugin.getConfigManager().getSettings();

        sendMessage(sender, "&8&m                                    ");
        sendMessage(sender, "&6&lShop Info &7(#" + shop.getId() + ")");
        sendMessage(sender, "&7Owner: &f" + shop.getOwnerName());
        sendMessage(sender, "&7Item: &f" + itemName);
        sendMessage(sender, "&7Type: &f" + shop.getShopType().name());
        if (shop.getBuyPrice() != null) sendMessage(sender, "&7Buy Price: &a" + s.formatPrice(shop.getBuyPrice()));
        if (shop.getSellPrice() != null) sendMessage(sender, "&7Sell Price: &c" + s.formatPrice(shop.getSellPrice()));
        sendMessage(sender, "&7Stock: &f" + (shop.isAdmin() ? "Unlimited" : shop.getCurrentStock()));
        sendMessage(sender, "&7Admin Shop: &f" + (shop.isAdmin() ? "Yes" : "No"));
        if (shop.getExpiresAt() != null) {
            sendMessage(sender, "&7Expires in: &f" + TimeUtils.formatDuration(shop.getTimeUntilExpiry()));
        }
        sendMessage(sender, "&7Location: &f" + LocationUtils.formatLocation(shop.getChestLocation()));
        sendMessage(sender, "&8&m                                    ");
    }

    private void handleLog(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;

        Shop shop = getTargetShop(player);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-looking-at-shop"));
            return;
        }
        if (!player.getUniqueId().equals(shop.getOwnerUuid()) && !player.hasPermission("chestmarket.admin.info")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-shop-owner"));
            return;
        }

        int page = args.length > 1 ? parseIntOr(args[1], 1) - 1 : 0;
        plugin.getGuiManager().openTransactionLogGui(player, shop, page);
    }

    private void handleFavorites(CommandSender sender) {
        if (!(sender instanceof Player player)) return;
        plugin.getGuiManager().openFavoritesGui(player);
    }

    private void handleFollow(CommandSender sender) {
        if (!(sender instanceof Player player)) return;
        Shop shop = getTargetShop(player);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-looking-at-shop"));
            return;
        }
        try {
            plugin.getDatabaseManager().getPlayerDataRepository().addFollow(player.getUniqueId(), shop.getId());
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-followed"));
        } catch (SQLException e) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("error-generic"));
        }
    }

    private void handleUnfollow(CommandSender sender) {
        if (!(sender instanceof Player player)) return;
        Shop shop = getTargetShop(player);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-looking-at-shop"));
            return;
        }
        try {
            plugin.getDatabaseManager().getPlayerDataRepository().removeFollow(player.getUniqueId(), shop.getId());
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-unfollowed"));
        } catch (SQLException e) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("error-generic"));
        }
    }

    private void handleNotify(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        try {
            boolean current = plugin.getDatabaseManager().getPlayerDataRepository().isNotifyEnabled(player.getUniqueId());
            boolean newState = args.length > 1 ? "on".equalsIgnoreCase(args[1]) : !current;
            plugin.getDatabaseManager().getPlayerDataRepository().setNotify(player.getUniqueId(), newState);
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage(
                    newState ? "notifications-enabled" : "notifications-disabled"));
        } catch (SQLException e) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("error-generic"));
        }
    }

    private void handleHolograms(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        try {
            plugin.getDatabaseManager().getPlayerDataRepository().ensurePlayer(player.getUniqueId(), player.getName());
            boolean current = plugin.getDatabaseManager().getPlayerDataRepository().isHologramsEnabled(player.getUniqueId());
            boolean newState = args.length > 1 ? "on".equalsIgnoreCase(args[1]) : !current;
            plugin.getDatabaseManager().getPlayerDataRepository().setHolograms(player.getUniqueId(), newState);
            plugin.getDisplayManager().setHologramsVisibleForPlayer(player, newState);
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage(
                    newState ? "holograms-enabled" : "holograms-disabled"));
        } catch (SQLException e) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("error-generic"));
        }
    }

    private void handleSetItem(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-only"));
            return;
        }

        Shop shop = getTargetShop(player);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-looking-at-shop"));
            return;
        }

        if (!player.getUniqueId().equals(shop.getOwnerUuid())
                && !player.hasPermission("chestmarket.admin.edit")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("not-shop-owner"));
            return;
        }

        plugin.getGuiManager().openItemEditGui(player, shop);
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("usage-admin"));
            return;
        }

        String adminSub = args[1].toLowerCase();

        switch (adminSub) {
            case "delete" -> handleAdminDelete(sender, args);
            case "edit" -> handleAdminEdit(sender, args);
            case "setprice" -> handleAdminSetPrice(sender, args);
            case "list" -> handleAdminList(sender, args);
            case "freeze" -> handleAdminFreeze(sender, args, true);
            case "unfreeze" -> handleAdminFreeze(sender, args, false);
            case "tp" -> handleAdminTp(sender, args);
            case "forcerestock" -> handleAdminForceRestock(sender, args);
            case "info" -> handleAdminInfo(sender, args);
            case "stats" -> handleAdminStats(sender);
            default -> sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("unknown-command"));
        }
    }

    private void handleAdminDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chestmarket.admin.delete")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /cm admin delete <shopId>");
            return;
        }
        int shopId = parseIntOr(args[2], -1);
        if (shopId == -1 || plugin.getShopManager().getShopById(shopId) == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-not-found"));
            return;
        }
        plugin.getShopManager().deleteShop(shopId);
        sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("admin-shop-deleted", "{id}", String.valueOf(shopId)));
    }

    private void handleAdminEdit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (!sender.hasPermission("chestmarket.admin.edit")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /cm admin edit <shopId>");
            return;
        }
        int shopId = parseIntOr(args[2], -1);
        Shop shop = plugin.getShopManager().getShopById(shopId);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-not-found"));
            return;
        }
        plugin.getGuiManager().openShopGui(player, shop);
    }

    private void handleAdminSetPrice(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chestmarket.admin.setprice")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        if (args.length < 5) {
            sendMessage(sender, "&cUsage: /cm admin setprice <shopId> <buy|sell> <price>");
            return;
        }
        int shopId = parseIntOr(args[2], -1);
        Shop shop = plugin.getShopManager().getShopById(shopId);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-not-found"));
            return;
        }
        String type = args[3].toLowerCase();
        double price;
        try { price = Double.parseDouble(args[4]); }
        catch (NumberFormatException e) { sendMessage(sender, "&cInvalid price."); return; }

        if ("buy".equals(type)) shop.setBuyPrice(price);
        else if ("sell".equals(type)) shop.setSellPrice(price);
        plugin.getShopManager().updateShop(shop);
        plugin.getDisplayManager().updateDisplay(shop);
        sendMessage(sender, "&aPrice updated for shop #" + shopId);
    }

    private void handleAdminList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chestmarket.admin.list")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }

        if (args.length > 2) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-not-found"));
                return;
            }
            List<Shop> shops = plugin.getShopManager().getShopsByOwner(target.getUniqueId());
            sendMessage(sender, "&6Shops owned by " + target.getName() + " (" + shops.size() + "):");
            for (Shop s : shops) {
                sendMessage(sender, " &7#" + s.getId() + " &f" + ItemUtils.getDisplayName(s.getItemTemplate())
                        + " &7at " + LocationUtils.formatLocation(s.getChestLocation())
                        + (s.isActive() ? " &a[Active]" : " &c[Inactive]"));
            }
        } else {
            sendMessage(sender, "&6Total shops: &f" + plugin.getShopManager().getShopCount());
        }
    }

    private void handleAdminFreeze(CommandSender sender, String[] args, boolean freeze) {
        if (!sender.hasPermission("chestmarket.admin.freeze")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /cm admin " + (freeze ? "freeze" : "unfreeze") + " <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-not-found"));
            return;
        }
        try {
            plugin.getDatabaseManager().getPlayerDataRepository().ensurePlayer(target.getUniqueId(), target.getName());
            plugin.getDatabaseManager().getPlayerDataRepository().setFrozen(target.getUniqueId(), freeze);
            sendMessage(sender, "&a" + target.getName() + " has been " + (freeze ? "frozen" : "unfrozen") + ".");

            var webhook = new com.blockforge.chestmarketplus.notification.DiscordWebhook(plugin);
            if (freeze) webhook.sendPlayerFrozen(target.getName(), sender.getName());
            else webhook.sendPlayerUnfrozen(target.getName(), sender.getName());
        } catch (SQLException e) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("error-generic"));
        }
    }

    private void handleAdminTp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (!sender.hasPermission("chestmarket.admin.tp")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /cm admin tp <shopId>");
            return;
        }
        int shopId = parseIntOr(args[2], -1);
        Shop shop = plugin.getShopManager().getShopById(shopId);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-not-found"));
            return;
        }
        Location loc = shop.getChestLocation();
        if (loc != null) {
            player.teleport(loc.clone().add(0.5, 1, 0.5));
            sendMessage(sender, "&aTeleported to shop #" + shopId);
        }
    }

    private void handleAdminForceRestock(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chestmarket.admin.restock")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /cm admin forcerestock <shopId>");
            return;
        }
        int shopId = parseIntOr(args[2], -1);
        Shop shop = plugin.getShopManager().getShopById(shopId);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-not-found"));
            return;
        }

        var inv = StockManager.getShopInventory(shop);
        if (inv != null) {
            ItemUtils.addItems(inv, shop.getItemTemplate(), shop.getItemTemplate().getMaxStackSize() * 27);
            StockManager.updateStock(shop);
            plugin.getDisplayManager().updateDisplay(shop);
            sendMessage(sender, "&aForce restocked shop #" + shopId);
        }
    }

    private void handleAdminInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chestmarket.admin.info")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /cm admin info <shopId>");
            return;
        }
        int shopId = parseIntOr(args[2], -1);
        Shop shop = plugin.getShopManager().getShopById(shopId);
        if (shop == null) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("shop-not-found"));
            return;
        }
        StockManager.updateStock(shop);
        var s = plugin.getConfigManager().getSettings();
        sendMessage(sender, "&8&m                                    ");
        sendMessage(sender, "&6&lAdmin Shop Info &7(#" + shop.getId() + ")");
        sendMessage(sender, "&7Owner: &f" + shop.getOwnerName() + " &7(" + shop.getOwnerUuid() + ")");
        sendMessage(sender, "&7Item: &f" + ItemUtils.getDisplayName(shop.getItemTemplate()));
        sendMessage(sender, "&7Type: &f" + shop.getShopType().name());
        if (shop.getBuyPrice() != null) sendMessage(sender, "&7Buy: &a" + s.formatPrice(shop.getBuyPrice()));
        if (shop.getSellPrice() != null) sendMessage(sender, "&7Sell: &c" + s.formatPrice(shop.getSellPrice()));
        sendMessage(sender, "&7Stock: &f" + (shop.isAdmin() ? "Unlimited" : shop.getCurrentStock()));
        sendMessage(sender, "&7Admin: &f" + shop.isAdmin());
        sendMessage(sender, "&7Active: &f" + shop.isActive());
        sendMessage(sender, "&7Created: &f" + TimeUtils.formatTimestamp(shop.getCreatedAt()));
        if (shop.getExpiresAt() != null) sendMessage(sender, "&7Expires: &f" + TimeUtils.formatTimestamp(shop.getExpiresAt()));
        sendMessage(sender, "&7Location: &f" + LocationUtils.formatLocation(shop.getChestLocation()));
        try {
            int txCount = plugin.getDatabaseManager().getTransactionRepository().getTransactionCountByShop(shopId);
            double revenue = plugin.getDatabaseManager().getTransactionRepository().getTotalRevenueByShop(shopId);
            sendMessage(sender, "&7Total Transactions: &f" + txCount);
            sendMessage(sender, "&7Total Revenue: &f" + s.formatPrice(revenue));
        } catch (SQLException ignored) {}
        sendMessage(sender, "&8&m                                    ");
    }

    private void handleAdminStats(CommandSender sender) {
        if (!sender.hasPermission("chestmarket.admin.stats")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        var s = plugin.getConfigManager().getSettings();
        sendMessage(sender, "&8&m                                    ");
        sendMessage(sender, "&6&lServer Economy Stats");
        sendMessage(sender, "&7Total Active Shops: &f" + plugin.getShopManager().getShopCount());
        try {
            var txRepo = plugin.getDatabaseManager().getTransactionRepository();
            sendMessage(sender, "&7Total Transactions: &f" + txRepo.getTotalTransactionCount());
            sendMessage(sender, "&7Total Volume Traded: &f" + s.formatPrice(txRepo.getTotalVolumeTraded()));
            sendMessage(sender, "&7Total Tax Collected: &f" + s.formatPrice(txRepo.getTotalTaxCollected()));
        } catch (SQLException ignored) {}
        sendMessage(sender, "&8&m                                    ");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("chestmarket.admin.reload")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        plugin.reload();
        sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("config-reloaded"));
    }

    private void handleConfig(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("player-only"));
            return;
        }
        if (!sender.hasPermission("chestmarket.admin.config")) {
            sendMessage(sender, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            return;
        }
        plugin.getGuiManager().openConfigGui(player);
    }

    private void sendHelp(CommandSender sender, int page) {
        boolean isAdmin = hasAnyAdminPerm(sender);
        int maxPage = isAdmin ? 2 : 1;
        page = Math.max(1, Math.min(page, maxPage));

        if (page == 1) {
            sendMessage(sender, "&8&m------------------------------------");
            sendMessage(sender, "&6&lChestMarket+ &7Player Commands");
            sendMessage(sender, "&e/cm create <buy|sell|both> <price> &7- Create a shop");
            sendMessage(sender, "&e/cm delete &7- Delete your shop");
            sendMessage(sender, "&e/cm setprice <buy|sell> <price> &7- Update price");
            sendMessage(sender, "&e/cm transfer <player> &7- Transfer ownership");
            sendMessage(sender, "&e/cm trust / untrust <player> &7- Manage access");
            sendMessage(sender, "&e/cm info &7- View shop info");
            sendMessage(sender, "&e/cm log [page] &7- Transaction log");
            sendMessage(sender, "&e/cm favorites &7- Open favorites");
            sendMessage(sender, "&e/cm follow / unfollow &7- Follow a shop");
            sendMessage(sender, "&e/cm notify [on|off] &7- Toggle notifications");
            sendMessage(sender, "&e/cm holograms [on|off] &7- Toggle holograms");
            sendMessage(sender, "&e/cm setitem &7- Change your shop's item (look at shop)");
            if (isAdmin) sendMessage(sender, "&7Type &e/cm help 2 &7for admin commands");
            sendMessage(sender, "&8&m------------------------------------");
        } else if (page == 2 && isAdmin) {
            sendMessage(sender, "&8&m------------------------------------");
            sendMessage(sender, "&6&lChestMarket+ &7Admin Commands");
            sendMessage(sender, "&e/cm admin delete <shopId> &7- Force-delete shop");
            sendMessage(sender, "&e/cm admin edit <shopId> &7- Edit any shop");
            sendMessage(sender, "&e/cm admin setprice <id> <type> <price>");
            sendMessage(sender, "&e/cm admin list [player] &7- List shops");
            sendMessage(sender, "&e/cm admin freeze/unfreeze <player>");
            sendMessage(sender, "&e/cm admin tp <shopId> &7- Teleport to shop");
            sendMessage(sender, "&e/cm admin forcerestock <shopId>");
            sendMessage(sender, "&e/cm admin info <shopId> &7- Detailed shop info");
            sendMessage(sender, "&e/cm admin stats &7- Server economy stats");
            sendMessage(sender, "&e/cm reload &7- Reload configuration");
            sendMessage(sender, "&e/cm config &7- Open config GUI");
            sendMessage(sender, "&8&m------------------------------------");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("help", "create", "delete", "setprice", "transfer", "trust", "untrust",
                    "info", "log", "favorites", "follow", "unfollow", "notify", "holograms", "setitem"));
            if (hasAnyAdminPerm(sender)) completions.add("admin");
            if (sender.hasPermission("chestmarket.admin.reload")) completions.add("reload");
            if (sender.hasPermission("chestmarket.admin.config")) completions.add("config");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "create" -> completions.addAll(List.of("buy", "sell", "both"));
                case "setprice" -> completions.addAll(List.of("buy", "sell"));
                case "transfer", "trust", "untrust" -> completions.addAll(getOnlinePlayerNames());
                case "notify", "holograms" -> completions.addAll(List.of("on", "off"));
                case "help" -> {
                    completions.add("1");
                    if (hasAnyAdminPerm(sender)) completions.add("2");
                }
                case "log" -> completions.addAll(List.of("1", "2", "3"));
                case "admin" -> {
                    if (sender.hasPermission("chestmarket.admin.delete"))
                        completions.add("delete");
                    if (sender.hasPermission("chestmarket.admin.edit"))
                        completions.add("edit");
                    if (sender.hasPermission("chestmarket.admin.setprice"))
                        completions.add("setprice");
                    if (sender.hasPermission("chestmarket.admin.list"))
                        completions.add("list");
                    if (sender.hasPermission("chestmarket.admin.freeze"))
                        completions.addAll(List.of("freeze", "unfreeze"));
                    if (sender.hasPermission("chestmarket.admin.tp"))
                        completions.add("tp");
                    if (sender.hasPermission("chestmarket.admin.restock"))
                        completions.add("forcerestock");
                    if (sender.hasPermission("chestmarket.admin.info"))
                        completions.add("info");
                    if (sender.hasPermission("chestmarket.admin.stats"))
                        completions.add("stats");
                }
            }
        } else if (args.length == 3 && "admin".equalsIgnoreCase(args[0])) {
            switch (args[1].toLowerCase()) {
                case "delete", "edit", "tp", "forcerestock", "info", "setprice" -> {
                    for (Shop s : plugin.getShopManager().getAllLoadedShops()) {
                        completions.add(String.valueOf(s.getId()));
                    }
                }
                case "list", "freeze", "unfreeze" -> completions.addAll(getOnlinePlayerNames());
            }
        } else if (args.length == 3 && "create".equalsIgnoreCase(args[0])) {
            completions.addAll(List.of("1.00", "5.00", "10.00", "50.00", "100.00"));
        }

        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean hasAnyAdminPerm(CommandSender sender) {
        return sender.hasPermission("chestmarket.admin.delete")
                || sender.hasPermission("chestmarket.admin.edit")
                || sender.hasPermission("chestmarket.admin.setprice")
                || sender.hasPermission("chestmarket.admin.list")
                || sender.hasPermission("chestmarket.admin.freeze")
                || sender.hasPermission("chestmarket.admin.tp")
                || sender.hasPermission("chestmarket.admin.restock")
                || sender.hasPermission("chestmarket.admin.info")
                || sender.hasPermission("chestmarket.admin.stats")
                || sender.hasPermission("chestmarket.admin.reload")
                || sender.hasPermission("chestmarket.admin.config");
    }

    private Shop getTargetShop(Player player) {
        Block target = player.getTargetBlockExact(5);
        if (target == null) return null;

        if (target.getState() instanceof Chest) {
            return plugin.getShopManager().getShopByLocation(target.getLocation());
        }
        if (target.getState() instanceof Sign) {
            return plugin.getShopManager().getShopBySignLocation(target.getLocation());
        }
        return null;
    }

    private Location findAttachedSign(Block chestBlock) {
        org.bukkit.block.BlockFace[] faces = {
                org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH,
                org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST,
                org.bukkit.block.BlockFace.UP
        };
        for (var face : faces) {
            Block adjacent = chestBlock.getRelative(face);
            if (adjacent.getState() instanceof Sign) {
                return adjacent.getLocation();
            }
        }
        return null;
    }

    private void updateSign(Location signLoc, Shop shop) {
        Block block = signLoc.getBlock();
        if (!(block.getState() instanceof Sign sign)) return;

        var settings = plugin.getConfigManager().getSettings();
        String colorTag = switch (shop.getShopType()) {
            case BUY -> settings.getBuyColor();
            case SELL -> settings.getSellColor();
            case BUY_SELL -> settings.getBothColor();
        };

        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        sign.setLine(0, MessageUtils.colorize(colorTag + shop.getOwnerName()));
        sign.setLine(1, MessageUtils.colorize("<white>" + (itemName.length() > 15 ? itemName.substring(0, 13) + ".." : itemName)));
        sign.setLine(2, shop.getBuyPrice() != null
                ? MessageUtils.colorize("<green>B: " + settings.formatPrice(shop.getBuyPrice()))
                : MessageUtils.colorize("<gray>B: N/A"));
        sign.setLine(3, shop.getSellPrice() != null
                ? MessageUtils.colorize("<red>S: " + settings.formatPrice(shop.getSellPrice()))
                : MessageUtils.colorize("<gray>S: N/A"));
        sign.update();
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    private int parseIntOr(String s, int defaultValue) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(MessageUtils.colorize(msg));
    }
}
