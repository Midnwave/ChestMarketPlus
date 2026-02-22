package com.blockforge.chestmarketplus.dialog;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.util.ItemUtils;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
