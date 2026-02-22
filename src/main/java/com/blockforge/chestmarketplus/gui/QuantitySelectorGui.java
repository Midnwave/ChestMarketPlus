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
import java.util.function.BiConsumer;

public class QuantitySelectorGui {

    private final ChestMarketPlus plugin;
    private final Player player;
    private final Shop shop;
    private final boolean isBuying;
    private final BiConsumer<Player, Integer> onSelect;
    private final int maxQty;
    private Inventory inventory;

    // Row 2 slots (middle row) for preset amounts
    private static final int[] QUANTITY_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] QUANTITIES = {1, 8, 16, 32, 64, 128, 256};

    public QuantitySelectorGui(ChestMarketPlus plugin, Player player, Shop shop,
                                boolean isBuying, BiConsumer<Player, Integer> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.shop = shop;
        this.isBuying = isBuying;
        this.onSelect = onSelect;

        if (isBuying) {
            this.maxQty = shop.isAdmin() ? 64 : shop.getCurrentStock();
        } else {
            this.maxQty = ItemUtils.countMatchingItems(player.getInventory(), shop.getItemTemplate());
        }
    }

    public void open() {
        String title = MessageUtils.colorize("<dark_gray>Select Quantity");
        inventory = Bukkit.createInventory(null, 27, title);

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        double pricePerItem = isBuying ? shop.getBuyPrice() : shop.getSellPrice();

        // Slot 4 (top center): custom amount via chat input
        ItemStack customBtn = createItem(Material.WRITABLE_BOOK,
                MessageUtils.colorize("<aqua><bold>Custom Amount"),
                MessageUtils.colorize("<gray>Click to type any quantity in chat"));
        inventory.setItem(4, customBtn);

        // Middle row: preset amounts
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

        // Slot 22: cancel
        ItemStack cancel = createItem(Material.BARRIER, MessageUtils.colorize("<red><bold>Cancel"));
        inventory.setItem(22, cancel);

        // Slot 26: buy/sell ALL (respects allow-all-quantity config)
        if (plugin.getConfigManager().getSettings().isAllowAllQuantity() && maxQty > 0) {
            ItemStack allBtn = createItem(Material.EMERALD,
                    MessageUtils.colorize("<green><bold>All (" + maxQty + "x)"),
                    MessageUtils.colorize("<gray>Total: <gold>" + plugin.getConfigManager().getSettings().formatPrice(pricePerItem * maxQty)));
            inventory.setItem(26, allBtn);
        }

        player.openInventory(inventory);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 22) {
            player.closeInventory();
            return;
        }

        // Custom amount — close GUI and await chat input
        if (slot == 4) {
            player.closeInventory();
            int max = maxQty;
            MessageUtils.sendMessage(player, MessageUtils.colorize(
                    "<yellow>Enter a custom quantity <gray>(1-" + max + ") or type <white>cancel<gray>:"));
            plugin.getChatInputListener().awaitInput(player, input -> {
                if ("cancel".equalsIgnoreCase(input)) return;
                try {
                    int qty = Integer.parseInt(input.trim());
                    if (qty <= 0 || qty > max) {
                        MessageUtils.sendMessage(player, MessageUtils.colorize(
                                "<red>Quantity must be between 1 and " + max + "."));
                        return;
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> onSelect.accept(player, qty));
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(player, MessageUtils.colorize("<red>Invalid number."));
                }
            });
            return;
        }

        // All button
        if (slot == 26 && plugin.getConfigManager().getSettings().isAllowAllQuantity() && maxQty > 0) {
            player.closeInventory();
            Bukkit.getScheduler().runTask(plugin, () -> onSelect.accept(player, maxQty));
            return;
        }

        // Preset amounts
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
