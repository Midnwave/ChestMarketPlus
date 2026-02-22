package com.blockforge.chestmarketplus.dialog;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.config.Settings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.IntConsumer;

public class InventoryDialogProvider implements DialogProvider {

    private final ChestMarketPlus plugin;

    public InventoryDialogProvider(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void showBuyConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel) {
        plugin.getGuiManager().openConfirmationGui(player, "Confirm Purchase", shop, quantity, onConfirm, onCancel);
    }

    @Override
    public void showSellConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel) {
        plugin.getGuiManager().openConfirmationGui(player, "Confirm Sale", shop, quantity, onConfirm, onCancel);
    }

    @Override
    public void showQuickSellConfirmation(Player player, Shop shop, List<ItemStack> items, Runnable onConfirm, Runnable onCancel) {
        int totalItems = items.stream().mapToInt(ItemStack::getAmount).sum();
        double totalPrice = shop.getSellPrice() * totalItems;
        plugin.getGuiManager().openQuickSellConfirmGui(player, shop, totalItems, totalPrice);
    }

    @Override
    public void showDeleteConfirmation(Player player, Shop shop, Runnable onConfirm, Runnable onCancel) {
        plugin.getGuiManager().openConfirmationGui(player, "Delete Shop?", shop, 1, onConfirm, onCancel);
    }

    @Override
    public void showShopDialog(Player player, Shop shop, Runnable onBuy, Runnable onSell, Runnable onFavorite) {
        plugin.getGuiManager().openShopGui(player, shop, onBuy, onSell, onFavorite);
    }

    @Override
    public void showBuyQuantityAndConfirm(Player player, Shop shop, int maxQty, IntConsumer onConfirm, Runnable onCancel) {
        plugin.getGuiManager().openQuantitySelector(player, shop, true, (p, qty) -> {
            Settings s = plugin.getConfigManager().getSettings();
            double total = shop.getBuyPrice() * qty;
            String title = "Buy " + qty + "x for " + s.formatPrice(total);
            plugin.getGuiManager().openConfirmationGui(player, title, shop, qty,
                    () -> onConfirm.accept(qty),
                    onCancel);
        });
    }

    @Override
    public void showSellQuantityAndConfirm(Player player, Shop shop, int maxQty, IntConsumer onConfirm, Runnable onCancel) {
        plugin.getGuiManager().openQuantitySelector(player, shop, false, (p, qty) -> {
            Settings s = plugin.getConfigManager().getSettings();
            double total = shop.getSellPrice() * qty;
            String title = "Sell " + qty + "x for " + s.formatPrice(total);
            plugin.getGuiManager().openConfirmationGui(player, title, shop, qty,
                    () -> onConfirm.accept(qty),
                    onCancel);
        });
    }
}
