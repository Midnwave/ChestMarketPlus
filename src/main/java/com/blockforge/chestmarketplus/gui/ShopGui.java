package com.blockforge.chestmarketplus.gui;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    private final Runnable onBuy;
    private final Runnable onSell;
    private final Runnable onFavorite;
    private Inventory inventory;

    public ShopGui(ChestMarketPlus plugin, Player player, Shop shop,
                   Runnable onBuy, Runnable onSell, Runnable onFavorite) {
        this.plugin = plugin;
        this.player = player;
        this.shop = shop;
        this.onBuy = onBuy;
        this.onSell = onSell;
        this.onFavorite = onFavorite;
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
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
        List<String> lore = new ArrayList<>();
        if (meta != null && meta.hasLore()) {
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
                    MessageUtils.colorize("<gray>Click to choose amount"));
            inventory.setItem(11, buyBtn);
        }

        if (shop.getShopType().canSell()) {
            ItemStack sellBtn = createItem(Material.RED_STAINED_GLASS_PANE,
                    MessageUtils.colorize("<red><bold>SELL"),
                    MessageUtils.colorize("<gray>Price: <red>" + settings.formatPrice(shop.getSellPrice())),
                    MessageUtils.colorize("<gray>Click to choose amount"));
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

        switch (slot) {
            case 11 -> {
                if (!shop.getShopType().canBuy()) return;
                player.closeInventory();
                Bukkit.getScheduler().runTask(plugin, onBuy);
            }
            case 15 -> {
                if (!shop.getShopType().canSell()) return;
                player.closeInventory();
                Bukkit.getScheduler().runTask(plugin, onSell);
            }
            case 22 -> {
                player.closeInventory();
                Bukkit.getScheduler().runTask(plugin, onFavorite);
            }
        }
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
