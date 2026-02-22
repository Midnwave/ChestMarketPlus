package com.blockforge.chestmarketplus;

import com.blockforge.chestmarketplus.command.CommandManager;
import com.blockforge.chestmarketplus.config.ConfigManager;
import com.blockforge.chestmarketplus.config.LocaleManager;
import com.blockforge.chestmarketplus.database.DatabaseManager;
import com.blockforge.chestmarketplus.display.DisplayManager;
import com.blockforge.chestmarketplus.economy.EconomyProvider;
import com.blockforge.chestmarketplus.gui.GuiManager;
import com.blockforge.chestmarketplus.hook.HookManager;
import com.blockforge.chestmarketplus.listener.*;
import com.blockforge.chestmarketplus.notification.NotificationManager;
import com.blockforge.chestmarketplus.platform.PlatformDetector;
import com.blockforge.chestmarketplus.protection.TrustManager;
import com.blockforge.chestmarketplus.rating.RatingManager;
import com.blockforge.chestmarketplus.shop.ShopExpiry;
import com.blockforge.chestmarketplus.shop.ShopManager;
import com.blockforge.chestmarketplus.update.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class ChestMarketPlus extends JavaPlugin {

    private static ChestMarketPlus instance;

    private PlatformDetector platformDetector;
    private ConfigManager configManager;
    private LocaleManager localeManager;
    private DatabaseManager databaseManager;
    private HookManager hookManager;
    private EconomyProvider economyProvider;
    private ShopManager shopManager;
    private DisplayManager displayManager;
    private GuiManager guiManager;
    private CommandManager commandManager;
    private NotificationManager notificationManager;
    private TrustManager trustManager;
    private RatingManager ratingManager;
    private ShopExpiry shopExpiry;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        instance = this;

        platformDetector = new PlatformDetector(this);
        getLogger().info("Detected platform: " + platformDetector.getPlatformName());
        if (platformDetector.hasDialogAPI()) {
            getLogger().info("Paper Dialog API detected - using native dialogs");
        } else {
            getLogger().info("Dialog API not available - using inventory GUI fallback");
        }

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        localeManager = new LocaleManager(this, configManager);
        localeManager.loadLocale();

        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        hookManager = new HookManager(this);
        hookManager.setupHooks();

        economyProvider = hookManager.getEconomyProvider();
        getLogger().info("Economy provider: " + economyProvider.getName());

        trustManager = new TrustManager(this);
        shopManager = new ShopManager(this);
        shopManager.loadShops();

        notificationManager = new NotificationManager(this);
        ratingManager = new RatingManager(this);

        displayManager = new DisplayManager(this);
        displayManager.initialize();

        guiManager = new GuiManager(this);

        commandManager = new CommandManager(this);
        commandManager.registerCommands();

        registerListeners();

        shopExpiry = new ShopExpiry(this);
        shopExpiry.startExpiryTask();

        updateChecker = new UpdateChecker(this);
        if (configManager.getSettings().isUpdateCheckerEnabled()) {
            updateChecker.checkForUpdates();
        }

        getLogger().info("ChestMarket+ v" + getDescription().getVersion() + " enabled!");
        getLogger().info("Loaded " + shopManager.getShopCount() + " shops.");
    }

    @Override
    public void onDisable() {
        if (shopExpiry != null) {
            shopExpiry.stopExpiryTask();
        }

        if (displayManager != null) {
            displayManager.removeAllDisplays();
        }

        if (notificationManager != null) {
            notificationManager.saveQueuedNotifications();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("ChestMarket+ disabled.");
        instance = null;
    }

    public void reload() {
        configManager.loadConfig();
        localeManager.loadLocale();

        displayManager.removeAllDisplays();
        shopManager.loadShops();
        displayManager.initialize();

        shopExpiry.stopExpiryTask();
        shopExpiry.startExpiryTask();

        getLogger().info("ChestMarket+ configuration reloaded.");
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new SignListener(this), this);
        pm.registerEvents(new ShopInteractListener(this), this);
        pm.registerEvents(new ChestListener(this), this);
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);
    }

    public static ChestMarketPlus getInstance() {
        return instance;
    }

    public PlatformDetector getPlatformDetector() { return platformDetector; }
    public ConfigManager getConfigManager() { return configManager; }
    public LocaleManager getLocaleManager() { return localeManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public HookManager getHookManager() { return hookManager; }
    public EconomyProvider getEconomyProvider() { return economyProvider; }
    public ShopManager getShopManager() { return shopManager; }
    public DisplayManager getDisplayManager() { return displayManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public CommandManager getCommandManager() { return commandManager; }
    public NotificationManager getNotificationManager() { return notificationManager; }
    public TrustManager getTrustManager() { return trustManager; }
    public RatingManager getRatingManager() { return ratingManager; }
    public ShopExpiry getShopExpiry() { return shopExpiry; }
    public UpdateChecker getUpdateChecker() { return updateChecker; }
}
