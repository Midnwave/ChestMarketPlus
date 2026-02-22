package com.blockforge.chestmarketplus.dialog;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.util.ItemUtils;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

@SuppressWarnings("UnstableApiUsage")
public class PaperDialogProvider implements DialogProvider {

    private final ChestMarketPlus plugin;

    public PaperDialogProvider(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void showBuyConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel) {
        Settings s = plugin.getConfigManager().getSettings();
        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        double total = shop.getBuyPrice() * quantity;
        Component body = Component.text("Buy " + quantity + "x " + itemName + " for " + s.formatPrice(total) + "?");
        showConfirmDialog(player, "Confirm Purchase", body, shop.getItemTemplate(), onConfirm, onCancel);
    }

    @Override
    public void showSellConfirmation(Player player, Shop shop, int quantity, Runnable onConfirm, Runnable onCancel) {
        Settings s = plugin.getConfigManager().getSettings();
        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        double total = shop.getSellPrice() * quantity;
        Component body = Component.text("Sell " + quantity + "x " + itemName + " for " + s.formatPrice(total) + "?");
        showConfirmDialog(player, "Confirm Sale", body, shop.getItemTemplate(), onConfirm, onCancel);
    }

    @Override
    public void showQuickSellConfirmation(Player player, Shop shop, List<ItemStack> items, Runnable onConfirm, Runnable onCancel) {
        Settings s = plugin.getConfigManager().getSettings();
        int totalItems = items.stream().mapToInt(ItemStack::getAmount).sum();
        double totalPrice = shop.getSellPrice() * totalItems;
        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        Component body = Component.text("Quick sell " + totalItems + "x " + itemName + " for " + s.formatPrice(totalPrice) + "?");
        showConfirmDialog(player, "Quick Sell", body, shop.getItemTemplate(), onConfirm, onCancel);
    }

    @Override
    public void showDeleteConfirmation(Player player, Shop shop, Runnable onConfirm, Runnable onCancel) {
        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        Component body = Component.text("Delete shop selling " + itemName + "? This action cannot be undone.");
        showConfirmDialog(player, "Delete Shop?", body, shop.getItemTemplate(), onConfirm, onCancel);
    }

    @Override
    public void showShopDialog(Player player, Shop shop, Runnable onBuy, Runnable onSell, Runnable onFavorite) {
        Settings s = plugin.getConfigManager().getSettings();
        ClickCallback.Options singleUse = ClickCallback.Options.builder().uses(1).build();

        boolean isFav = false;
        try {
            isFav = plugin.getDatabaseManager().getPlayerDataRepository()
                    .isFavorite(player.getUniqueId(), shop.getId());
        } catch (Exception ignored) {}

        String favLabel = isFav ? "Unfavorite" : "Favorite";
        NamedTextColor favColor = isFav ? NamedTextColor.GOLD : NamedTextColor.GRAY;

        List<ActionButton> buttons = new ArrayList<>();

        if (shop.getShopType().canBuy()) {
            buttons.add(ActionButton.builder(Component.text("Buy", NamedTextColor.GREEN))
                    .tooltip(Component.text("Price: " + s.formatPrice(shop.getBuyPrice()) + " each"))
                    .action(DialogAction.customClick(
                            (view, audience) -> Bukkit.getScheduler().runTask(plugin, onBuy), singleUse))
                    .build());
        }

        if (shop.getShopType().canSell()) {
            buttons.add(ActionButton.builder(Component.text("Sell", NamedTextColor.RED))
                    .tooltip(Component.text("Price: " + s.formatPrice(shop.getSellPrice()) + " each"))
                    .action(DialogAction.customClick(
                            (view, audience) -> Bukkit.getScheduler().runTask(plugin, onSell), singleUse))
                    .build());
        }

        buttons.add(ActionButton.builder(Component.text(favLabel, favColor))
                .action(DialogAction.customClick(
                        (view, audience) -> Bukkit.getScheduler().runTask(plugin, onFavorite), singleUse))
                .build());

        // Build shop info text
        StringBuilder infoText = new StringBuilder();
        infoText.append("Owner: ").append(shop.getOwnerName());
        infoText.append("\nStock: ").append(shop.isAdmin() ? "Unlimited" : shop.getCurrentStock());
        if (shop.getBuyPrice() != null)
            infoText.append("\nBuy Price: ").append(s.formatPrice(shop.getBuyPrice())).append(" each");
        if (shop.getSellPrice() != null)
            infoText.append("\nSell Price: ").append(s.formatPrice(shop.getSellPrice())).append(" each");

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(Component.text(shop.getOwnerName() + "'s Shop", NamedTextColor.GOLD))
                        .canCloseWithEscape(true)
                        .body(List.<DialogBody>of(
                                DialogBody.item(shop.getItemTemplate()).build(),
                                DialogBody.plainMessage(Component.text(infoText.toString()))
                        ))
                        .build())
                .type(DialogType.multiAction(buttons).build())
        );

        player.showDialog(dialog);
    }

    @Override
    public void showBuyQuantityAndConfirm(Player player, Shop shop, int maxQty, IntConsumer onConfirm, Runnable onCancel) {
        Settings s = plugin.getConfigManager().getSettings();
        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        ClickCallback.Options singleUse = ClickCallback.Options.builder().uses(1).build();

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(Component.text("Buy " + itemName, NamedTextColor.GREEN))
                        .canCloseWithEscape(true)
                        .body(List.<DialogBody>of(
                                DialogBody.item(shop.getItemTemplate()).build(),
                                DialogBody.plainMessage(Component.text(
                                        "Price: " + s.formatPrice(shop.getBuyPrice()) + " each"
                                                + "\nAvailable: " + (shop.isAdmin() ? "Unlimited" : maxQty)))
                        ))
                        .inputs(List.of(
                                DialogInput.numberRange("quantity", Component.text("Quantity"), 1f, (float) Math.max(1, maxQty))
                                        .step(1f)
                                        .initial(1f)
                                        .build()
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Buy", NamedTextColor.GREEN))
                                .action(DialogAction.customClick((view, audience) -> {
                                    Float qty = view.getFloat("quantity");
                                    int quantity = qty != null ? Math.round(qty) : 1;
                                    if (quantity < 1) quantity = 1;
                                    if (quantity > maxQty) quantity = maxQty;
                                    int finalQty = quantity;
                                    Bukkit.getScheduler().runTask(plugin, () -> onConfirm.accept(finalQty));
                                }, singleUse))
                                .build(),
                        ActionButton.builder(Component.text("Cancel", NamedTextColor.RED))
                                .action(DialogAction.customClick(
                                        (view, audience) -> Bukkit.getScheduler().runTask(plugin, onCancel), singleUse))
                                .build()
                ))
        );

        player.showDialog(dialog);
    }

    @Override
    public void showSellQuantityAndConfirm(Player player, Shop shop, int maxQty, IntConsumer onConfirm, Runnable onCancel) {
        Settings s = plugin.getConfigManager().getSettings();
        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        ClickCallback.Options singleUse = ClickCallback.Options.builder().uses(1).build();

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(Component.text("Sell " + itemName, NamedTextColor.RED))
                        .canCloseWithEscape(true)
                        .body(List.<DialogBody>of(
                                DialogBody.item(shop.getItemTemplate()).build(),
                                DialogBody.plainMessage(Component.text(
                                        "Price: " + s.formatPrice(shop.getSellPrice()) + " each"
                                                + "\nYou have: " + maxQty))
                        ))
                        .inputs(List.of(
                                DialogInput.numberRange("quantity", Component.text("Quantity"), 1f, (float) Math.max(1, maxQty))
                                        .step(1f)
                                        .initial(1f)
                                        .build()
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Sell", NamedTextColor.RED))
                                .action(DialogAction.customClick((view, audience) -> {
                                    Float qty = view.getFloat("quantity");
                                    int quantity = qty != null ? Math.round(qty) : 1;
                                    if (quantity < 1) quantity = 1;
                                    if (quantity > maxQty) quantity = maxQty;
                                    int finalQty = quantity;
                                    Bukkit.getScheduler().runTask(plugin, () -> onConfirm.accept(finalQty));
                                }, singleUse))
                                .build(),
                        ActionButton.builder(Component.text("Cancel", NamedTextColor.RED))
                                .action(DialogAction.customClick(
                                        (view, audience) -> Bukkit.getScheduler().runTask(plugin, onCancel), singleUse))
                                .build()
                ))
        );

        player.showDialog(dialog);
    }

    private void showConfirmDialog(Player player, String title, Component bodyText,
                                   ItemStack displayItem, Runnable onConfirm, Runnable onCancel) {
        ClickCallback.Options singleUse = ClickCallback.Options.builder().uses(1).build();

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(Component.text(title, NamedTextColor.GOLD))
                        .canCloseWithEscape(true)
                        .body(List.<DialogBody>of(
                                DialogBody.item(displayItem).build(),
                                DialogBody.plainMessage(bodyText)
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Confirm", NamedTextColor.GREEN))
                                .action(DialogAction.customClick(
                                        (view, audience) -> Bukkit.getScheduler().runTask(plugin, onConfirm),
                                        singleUse))
                                .build(),
                        ActionButton.builder(Component.text("Cancel", NamedTextColor.RED))
                                .action(DialogAction.customClick(
                                        (view, audience) -> Bukkit.getScheduler().runTask(plugin, onCancel),
                                        singleUse))
                                .build()
                ))
        );

        player.showDialog(dialog);
    }
}
