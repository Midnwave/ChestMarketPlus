package com.blockforge.chestmarketplus.dialog;

import com.blockforge.chestmarketplus.api.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.IntConsumer;

public interface DialogProvider {

    void showBuyConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel);

    void showSellConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel);

    void showQuickSellConfirmation(Player player, Shop shop, List<ItemStack> items, Runnable onConfirm, Runnable onCancel);

    void showDeleteConfirmation(Player player, Shop shop, Runnable onConfirm, Runnable onCancel);

    /**
     * Shows the main shop interaction dialog for BUY_SELL shops.
     * Displays item info, prices, stock, and Buy/Sell/Favorite buttons.
     */
    void showShopDialog(Player player, Shop shop, Runnable onBuy, Runnable onSell, Runnable onFavorite);

    /**
     * Shows a quantity selection + confirmation for buying.
     * On Paper: a single dialog with a number range slider and Buy/Cancel buttons.
     * On Spigot: chains QuantitySelectorGui → ConfirmationGui.
     * The onConfirm callback receives the chosen quantity when the user confirms.
     */
    void showBuyQuantityAndConfirm(Player player, Shop shop, int maxQty, IntConsumer onConfirm, Runnable onCancel);

    /**
     * Shows a quantity selection + confirmation for selling.
     * Same behavior as showBuyQuantityAndConfirm but for sell flow.
     */
    void showSellQuantityAndConfirm(Player player, Shop shop, int maxQty, IntConsumer onConfirm, Runnable onCancel);
}
