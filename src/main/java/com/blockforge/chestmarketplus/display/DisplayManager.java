package com.blockforge.chestmarketplus.display;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisplayManager {

    private final ChestMarketPlus plugin;
    private final Map<Integer, HologramDisplay> displays = new ConcurrentHashMap<>();
    private BukkitTask rotationTask;

    public DisplayManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (!plugin.getConfigManager().getSettings().isDisplayEnabled()) return;

        for (Shop shop : plugin.getShopManager().getAllLoadedShops()) {
            createDisplay(shop);
        }

        startRotationTask();
    }

    public void createDisplay(Shop shop) {
        if (!plugin.getConfigManager().getSettings().isDisplayEnabled()) return;
        if (!shop.isActive()) return;

        Location chestLoc = shop.getChestLocation();
        if (chestLoc == null) return;
        if (!chestLoc.getWorld().isChunkLoaded(chestLoc.getBlockX() >> 4, chestLoc.getBlockZ() >> 4)) return;

        // Remove existing display if any
        removeDisplay(shop);

        HologramDisplay display = new HologramDisplay(plugin, shop);

        display.spawn();
        displays.put(shop.getId(), display);
        shop.setDisplay(display);
    }

    public void updateDisplay(Shop shop) {
        HologramDisplay display = displays.get(shop.getId());
        if (display != null) {
            display.update();
        }
    }

    public void removeDisplay(Shop shop) {
        HologramDisplay display = displays.remove(shop.getId());
        if (display != null) {
            display.remove();
            shop.setDisplay(null);
        }
    }

    public void setHologramsVisibleForPlayer(org.bukkit.entity.Player player, boolean visible) {
        for (HologramDisplay display : displays.values()) {
            if (visible) {
                display.showForPlayer(player);
            } else {
                display.hideForPlayer(player);
            }
        }
    }

    public void removeAllDisplays() {
        for (HologramDisplay display : displays.values()) {
            display.remove();
        }
        displays.clear();

        if (rotationTask != null) {
            rotationTask.cancel();
        }
    }

    private void startRotationTask() {
        double speed = plugin.getConfigManager().getSettings().getItemRotationSpeed();
        if (speed <= 0) return;

        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (HologramDisplay display : displays.values()) {
                display.tickRotation();
                display.tickScroll();
            }
        }, 2L, 2L);
    }

    public void onChunkLoad(String world, int chunkX, int chunkZ) {
        for (Shop shop : plugin.getShopManager().getAllLoadedShops()) {
            if (shop.getWorld().equals(world)
                    && (shop.getX() >> 4) == chunkX
                    && (shop.getZ() >> 4) == chunkZ
                    && !displays.containsKey(shop.getId())) {
                createDisplay(shop);
            }
        }
    }

    public void onChunkUnload(String world, int chunkX, int chunkZ) {
        displays.entrySet().removeIf(entry -> {
            Shop shop = plugin.getShopManager().getShopById(entry.getKey());
            if (shop != null && shop.getWorld().equals(world)
                    && (shop.getX() >> 4) == chunkX
                    && (shop.getZ() >> 4) == chunkZ) {
                entry.getValue().remove();
                return true;
            }
            return false;
        });
    }
}
