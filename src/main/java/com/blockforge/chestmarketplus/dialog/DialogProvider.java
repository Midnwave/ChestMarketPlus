package com.blockforge.chestmarketplus.dialog;

import com.blockforge.chestmarketplus.api.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface DialogProvider {

    void showBuyConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel);

    void showSellConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel);

    void showQuickSellConfirmation(Player player, Shop shop, List<ItemStack> items, Runnable onConfirm, Runnable onCancel);

    void showDeleteConfirmation(Player player, Shop shop, Runnable onConfirm, Runnable onCancel);
}
