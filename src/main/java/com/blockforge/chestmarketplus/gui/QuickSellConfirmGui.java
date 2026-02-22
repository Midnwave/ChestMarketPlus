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

import java.util.List;

public class QuickSellConfirmGui {

    private final ChestMarketPlus plugin;
    private final Player player;
    private final Shop shop;
    private final int totalItems;
    private final double totalPrice;
    private Inventory inventory;

    public QuickSellConfirmGui(ChestMarketPlus plugin, Player player, Shop shop,
                                int totalItems, double totalPrice) {
        this.plugin = plugin;
        this.player = player;
        this.shop = shop;
        this.totalItems = totalItems;
        this.totalPrice = totalPrice;
    }

    public void open() {
        String title = MessageUtils.colorize("<dark_gray>Quick Sell Confirmation");
        inventory = Bukkit.createInventory(null, 27, title);

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        ItemStack info = shop.getItemTemplate().clone();
        info.setAmount(Math.min(totalItems, 64));
        ItemMeta meta = info.getItemMeta();
        meta.setLore(List.of(
                MessageUtils.colorize("<gray>Selling: <white>" + totalItems + "x " + ItemUtils.getDisplayName(shop.getItemTemplate())),
                MessageUtils.colorize("<gray>You receive: <gold>" + plugin.getConfigManager().getSettings().formatPrice(totalPrice)),
                "",
                MessageUtils.colorize("<yellow>Are you sure?")
        ));
        info.setItemMeta(meta);
        inventory.setItem(13, info);

        ItemStack confirm = createItem(Material.LIME_STAINED_GLASS_PANE,
                MessageUtils.colorize("<green><bold>CONFIRM SELL"));
        for (int slot : new int[]{9, 10, 11, 12}) {
            inventory.setItem(slot, confirm);
        }

        ItemStack cancel = createItem(Material.RED_STAINED_GLASS_PANE,
                MessageUtils.colorize("<red><bold>CANCEL"));
        for (int slot : new int[]{14, 15, 16, 17}) {
            inventory.setItem(slot, cancel);
        }

        player.openInventory(inventory);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot >= 9 && slot <= 12) {
            player.closeInventory();
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("quick-sell-processing"));
        } else if (slot >= 14 && slot <= 17) {
            player.closeInventory();
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("transaction-cancelled"));
        }
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
