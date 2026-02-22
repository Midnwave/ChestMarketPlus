package com.blockforge.chestmarketplus.gui;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class GuiManager {
    private final ChestMarketPlus plugin;
    private final Map<UUID, ActiveGui> activeGuis = new ConcurrentHashMap<>();

    public GuiManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void openShopGui(Player player, Shop shop) {
        ShopGui gui = new ShopGui(plugin, player, shop);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick, null));
    }

    public void openConfirmationGui(Player player, String title, Shop shop, int quantity,
                                     Runnable onConfirm, Runnable onCancel) {
        ConfirmationGui gui = new ConfirmationGui(plugin, player, title, shop, quantity, onConfirm, onCancel);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick, null));
    }

    public void openQuantitySelector(Player player, Shop shop, boolean isBuying,
                                      java.util.function.BiConsumer<Player, Integer> onSelect) {
        QuantitySelectorGui gui = new QuantitySelectorGui(plugin, player, shop, isBuying, onSelect);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick, null));
    }

    public void openTransactionLogGui(Player player, Shop shop, int page) {
        TransactionLogGui gui = new TransactionLogGui(plugin, player, shop, page);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick, null));
    }

    public void openFavoritesGui(Player player) {
        FavoritesGui gui = new FavoritesGui(plugin, player);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick, null));
    }

    public void openQuickSellConfirmGui(Player player, Shop shop, int totalItems, double totalPrice) {
        QuickSellConfirmGui gui = new QuickSellConfirmGui(plugin, player, shop, totalItems, totalPrice);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick, null));
    }

    public void openItemEditGui(Player player, Shop shop) {
        ShopItemEditGui gui = new ShopItemEditGui(plugin, player, shop);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick, gui::onClose));
    }

    /**
     * Opens a read-only snapshot of the shop's chest inventory so the player can
     * see what's inside without being able to take anything.
     */
    public void openChestPeekGui(Player player, Shop shop) {
        org.bukkit.inventory.Inventory shopInv =
                com.blockforge.chestmarketplus.shop.StockManager.getShopInventory(shop);
        if (shopInv == null) return;

        int size = shopInv.getSize();
        String title = com.blockforge.chestmarketplus.util.MessageUtils.colorize(
                "<gray>Preview: <white>" + shop.getOwnerName() + "'s Shop");
        org.bukkit.inventory.Inventory peekInv = org.bukkit.Bukkit.createInventory(null, size, title);
        for (int i = 0; i < size; i++) {
            org.bukkit.inventory.ItemStack item = shopInv.getItem(i);
            peekInv.setItem(i, item != null ? item.clone() : null);
        }
        player.openInventory(peekInv);
        // No-op click handler — InventoryListener already cancels all clicks for plugin GUIs
        activeGuis.put(player.getUniqueId(), new ActiveGui(null, (p, e) -> {}, null));
    }

    public boolean isPluginGui(Player player) {
        return activeGuis.containsKey(player.getUniqueId());
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        ActiveGui gui = activeGuis.get(player.getUniqueId());
        if (gui != null) {
            gui.clickHandler().accept(player, event);
        }
    }

    public void handleClose(Player player) {
        ActiveGui gui = activeGuis.remove(player.getUniqueId());
        if (gui != null && gui.closeHandler() != null) {
            gui.closeHandler().run();
        }
    }

    public void openCreationGui(Player player, ShopCreationGui gui) {
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick, null));
    }

    public void openConfigGui(Player player) {
        ConfigGui gui = new ConfigGui(plugin, player);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick, null));
    }

    public void closeAll() {
        activeGuis.clear();
    }

    private record ActiveGui(Object gui, BiConsumer<Player, InventoryClickEvent> clickHandler, Runnable closeHandler) {}
}
