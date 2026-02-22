package com.blockforge.chestmarketplus.gui;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.api.ShopType;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.shop.ShopCreator;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ShopCreationGui {

    private final ChestMarketPlus plugin;
    private final Player player;
    private final ItemStack item;
    private final Location chestLocation;
    private final Location signLocation;

    public ShopCreationGui(ChestMarketPlus plugin, Player player, ItemStack item,
                           Location chestLocation, Location signLocation) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.chestLocation = chestLocation;
        this.signLocation = signLocation;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27,
                MessageUtils.colorize("&8Create Shop"));

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);

        ItemStack display = item.clone();
        display.setAmount(1);
        inv.setItem(4, display);

        inv.setItem(11, createItem(Material.LIME_CONCRETE,
                MessageUtils.colorize("&a&lBUY Shop"),
                MessageUtils.colorize("&7Players buy items from you")));

        inv.setItem(13, createItem(Material.YELLOW_CONCRETE,
                MessageUtils.colorize("&e&lBUY + SELL"),
                MessageUtils.colorize("&7Players can buy and sell")));

        inv.setItem(15, createItem(Material.RED_CONCRETE,
                MessageUtils.colorize("&c&lSELL Shop"),
                MessageUtils.colorize("&7You buy items from players")));

        inv.setItem(22, createItem(Material.BARRIER,
                MessageUtils.colorize("&cCancel")));

        player.openInventory(inv);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        ShopType type = switch (slot) {
            case 11 -> ShopType.BUY;
            case 13 -> ShopType.BUY_SELL;
            case 15 -> ShopType.SELL;
            default -> null;
        };

        if (slot == 22) {
            player.closeInventory();
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled"));
            return;
        }

        if (type == null) return;

        player.closeInventory();
        promptForPrice(type, null);
    }

    private void promptForPrice(ShopType type, Double buyPrice) {
        if (type == ShopType.BUY_SELL && buyPrice == null) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("enter-buy-price"));
        } else if (type == ShopType.BUY) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("enter-price"));
        } else {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("enter-sell-price"));
        }

        final Double savedBuyPrice = buyPrice;

        plugin.getChatInputListener().awaitInput(player, input -> {
            if ("cancel".equalsIgnoreCase(input)) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled"));
                return;
            }

            double price;
            try {
                price = Double.parseDouble(input);
                if (price <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("invalid-price"));
                promptForPrice(type, savedBuyPrice);
                return;
            }

            if (type == ShopType.BUY_SELL && savedBuyPrice == null) {
                promptForPrice(type, price);
            } else {
                double bp = 0, sp = 0;
                if (type == ShopType.BUY) bp = price;
                else if (type == ShopType.SELL) sp = price;
                else { bp = savedBuyPrice; sp = price; }
                createShop(type, bp, sp);
            }
        });
    }

    private void createShop(ShopType type, double buyPrice, double sellPrice) {
        ShopCreator creator = new ShopCreator(plugin);
        ShopCreator.CreateResult result = creator.createFromSign(
                player, type, buyPrice, sellPrice, chestLocation, signLocation, item);

        if (!result.isSuccess()) {
            MessageUtils.sendMessage(player, result.getErrorMessage());
            return;
        }

        Shop shop = result.getShop();
        updateSign(shop);

        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("shop-created",
                "{item}", itemName,
                "{price}", shop.getBuyPrice() != null
                        ? plugin.getConfigManager().getSettings().formatPrice(shop.getBuyPrice())
                        : plugin.getConfigManager().getSettings().formatPrice(shop.getSellPrice())));
    }

    private void updateSign(Shop shop) {
        Block block = signLocation.getBlock();
        if (!(block.getState() instanceof Sign sign)) return;

        Settings settings = plugin.getConfigManager().getSettings();
        String colorTag = switch (shop.getShopType()) {
            case BUY -> settings.getBuyColor();
            case SELL -> settings.getSellColor();
            case BUY_SELL -> settings.getBothColor();
        };

        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        sign.setLine(0, MessageUtils.colorize(colorTag + shop.getOwnerName()));
        sign.setLine(1, MessageUtils.colorize("&f" + truncate(itemName, 15)));
        sign.setLine(2, shop.getBuyPrice() != null
                ? MessageUtils.colorize("&aB: " + settings.formatPrice(shop.getBuyPrice()))
                : MessageUtils.colorize("&7B: N/A"));
        sign.setLine(3, shop.getSellPrice() != null
                ? MessageUtils.colorize("&cS: " + settings.formatPrice(shop.getSellPrice()))
                : MessageUtils.colorize("&7S: N/A"));
        sign.update(true);
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 2) + "..";
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
