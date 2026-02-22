package com.blockforge.chestmarketplus.config;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.List;

public class ConfigManager {

    private final ChestMarketPlus plugin;
    private Settings settings;

    public ConfigManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
        this.settings = new Settings();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        migrateConfig(config);
        Settings s = new Settings();

        s.setConfigVersion(config.getInt("config-version", 1));
        s.setPrefix(config.getString("prefix", "<gray>[<gold>ChestMarket+<gray>] "));
        s.setLanguage(config.getString("language", "en_US"));
        s.setTriggerWords(config.getStringList("shop-creation-trigger-words"));
        if (s.getTriggerWords().isEmpty()) {
            s.setTriggerWords(Arrays.asList("[ChestMarket]", "[ChestShop]", "[Shop]", "[CM]", "[CM+]", "[Market]"));
        }

        s.setUseVault(config.getBoolean("economy.use-vault", true));
        s.setStartingBalance(config.getDouble("economy.starting-balance", 0.0));
        s.setCurrencySymbol(config.getString("economy.currency-symbol", "$"));
        s.setCurrencyName(config.getString("economy.currency-name", "dollars"));
        s.setDecimalPlaces(config.getInt("economy.decimal-places", 2));

        s.setDefaultMaxShops(config.getInt("shops.default-max-shops", 10));
        s.setCreationFee(config.getDouble("shops.creation-fee", 100.0));
        s.setTaxRate(config.getDouble("shops.tax-rate", 5.0));
        s.setGlobalMinPrice(config.getDouble("shops.global-min-price", 0.01));
        s.setGlobalMaxPrice(config.getDouble("shops.global-max-price", 1000000.0));
        s.setChatInputTimeout(config.getInt("shops.chat-input-timeout", 30));
        s.setAllowAllQuantity(config.getBoolean("shops.allow-all-quantity", true));

        s.setExpiryEnabled(config.getBoolean("expiry.enabled", true));
        s.setExpiryDurationDays(config.getInt("expiry.duration-days", 30));
        s.setExpiryWarnDaysBefore(config.getInt("expiry.warn-days-before", 3));
        s.setAutoDeleteExpired(config.getBoolean("expiry.auto-delete-expired", false));

        s.setDisplayEnabled(config.getBoolean("display.enabled", true));
        s.setRenderDistance(config.getInt("display.render-distance", 16));
        s.setItemRotationSpeed(config.getDouble("display.item-rotation-speed", 2.0));
        s.setScrollingTextSpeed(config.getInt("display.scrolling-text-speed", 13));
        s.setOutOfStockText(config.getString("display.out-of-stock-text", "<red><bold>OUT OF STOCK"));

        s.setSignAutoColor(config.getBoolean("signs.auto-color", true));
        s.setRequireCrouchForSign(config.getBoolean("signs.require-crouch", false));
        s.setBuyColor(config.getString("signs.buy-color", "<green>"));
        s.setSellColor(config.getString("signs.sell-color", "<red>"));
        s.setBothColor(config.getString("signs.both-color", "<yellow>"));

        s.setItemBlacklist(config.getStringList("items.blacklist"));
        s.setBlacklistBypassPermission(config.getString("items.blacklist-bypass-permission", "chestmarket.bypass.blacklist"));
        s.setWhitelistEnabled(config.getBoolean("items.whitelist.enabled", false));
        s.setWhitelistMode(config.getString("items.whitelist.mode", "blacklist"));

        s.setChestProtection(config.getBoolean("protection.chest-protection", true));
        s.setAllowChestPeek(config.getBoolean("protection.allow-chest-peek", false));
        s.setAdminBypassPermission(config.getString("protection.admin-bypass-permission", "chestmarket.admin.bypass"));

        s.setWorldGuardEnabled(config.getBoolean("worldguard.enabled", true));
        s.setWorldGuardFlagName(config.getString("worldguard.flag-name", "chest-shop"));
        s.setWorldGuardDefaultValue(config.getBoolean("worldguard.default-value", true));

        s.setWorldRestrictionMode(config.getString("worlds.mode", "blacklist"));
        s.setWorldRestrictionList(config.getStringList("worlds.list"));

        s.setNotificationsDefaultEnabled(config.getBoolean("notifications.default-enabled", true));
        s.setBuySoundName(config.getString("notifications.sound.buy", "ENTITY_EXPERIENCE_ORB_PICKUP"));
        s.setSellSoundName(config.getString("notifications.sound.sell", "ENTITY_VILLAGER_YES"));
        s.setTransactionBurst(config.getBoolean("notifications.particles.transaction-burst", true));
        s.setParticleTypeName(config.getString("notifications.particles.particle-type", "VILLAGER_HAPPY"));
        s.setParticleCount(config.getInt("notifications.particles.count", 15));

        s.setRatingsEnabled(config.getBoolean("ratings.enabled", true));
        s.setRatingsMode(config.getString("ratings.mode", "thumbs"));

        s.setDiscordWebhookUrl(config.getString("discord.webhook-url", ""));
        s.setDiscordAdminEvents(config.getBoolean("discord.admin-events", true));

        s.setUpdateCheckerEnabled(config.getBoolean("update-checker.enabled", true));
        s.setUpdateNotifyInGame(config.getBoolean("update-checker.notify-in-game", true));

        s.setBstatsEnabled(config.getBoolean("bstats.enabled", true));

        this.settings = s;
    }

    private static final int CURRENT_CONFIG_VERSION = 1;

    private void migrateConfig(FileConfiguration config) {
        int version = config.getInt("config-version", 0);
        if (version >= CURRENT_CONFIG_VERSION) return;

        // version 0 -> 1: added prefix and config-version keys
        if (version < 1) {
            if (!config.contains("prefix")) {
                config.set("prefix", "<gray>[<gold>ChestMarket+<gray>] ");
            }
            if (!config.contains("signs.require-crouch")) {
                config.set("signs.require-crouch", false);
            }
            config.set("config-version", 1);
            plugin.saveConfig();
            plugin.getLogger().info("Config migrated to version 1");
        }
    }

    public Settings getSettings() {
        return settings;
    }
}
