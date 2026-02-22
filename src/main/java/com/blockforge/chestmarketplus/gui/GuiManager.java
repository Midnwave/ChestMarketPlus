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
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick));
    }

    public void openConfirmationGui(Player player, String title, Shop shop, int quantity,
                                     Runnable onConfirm, Runnable onCancel) {
        ConfirmationGui gui = new ConfirmationGui(plugin, player, title, shop, quantity, onConfirm, onCancel);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick));
    }

    public void openQuantitySelector(Player player, Shop shop, boolean isBuying,
                                      java.util.function.BiConsumer<Player, Integer> onSelect) {
        QuantitySelectorGui gui = new QuantitySelectorGui(plugin, player, shop, isBuying, onSelect);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick));
    }

    public void openTransactionLogGui(Player player, Shop shop, int page) {
        TransactionLogGui gui = new TransactionLogGui(plugin, player, shop, page);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick));
    }

    public void openFavoritesGui(Player player) {
        FavoritesGui gui = new FavoritesGui(plugin, player);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick));
    }

    public void openQuickSellConfirmGui(Player player, Shop shop, int totalItems, double totalPrice) {
        QuickSellConfirmGui gui = new QuickSellConfirmGui(plugin, player, shop, totalItems, totalPrice);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick));
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
        activeGuis.remove(player.getUniqueId());
    }

    public void openCreationGui(Player player, ShopCreationGui gui) {
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick));
    }

    public void openConfigGui(Player player) {
        ConfigGui gui = new ConfigGui(plugin, player);
        gui.open();
        activeGuis.put(player.getUniqueId(), new ActiveGui(gui, gui::handleClick));
    }

    public void closeAll() {
        activeGuis.clear();
    }

    private record ActiveGui(Object gui, BiConsumer<Player, InventoryClickEvent> clickHandler) {}
}
