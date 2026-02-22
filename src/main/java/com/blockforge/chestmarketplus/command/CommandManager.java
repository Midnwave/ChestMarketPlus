package com.blockforge.chestmarketplus.command;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import org.bukkit.command.PluginCommand;

public class CommandManager {

    private final ChestMarketPlus plugin;

    public CommandManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        ChestMarketCommand mainCommand = new ChestMarketCommand(plugin);
        PluginCommand cmd = plugin.getCommand("cm");
        if (cmd != null) {
            cmd.setExecutor(mainCommand);
            cmd.setTabCompleter(mainCommand);
        }
    }
}
