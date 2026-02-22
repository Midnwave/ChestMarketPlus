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

public class ConfirmationGui {

    private final ChestMarketPlus plugin;
    private final Player player;
    private final String title;
    private final Shop shop;
    private final int quantity;
    private final Runnable onConfirm;
    private final Runnable onCancel;
    private Inventory inventory;

    public ConfirmationGui(ChestMarketPlus plugin, Player player, String title,
                           Shop shop, int quantity, Runnable onConfirm, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.shop = shop;
        this.quantity = quantity;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    public void open() {
        inventory = Bukkit.createInventory(null, 27, MessageUtils.colorize("<dark_gray>" + title));

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        ItemStack preview = shop.getItemTemplate().clone();
        preview.setAmount(Math.min(quantity, 64));
        ItemMeta meta = preview.getItemMeta();
        List<String> lore = new java.util.ArrayList<>();
        if (meta.hasLore()) {
            lore.addAll(meta.getLore());
            lore.add("");
        }
        lore.add(MessageUtils.colorize("<gray>Quantity: <white>" + quantity));
        if (shop.getBuyPrice() != null)
            lore.add(MessageUtils.colorize("<gray>Total: <gold>" + plugin.getConfigManager().getSettings().formatPrice(shop.getBuyPrice() * quantity)));
        else if (shop.getSellPrice() != null)
            lore.add(MessageUtils.colorize("<gray>Total: <gold>" + plugin.getConfigManager().getSettings().formatPrice(shop.getSellPrice() * quantity)));
        meta.setLore(lore);
        preview.setItemMeta(meta);
        inventory.setItem(13, preview);

        ItemStack confirm = createItem(Material.LIME_STAINED_GLASS_PANE,
                MessageUtils.colorize("<green><bold>CONFIRM"));
        for (int slot : new int[]{9, 10, 11, 12, 0, 1, 2, 3}) {
            inventory.setItem(slot, confirm);
        }

        ItemStack cancel = createItem(Material.RED_STAINED_GLASS_PANE,
                MessageUtils.colorize("<red><bold>CANCEL"));
        for (int slot : new int[]{14, 15, 16, 17, 23, 24, 25, 26}) {
            inventory.setItem(slot, cancel);
        }

        player.openInventory(inventory);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot >= 0 && slot <= 12 && slot != 4 && slot != 5 && slot != 13) {
            player.closeInventory();
            Bukkit.getScheduler().runTask(plugin, onConfirm);
        }
        else if (slot >= 14 && slot <= 26 && slot != 22) {
            player.closeInventory();
            Bukkit.getScheduler().runTask(plugin, onCancel);
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
