package com.blockforge.chestmarketplus.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class LocationUtils {

    private LocationUtils() {}

    public static String toKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public static Location fromKey(String key) {
        String[] parts = key.split(":");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }

    public static String formatLocation(Location loc) {
        return loc.getWorld().getName() + " " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    public static boolean isAdjacentToChest(Location signLoc, Location chestLoc) {
        if (!signLoc.getWorld().equals(chestLoc.getWorld())) return false;
        int dx = Math.abs(signLoc.getBlockX() - chestLoc.getBlockX());
        int dy = Math.abs(signLoc.getBlockY() - chestLoc.getBlockY());
        int dz = Math.abs(signLoc.getBlockZ() - chestLoc.getBlockZ());
        // sign must be directly adjacent, not diagonal
        return (dx + dy + dz) == 1;
    }
}
