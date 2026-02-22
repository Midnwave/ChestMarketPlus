package com.blockforge.chestmarketplus.gui;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoritesGui {

    private final ChestMarketPlus plugin;
    private final Player player;
    private Inventory inventory;
    private final List<Shop> favoriteShops = new ArrayList<>();

    public FavoritesGui(ChestMarketPlus plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        String title = MessageUtils.colorize("<dark_gray>Your Favorites");
        inventory = Bukkit.createInventory(null, 54, title);

        try {
            List<Integer> shopIds = plugin.getDatabaseManager().getPlayerDataRepository()
                    .getFavoriteShopIds(player.getUniqueId());

            for (int id : shopIds) {
                Shop shop = plugin.getShopManager().getShopById(id);
                if (shop != null && shop.isActive()) {
                    favoriteShops.add(shop);
                }
            }

            for (int i = 0; i < favoriteShops.size() && i < 45; i++) {
                Shop shop = favoriteShops.get(i);
                ItemStack display = shop.getItemTemplate().clone();
                ItemMeta meta = display.getItemMeta();

                List<String> lore = new ArrayList<>();
                lore.add(MessageUtils.colorize("<gray>Owner: <white>" + shop.getOwnerName()));
                if (shop.getBuyPrice() != null)
                    lore.add(MessageUtils.colorize("<gray>Buy: <green>" + plugin.getConfigManager().getSettings().formatPrice(shop.getBuyPrice())));
                if (shop.getSellPrice() != null)
                    lore.add(MessageUtils.colorize("<gray>Sell: <red>" + plugin.getConfigManager().getSettings().formatPrice(shop.getSellPrice())));
                lore.add(MessageUtils.colorize("<gray>Stock: <white>" + shop.getCurrentStock()));
                lore.add("");
                lore.add(MessageUtils.colorize("<yellow>Click to view shop"));

                meta.setLore(lore);
                display.setItemMeta(meta);
                inventory.setItem(i, display);
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load favorites: " + e.getMessage());
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(MessageUtils.colorize("<red>Close"));
        close.setItemMeta(cm);
        inventory.setItem(49, close);

        player.openInventory(inventory);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if (slot >= 0 && slot < favoriteShops.size()) {
            Shop shop = favoriteShops.get(slot);
            player.closeInventory();
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getShopInteractListener().openShopInteraction(player, shop));
        }
    }
}
