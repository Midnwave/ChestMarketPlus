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

        inv.setItem(0, createNumberItem("&eCreation Fee", s.getCreationFee(), "shops.creation-fee"));
        inv.setItem(1, createNumberItem("&eTax Rate", s.getTaxRate(), "shops.tax-rate"));
        inv.setItem(2, createNumberItem("&eMax Shops", s.getDefaultMaxShops(), "shops.default-max-shops"));
        inv.setItem(3, createNumberItem("&eMin Price", s.getGlobalMinPrice(), "shops.global-min-price"));
        inv.setItem(4, createNumberItem("&eMax Price", s.getGlobalMaxPrice(), "shops.global-max-price"));
        inv.setItem(5, createNumberItem("&eRender Distance", s.getRenderDistance(), "display.render-distance"));

        inv.setItem(18, createToggle("&bShop Expiry", s.isExpiryEnabled(), "expiry.enabled"));
        inv.setItem(19, createToggle("&bHolograms", s.isDisplayEnabled(), "display.enabled"));
        inv.setItem(20, createToggle("&bChest Protection", s.isChestProtection(), "protection.chest-protection"));
        inv.setItem(21, createToggle("&bSign Auto-Color", s.isSignAutoColor(), "signs.auto-color"));
        inv.setItem(22, createToggle("&bRequire Crouch", s.isRequireCrouchForSign(), "signs.require-crouch"));
        inv.setItem(23, createToggle("&bNotifications", s.isNotificationsDefaultEnabled(), "notifications.default-enabled"));
        inv.setItem(24, createToggle("&bRatings", s.isRatingsEnabled(), "ratings.enabled"));
        inv.setItem(25, createToggle("&bWorldGuard", s.isWorldGuardEnabled(), "worldguard.enabled"));
        inv.setItem(26, createToggle("&bUpdate Checker", s.isUpdateCheckerEnabled(), "update-checker.enabled"));

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

        // toggle slots
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
            default -> null;
        };

        if (togglePath != null) {
            boolean current = plugin.getConfig().getBoolean(togglePath, false);
            plugin.getConfig().set(togglePath, !current);
            plugin.saveConfig();
            plugin.getConfigManager().loadConfig();
            open();
            return;
        }

        // number value slots - cycle through values
        switch (slot) {
            case 0 -> cycleDouble("shops.creation-fee", new double[]{0, 50, 100, 250, 500, 1000});
            case 1 -> cycleDouble("shops.tax-rate", new double[]{0, 1, 2, 5, 10, 15, 20});
            case 2 -> cycleInt("shops.default-max-shops", new int[]{5, 10, 15, 20, 25, 50, 100});
            case 3 -> cycleDouble("shops.global-min-price", new double[]{0.01, 0.1, 1.0, 5.0, 10.0});
            case 4 -> cycleDouble("shops.global-max-price", new double[]{1000, 10000, 100000, 1000000, 10000000});
            case 5 -> cycleInt("display.render-distance", new int[]{8, 16, 24, 32, 48, 64});
        }
    }

    private void cycleDouble(String path, double[] values) {
        double current = plugin.getConfig().getDouble(path);
        double next = values[0];
        for (int i = 0; i < values.length; i++) {
            if (current == values[i] && i + 1 < values.length) {
                next = values[i + 1];
                break;
            } else if (current == values[values.length - 1]) {
                next = values[0];
                break;
            }
        }
        plugin.getConfig().set(path, next);
        plugin.saveConfig();
        plugin.getConfigManager().loadConfig();
        open();
    }

    private void cycleInt(String path, int[] values) {
        int current = plugin.getConfig().getInt(path);
        int next = values[0];
        for (int i = 0; i < values.length; i++) {
            if (current == values[i] && i + 1 < values.length) {
                next = values[i + 1];
                break;
            } else if (current == values[values.length - 1]) {
                next = values[0];
                break;
            }
        }
        plugin.getConfig().set(path, next);
        plugin.saveConfig();
        plugin.getConfigManager().loadConfig();
        open();
    }

    private ItemStack createToggle(String name, boolean value, String path) {
        Material mat = value ? Material.LIME_DYE : Material.GRAY_DYE;
        String state = value ? "&aEnabled" : "&cDisabled";
        return createItem(mat, MessageUtils.colorize(name),
                MessageUtils.colorize(state),
                MessageUtils.colorize("&7Click to toggle"));
    }

    private ItemStack createNumberItem(String name, double value, String path) {
        return createItem(Material.PAPER, MessageUtils.colorize(name),
                MessageUtils.colorize("&fValue: &e" + value),
                MessageUtils.colorize("&7Click to cycle"));
    }

    private ItemStack createNumberItem(String name, int value, String path) {
        return createItem(Material.PAPER, MessageUtils.colorize(name),
                MessageUtils.colorize("&fValue: &e" + value),
                MessageUtils.colorize("&7Click to cycle"));
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
