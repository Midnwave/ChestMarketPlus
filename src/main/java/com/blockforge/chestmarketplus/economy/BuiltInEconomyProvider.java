package com.blockforge.chestmarketplus.economy;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.database.PlayerDataRepository;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.logging.Level;

public class BuiltInEconomyProvider implements EconomyProvider {

    private final ChestMarketPlus plugin;

    public BuiltInEconomyProvider(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "ChestMarket+ Built-In Economy";
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        try {
            return getRepo().getBalance(player.getUniqueId());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get balance for " + player.getName(), e);
            return 0;
        }
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean withdraw(OfflinePlayer player, double amount) {
        try {
            double balance = getRepo().getBalance(player.getUniqueId());
            if (balance < amount) return false;
            getRepo().setBalance(player.getUniqueId(), balance - amount);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to withdraw from " + player.getName(), e);
            return false;
        }
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        try {
            double balance = getRepo().getBalance(player.getUniqueId());
            getRepo().setBalance(player.getUniqueId(), balance + amount);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to deposit to " + player.getName(), e);
            return false;
        }
    }

    @Override
    public String format(double amount) {
        Settings s = plugin.getConfigManager().getSettings();
        return s.formatPrice(amount);
    }

    private PlayerDataRepository getRepo() {
        return plugin.getDatabaseManager().getPlayerDataRepository();
    }
}
