package com.blockforge.chestmarketplus.gui;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ConfigGui {

    private final ChestMarketPlus plugin;
    private final Player player;

    public ConfigGui(ChestMarketPlus plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54,
                MessageUtils.colorize("&8ChestMarket+ Configuration"));

        Settings s = plugin.getConfigManager().getSettings();

        inv.setItem(0, createNumberItem("&eCreation Fee", s.getCreationFee()));
        inv.setItem(1, createNumberItem("&eTax Rate (%)", s.getTaxRate()));
        inv.setItem(2, createNumberItem("&eMax Shops", s.getDefaultMaxShops()));
        inv.setItem(3, createNumberItem("&eMin Price", s.getGlobalMinPrice()));
        inv.setItem(4, createNumberItem("&eMax Price", s.getGlobalMaxPrice()));
        inv.setItem(5, createNumberItem("&eRender Distance", s.getRenderDistance()));

        inv.setItem(18, createToggle("&bShop Expiry", s.isExpiryEnabled()));
        inv.setItem(19, createToggle("&bHolograms", s.isDisplayEnabled()));
        inv.setItem(20, createToggle("&bChest Protection", s.isChestProtection()));
        inv.setItem(21, createToggle("&bSign Auto-Color", s.isSignAutoColor()));
        inv.setItem(22, createToggle("&bRequire Crouch", s.isRequireCrouchForSign()));
        inv.setItem(23, createToggle("&bNotifications", s.isNotificationsDefaultEnabled()));
        inv.setItem(24, createToggle("&bRatings", s.isRatingsEnabled()));
        inv.setItem(25, createToggle("&bWorldGuard", s.isWorldGuardEnabled()));
        inv.setItem(26, createToggle("&bUpdate Checker", s.isUpdateCheckerEnabled()));
        inv.setItem(27, createToggle("&bChest Peek", s.isAllowChestPeek()));

        inv.setItem(49, createItem(Material.REDSTONE, MessageUtils.colorize("&c&lReload Plugin"),
                MessageUtils.colorize("&7Click to reload configuration")));
        inv.setItem(53, createItem(Material.BARRIER, MessageUtils.colorize("&cClose")));

        player.openInventory(inv);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 53) {
            player.closeInventory();
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            plugin.reload();
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("config-reloaded"));
            return;
        }

        String togglePath = switch (slot) {
            case 18 -> "expiry.enabled";
            case 19 -> "display.enabled";
            case 20 -> "protection.chest-protection";
            case 21 -> "signs.auto-color";
            case 22 -> "signs.require-crouch";
            case 23 -> "notifications.default-enabled";
            case 24 -> "ratings.enabled";
            case 25 -> "worldguard.enabled";
            case 26 -> "update-checker.enabled";
            case 27 -> "protection.allow-chest-peek";
            default -> null;
        };

        if (togglePath != null) {
            boolean current = plugin.getConfig().getBoolean(togglePath, false);
            plugin.getConfig().set(togglePath, !current);
            plugin.saveConfig();
            plugin.getConfigManager().loadConfig();
            // Schedule next tick to avoid inventory close event clearing the GUI registration
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getGuiManager().openConfigGui(player));
            return;
        }

        String numberPath = switch (slot) {
            case 0 -> "shops.creation-fee";
            case 1 -> "shops.tax-rate";
            case 2 -> "shops.default-max-shops";
            case 3 -> "shops.global-min-price";
            case 4 -> "shops.global-max-price";
            case 5 -> "display.render-distance";
            default -> null;
        };

        if (numberPath != null) {
            player.closeInventory();
            String label = switch (slot) {
                case 0 -> "Creation Fee";
                case 1 -> "Tax Rate (%)";
                case 2 -> "Max Shops";
                case 3 -> "Min Price";
                case 4 -> "Max Price";
                case 5 -> "Render Distance";
                default -> "Value";
            };
            boolean isInt = slot == 2 || slot == 5;
            double currentVal = plugin.getConfig().getDouble(numberPath);
            MessageUtils.sendMessage(player, MessageUtils.colorize(
                    "<yellow>Enter new value for <white>" + label + " <gray>(current: <white>"
                    + (isInt ? (int) currentVal : currentVal) + "<gray>) or type <white>cancel<gray>:"));

            final String finalPath = numberPath;
            plugin.getChatInputListener().awaitInput(player, input -> {
                if ("cancel".equalsIgnoreCase(input)) {
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.getGuiManager().openConfigGui(player));
                    return;
                }
                try {
                    if (isInt) {
                        int val = Integer.parseInt(input);
                        plugin.getConfig().set(finalPath, val);
                    } else {
                        double val = Double.parseDouble(input);
                        plugin.getConfig().set(finalPath, val);
                    }
                    plugin.saveConfig();
                    plugin.getConfigManager().loadConfig();
                    MessageUtils.sendMessage(player, MessageUtils.colorize(
                            "<green>" + label + " set to <white>" + input));
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("invalid-price"));
                }
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getGuiManager().openConfigGui(player));
            });
        }
    }

    private ItemStack createToggle(String name, boolean value) {
        Material mat = value ? Material.LIME_DYE : Material.GRAY_DYE;
        String state = value ? "&aEnabled" : "&cDisabled";
        return createItem(mat, MessageUtils.colorize(name),
                MessageUtils.colorize(state),
                MessageUtils.colorize("&7Click to toggle"));
    }

    private ItemStack createNumberItem(String name, double value) {
        return createItem(Material.PAPER, MessageUtils.colorize(name),
                MessageUtils.colorize("&fValue: &e" + value),
                MessageUtils.colorize("&7Click to change"));
    }

    private ItemStack createNumberItem(String name, int value) {
        return createItem(Material.PAPER, MessageUtils.colorize(name),
                MessageUtils.colorize("&fValue: &e" + value),
                MessageUtils.colorize("&7Click to change"));
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
