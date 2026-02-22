package com.blockforge.chestmarketplus.dialog;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.util.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PaperDialogProvider implements DialogProvider {

    private final ChestMarketPlus plugin;

    public PaperDialogProvider(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void showBuyConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel) {
        try {
            showDialogInternal(player, shop, quantity, "Confirm Purchase",
                    "Buy " + quantity + "x " + ItemUtils.getDisplayName(shop.getItemTemplate())
                            + " for " + plugin.getConfigManager().getSettings().formatPrice(shop.getBuyPrice() * quantity) + "?",
                    onConfirm, onCancel);
        } catch (Exception e) {
            new InventoryDialogProvider(plugin).showBuyConfirmation(player, shop, quantity, onConfirm, onCancel);
        }
    }

    @Override
    public void showSellConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel) {
        try {
            showDialogInternal(player, shop, quantity, "Confirm Sale",
                    "Sell " + quantity + "x " + ItemUtils.getDisplayName(shop.getItemTemplate())
                            + " for " + plugin.getConfigManager().getSettings().formatPrice(shop.getSellPrice() * quantity) + "?",
                    onConfirm, onCancel);
        } catch (Exception e) {
            new InventoryDialogProvider(plugin).showSellConfirmation(player, shop, quantity, onConfirm, onCancel);
        }
    }

    @Override
    public void showQuickSellConfirmation(Player player, Shop shop, List<ItemStack> items, Runnable onConfirm, Runnable onCancel) {
        try {
            int totalItems = items.stream().mapToInt(ItemStack::getAmount).sum();
            double totalPrice = shop.getSellPrice() * totalItems;
            showDialogInternal(player, shop, totalItems, "Quick Sell",
                    "Sell " + totalItems + "x " + ItemUtils.getDisplayName(shop.getItemTemplate())
                            + " for " + plugin.getConfigManager().getSettings().formatPrice(totalPrice) + "?",
                    onConfirm, onCancel);
        } catch (Exception e) {
            new InventoryDialogProvider(plugin).showQuickSellConfirmation(player, shop, items, onConfirm, onCancel);
        }
    }

    @Override
    public void showDeleteConfirmation(Player player, Shop shop, Runnable onConfirm, Runnable onCancel) {
        try {
            showDialogInternal(player, shop, 1, "Delete Shop",
                    "Are you sure you want to delete this shop? This action cannot be undone.",
                    onConfirm, onCancel);
        } catch (Exception e) {
            new InventoryDialogProvider(plugin).showDeleteConfirmation(player, shop, onConfirm, onCancel);
        }
    }

    private void showDialogInternal(Player player, Shop shop, int quantity, String title,
                                     String description, Runnable onConfirm, Runnable onCancel) throws Exception {
        // use paper dialog api via reflection to avoid compile-time dependency

        Class<?> dialogClass = Class.forName("io.papermc.paper.dialog.Dialog");
        Class<?> dialogBaseClass = Class.forName("io.papermc.paper.registry.data.dialog.DialogBase");
        Class<?> dialogTypeClass = Class.forName("io.papermc.paper.registry.data.dialog.type.DialogType");
        Class<?> dialogBodyClass = Class.forName("io.papermc.paper.registry.data.dialog.body.DialogBody");
        Class<?> actionButtonClass = Class.forName("io.papermc.paper.registry.data.dialog.ActionButton");
        Class<?> dialogActionClass = Class.forName("io.papermc.paper.registry.data.dialog.action.DialogAction");

        var componentClass = Class.forName("net.kyori.adventure.text.Component");
        var textMethod = componentClass.getMethod("text", String.class);
        var titleComponent = textMethod.invoke(null, title);
        var bodyComponent = textMethod.invoke(null, description);

        var plainMessageMethod = dialogBodyClass.getMethod("plainMessage", componentClass);
        var body = plainMessageMethod.invoke(null, bodyComponent);

        var confirmMethod = dialogTypeClass.getMethod("confirmation");
        var confirmationType = confirmMethod.invoke(null);

        var createMethod = dialogClass.getMethod("create", java.util.function.Consumer.class);

        var dialog = createMethod.invoke(null, (java.util.function.Consumer<?>) builder -> {
            try {
                var baseBuilderMethod = dialogBaseClass.getMethod("builder", componentClass);
                var baseBuilder = baseBuilderMethod.invoke(null, titleComponent);
                var baseBuildMethod = baseBuilder.getClass().getMethod("build");
                var base = baseBuildMethod.invoke(baseBuilder);

                var setBaseMethod = builder.getClass().getMethod("base", dialogBaseClass);
                setBaseMethod.invoke(builder, base);

                var setBodyMethod = builder.getClass().getMethod("body", dialogBodyClass);
                setBodyMethod.invoke(builder, body);

                var setTypeMethod = builder.getClass().getMethod("type", dialogTypeClass);
                setTypeMethod.invoke(builder, confirmationType);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to build dialog", ex);
            }
        });

        var showDialogMethod = player.getClass().getMethod("showDialog", dialogClass);
        showDialogMethod.invoke(player, dialog);
    }
}
