package com.blockforge.chestmarketplus.dialog;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
}
