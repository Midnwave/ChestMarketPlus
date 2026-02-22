package com.blockforge.chestmarketplus.gui;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.api.ShopTransaction;
import com.blockforge.chestmarketplus.util.MessageUtils;
import com.blockforge.chestmarketplus.util.TimeUtils;
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

public class TransactionLogGui {

    private final ChestMarketPlus plugin;
    private final Player player;
    private final Shop shop;
    private final int page;
    private static final int ITEMS_PER_PAGE = 21;
    private Inventory inventory;

    public TransactionLogGui(ChestMarketPlus plugin, Player player, Shop shop, int page) {
        this.plugin = plugin;
        this.player = player;
        this.shop = shop;
        this.page = page;
    }

    public void open() {
        String title = MessageUtils.colorize("<dark_gray>Transaction Log - Page " + (page + 1));
        inventory = Bukkit.createInventory(null, 36, title);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 27; i < 36; i++) {
            inventory.setItem(i, filler);
        }

        try {
            List<ShopTransaction> transactions = plugin.getDatabaseManager()
                    .getTransactionRepository()
                    .getTransactionsByShop(shop.getId(), ITEMS_PER_PAGE, page * ITEMS_PER_PAGE);

            for (int i = 0; i < transactions.size() && i < ITEMS_PER_PAGE; i++) {
                ShopTransaction tx = transactions.get(i);
                Material mat = "BUY".equals(tx.getAction()) ? Material.EMERALD : Material.REDSTONE;

                List<String> lore = new ArrayList<>();
                lore.add(MessageUtils.colorize("<gray>Player: <white>" + tx.getBuyerName()));
                lore.add(MessageUtils.colorize("<gray>Action: <white>" + tx.getAction()));
                lore.add(MessageUtils.colorize("<gray>Quantity: <white>" + tx.getQuantity()));
                lore.add(MessageUtils.colorize("<gray>Total: <gold>" + plugin.getConfigManager().getSettings().formatPrice(tx.getPriceTotal())));
                if (tx.getTaxAmount() > 0) {
                    lore.add(MessageUtils.colorize("<gray>Tax: <red>" + plugin.getConfigManager().getSettings().formatPrice(tx.getTaxAmount())));
                }
                lore.add(MessageUtils.colorize("<gray>Date: <white>" + TimeUtils.formatTimestamp(tx.getCreatedAt())));

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(MessageUtils.colorize(
                        ("BUY".equals(tx.getAction()) ? "<green>" : "<red>") + tx.getAction() + " - " + tx.getItemType()));
                meta.setLore(lore);
                item.setItemMeta(meta);
                inventory.setItem(i, item);
            }

            if (page > 0) {
                ItemStack prev = new ItemStack(Material.ARROW);
                ItemMeta pm = prev.getItemMeta();
                pm.setDisplayName(MessageUtils.colorize("<yellow>Previous Page"));
                prev.setItemMeta(pm);
                inventory.setItem(27, prev);
            }

            if (transactions.size() == ITEMS_PER_PAGE) {
                ItemStack next = new ItemStack(Material.ARROW);
                ItemMeta nm = next.getItemMeta();
                nm.setDisplayName(MessageUtils.colorize("<yellow>Next Page"));
                next.setItemMeta(nm);
                inventory.setItem(35, next);
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load transaction log: " + e.getMessage());
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(MessageUtils.colorize("<red>Close"));
        close.setItemMeta(cm);
        inventory.setItem(31, close);

        player.openInventory(inventory);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 27 && page > 0) {
            player.closeInventory();
            plugin.getGuiManager().openTransactionLogGui(player, shop, page - 1);
        } else if (slot == 35) {
            player.closeInventory();
            plugin.getGuiManager().openTransactionLogGui(player, shop, page + 1);
        } else if (slot == 31) {
            player.closeInventory();
        }
    }
}
