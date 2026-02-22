package com.blockforge.chestmarketplus.config;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LocaleManager {

    private final ChestMarketPlus plugin;
    private final ConfigManager configManager;
    private final Map<String, String> messages = new HashMap<>();
    private String prefix = "";

    public LocaleManager(ChestMarketPlus plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void loadLocale() {
        messages.clear();
        String language = configManager.getSettings().getLanguage();

        saveDefaultLocale("en_US");
        saveDefaultLocale("es_ES");

        File localeFile = new File(plugin.getDataFolder(), "locale/" + language + ".yml");
        if (!localeFile.exists()) {
            plugin.getLogger().warning("Locale file '" + language + ".yml' not found, falling back to en_US.");
            localeFile = new File(plugin.getDataFolder(), "locale/en_US.yml");
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(localeFile);

        for (String key : yaml.getKeys(true)) {
            if (yaml.isString(key)) {
                messages.put(key, yaml.getString(key));
            }
        }

        prefix = messages.getOrDefault("prefix", "<gray>[<gold>ChestMarket+<gray>] ");
    }

    private void saveDefaultLocale(String name) {
        File localeDir = new File(plugin.getDataFolder(), "locale");
        if (!localeDir.exists()) {
            localeDir.mkdirs();
        }

        File localeFile = new File(localeDir, name + ".yml");
        if (!localeFile.exists()) {
            InputStream resource = plugin.getResource("locale/" + name + ".yml");
            if (resource != null) {
                plugin.saveResource("locale/" + name + ".yml", false);
            }
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "<red>Missing message: " + key);
    }

    public String getMessage(String key, Object... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String placeholder = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            msg = msg.replace(placeholder, value);
        }
        return msg;
    }

    public String getPrefixedMessage(String key) {
        return prefix + getMessage(key);
    }

    public String getPrefixedMessage(String key, Object... replacements) {
        return prefix + getMessage(key, replacements);
    }

    public String getPrefix() {
        return prefix;
    }
}
