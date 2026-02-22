package com.blockforge.chestmarketplus.gui;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.BiConsumer;

public class QuantitySelectorGui {

    private final ChestMarketPlus plugin;
    private final Player player;
    private final Shop shop;
    private final boolean isBuying;
    private final BiConsumer<Player, Integer> onSelect;
    private Inventory inventory;

    private static final int[] QUANTITY_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] QUANTITIES = {1, 8, 16, 32, 64, 128, 256};

    public QuantitySelectorGui(ChestMarketPlus plugin, Player player, Shop shop,
                                boolean isBuying, BiConsumer<Player, Integer> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.shop = shop;
        this.isBuying = isBuying;
        this.onSelect = onSelect;
    }

    public void open() {
        String title = MessageUtils.colorize("<dark_gray>Select Quantity");
        inventory = Bukkit.createInventory(null, 27, title);

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        double pricePerItem = isBuying ? shop.getBuyPrice() : shop.getSellPrice();
        String priceFormat = plugin.getConfigManager().getSettings().getCurrencySymbol();

        for (int i = 0; i < QUANTITY_SLOTS.length; i++) {
            int qty = QUANTITIES[i];
            double total = pricePerItem * qty;
            Material mat = qty <= 64 ? Material.PAPER : Material.BOOK;

            ItemStack item = createItem(mat,
                    MessageUtils.colorize("<yellow><bold>" + qty + "x"),
                    MessageUtils.colorize("<gray>Total: <gold>" + plugin.getConfigManager().getSettings().formatPrice(total)));
            item.setAmount(Math.min(qty, 64));
            inventory.setItem(QUANTITY_SLOTS[i], item);
        }

        ItemStack cancel = createItem(Material.BARRIER, MessageUtils.colorize("<red><bold>Cancel"));
        inventory.setItem(22, cancel);

        player.openInventory(inventory);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 22) {
            player.closeInventory();
            return;
        }

        for (int i = 0; i < QUANTITY_SLOTS.length; i++) {
            if (slot == QUANTITY_SLOTS[i]) {
                int qty = QUANTITIES[i];
                player.closeInventory();
                Bukkit.getScheduler().runTask(plugin, () -> onSelect.accept(player, qty));
                return;
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
