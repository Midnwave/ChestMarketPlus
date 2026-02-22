package com.blockforge.chestmarketplus.platform;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import org.bukkit.Bukkit;

public class PlatformDetector {

    private final ChestMarketPlus plugin;
    private final boolean isPaper;
    private final boolean hasDialogAPI;
    private final boolean hasDisplayEntities;
    private final String serverVersion;

    public PlatformDetector(ChestMarketPlus plugin) {
        this.plugin = plugin;
        this.serverVersion = Bukkit.getBukkitVersion();

        boolean paperDetected = false;
        try {
            Class.forName("io.papermc.paper.configuration.PaperConfigurations");
            paperDetected = true;
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                paperDetected = true;
            } catch (ClassNotFoundException e2) {
                paperDetected = false;
            }
        }
        this.isPaper = paperDetected;

        boolean dialogDetected = false;
        if (isPaper) {
            try {
                Class.forName("io.papermc.paper.dialog.Dialog");
                dialogDetected = true;
            } catch (ClassNotFoundException e) {
                dialogDetected = false;
            }
        }
        this.hasDialogAPI = dialogDetected;

        boolean displayDetected = false;
        try {
            Class.forName("org.bukkit.entity.TextDisplay");
            displayDetected = true;
        } catch (ClassNotFoundException e) {
            displayDetected = false;
        }
        this.hasDisplayEntities = displayDetected;
    }

    public boolean isPaper() {
        return isPaper;
    }

    public boolean hasDialogAPI() {
        return hasDialogAPI;
    }

    public boolean hasDisplayEntities() {
        return hasDisplayEntities;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public String getPlatformName() {
        if (isPaper) {
            return "Paper " + serverVersion;
        }
        return "Spigot " + serverVersion;
    }
}
