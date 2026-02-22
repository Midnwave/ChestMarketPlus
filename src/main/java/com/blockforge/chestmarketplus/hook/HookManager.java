package com.blockforge.chestmarketplus.hook;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.economy.BuiltInEconomyProvider;
import com.blockforge.chestmarketplus.economy.EconomyProvider;
import com.blockforge.chestmarketplus.economy.VaultEconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class HookManager {

    private final ChestMarketPlus plugin;
    private EconomyProvider economyProvider;
    private WorldGuardHook worldGuardHook;

    public HookManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void setupHooks() {
        setupEconomy();
        setupWorldGuard();
    }

    private void setupEconomy() {
        if (plugin.getConfigManager().getSettings().isUseVault()
                && Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try {
                RegisteredServiceProvider<Economy> rsp =
                        Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    Economy economy = rsp.getProvider();
                    economyProvider = new VaultEconomyProvider(economy);
                    plugin.getLogger().info("Hooked into Vault economy: " + economy.getName());
                    return;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Vault found but no economy provider registered.");
            }
        }

        economyProvider = new BuiltInEconomyProvider(plugin);
        plugin.getLogger().info("Using built-in economy system.");
    }

    private void setupWorldGuard() {
        if (!plugin.getConfigManager().getSettings().isWorldGuardEnabled()) return;

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                worldGuardHook = new WorldGuardHook(plugin);
                worldGuardHook.registerFlags();
                plugin.getLogger().info("Hooked into WorldGuard.");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to hook into WorldGuard: " + e.getMessage());
                worldGuardHook = null;
            }
        }
    }

    public EconomyProvider getEconomyProvider() {
        return economyProvider;
    }

    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    public boolean isWorldGuardAvailable() {
        return worldGuardHook != null;
    }
}
