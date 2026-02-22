package com.blockforge.chestmarketplus.gui;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ShopItemEditGui {

    private static final int ITEM_SLOT = 13;

    private final ChestMarketPlus plugin;
    private final Player player;
    private final Shop shop;
    private Inventory inventory;

    public ShopItemEditGui(ChestMarketPlus plugin, Player player, Shop shop) {
        this.plugin = plugin;
        this.player = player;
        this.shop = shop;
    }

    public void open() {
        inventory = Bukkit.createInventory(null, 27,
                MessageUtils.colorize("&8Edit Shop Item"));

        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        // Place current item in the center slot
        ItemStack preview = shop.getItemTemplate().clone();
        preview.setAmount(1);
        inventory.setItem(ITEM_SLOT, preview);

        // Label above item slot
        inventory.setItem(4, makeItem(Material.OAK_SIGN,
                MessageUtils.colorize("&eChange Shop Item"),
                MessageUtils.colorize("&7Drag any item from your inventory"),
                MessageUtils.colorize("&7into the center slot to change it."),
                MessageUtils.colorize("&7Current: &f" + ItemUtils.getDisplayName(shop.getItemTemplate()))));

        // Confirm
        inventory.setItem(11, makeItem(Material.LIME_STAINED_GLASS_PANE,
                MessageUtils.colorize("&a&lCONFIRM"),
                MessageUtils.colorize("&7Set the center item as the new shop item.")));

        // Cancel
        inventory.setItem(15, makeItem(Material.RED_STAINED_GLASS_PANE,
                MessageUtils.colorize("&c&lCANCEL"),
                MessageUtils.colorize("&7Return item and go back.")));

        player.openInventory(inventory);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        int topSize = inventory.getSize();

        if (slot == 11) {
            // Confirm — save the item in slot 13
            ItemStack newItem = inventory.getItem(ITEM_SLOT);
            if (newItem == null || newItem.getType() == Material.AIR
                    || newItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                MessageUtils.sendMessage(player, MessageUtils.colorize(
                        "<red>No item in the slot. Drag an item in first."));
                return;
            }
            player.closeInventory();
            newItem = newItem.clone();
            newItem.setAmount(1);
            applyItemChange(player, newItem);
            return;
        }

        if (slot == 15) {
            // Cancel — return any item in slot 13 and close
            returnSlotItem(player);
            player.closeInventory();
            return;
        }

        if (slot == ITEM_SLOT) {
            // Player clicked the item slot — handle swap with cursor or held item
            ItemStack cursor = event.getCursor();
            ItemStack current = inventory.getItem(ITEM_SLOT);

            if (cursor != null && cursor.getType() != Material.AIR) {
                // Player is placing an item from cursor into the slot
                ItemStack toReturn = (current != null && current.getType() != Material.AIR
                        && current.getType() != Material.GRAY_STAINED_GLASS_PANE) ? current.clone() : null;

                inventory.setItem(ITEM_SLOT, cursor.clone());
                event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));

                if (toReturn != null) {
                    player.getInventory().addItem(toReturn).values()
                            .forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
                }
                refreshLabel();
            } else if (current != null && current.getType() != Material.AIR
                    && current.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                // Player clicked to remove current item
                player.getInventory().addItem(current.clone()).values()
                        .forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
                inventory.setItem(ITEM_SLOT, new ItemStack(Material.AIR));
                refreshLabel();
            }
            return;
        }

        // Player clicked from their own inventory — move item to slot 13
        if (slot >= topSize) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            ItemStack current = inventory.getItem(ITEM_SLOT);

            // Return old slot item to inventory first
            if (current != null && current.getType() != Material.AIR
                    && current.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                player.getInventory().addItem(current.clone()).values()
                        .forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
            }

            // Take one of the clicked item and put it in slot 13
            ItemStack one = clicked.clone();
            one.setAmount(1);
            inventory.setItem(ITEM_SLOT, one);

            // Remove one from the source slot
            ItemStack remaining = clicked.clone();
            remaining.setAmount(clicked.getAmount() - 1);
            if (remaining.getAmount() <= 0) {
                event.getClickedInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
            } else {
                event.getClickedInventory().setItem(event.getSlot(), remaining);
            }

            refreshLabel();
        }
    }

    public void onClose() {
        // Safety: if player closes without confirming, return the item in slot 13
        returnSlotItem(player);
    }

    private void returnSlotItem(Player player) {
        if (inventory == null) return;
        ItemStack current = inventory.getItem(ITEM_SLOT);
        if (current != null && current.getType() != Material.AIR
                && current.getType() != Material.GRAY_STAINED_GLASS_PANE) {
            // Only return if it differs from the original shop item
            if (!current.isSimilar(shop.getItemTemplate())) {
                player.getInventory().addItem(current.clone()).values()
                        .forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
            }
        }
    }

    private void applyItemChange(Player player, ItemStack newItem) {
        // Check blacklist
        List<String> blacklist = plugin.getConfigManager().getSettings().getItemBlacklist();
        if (blacklist != null && blacklist.contains(newItem.getType().name())
                && !player.hasPermission("chestmarket.bypass.blacklist")) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("item-blacklisted"));
            player.getInventory().addItem(newItem).values()
                    .forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
            return;
        }

        shop.setItemTemplate(newItem);
        plugin.getShopManager().updateShop(shop);
        plugin.getDisplayManager().updateDisplay(shop);

        // Refresh sign text
        org.bukkit.Location signLoc = shop.getSignLocation();
        if (signLoc != null) {
            org.bukkit.block.Block block = signLoc.getBlock();
            if (block.getState() instanceof org.bukkit.block.Sign sign) {
                var settings = plugin.getConfigManager().getSettings();
                String colorTag = switch (shop.getShopType()) {
                    case BUY -> settings.getBuyColor();
                    case SELL -> settings.getSellColor();
                    case BUY_SELL -> settings.getBothColor();
                };
                String itemName = ItemUtils.getDisplayName(newItem);
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
        }

        MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("item-updated",
                "{item}", ItemUtils.getDisplayName(newItem)));
    }

    private void refreshLabel() {
        ItemStack current = inventory.getItem(ITEM_SLOT);
        String currentName = (current != null && current.getType() != Material.AIR
                && current.getType() != Material.GRAY_STAINED_GLASS_PANE)
                ? ItemUtils.getDisplayName(current) : "None";

        inventory.setItem(4, makeItem(Material.OAK_SIGN,
                MessageUtils.colorize("&eChange Shop Item"),
                MessageUtils.colorize("&7Drag any item from your inventory"),
                MessageUtils.colorize("&7into the center slot to change it."),
                MessageUtils.colorize("&7Current: &f" + currentName)));
    }

    private ItemStack makeItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
