package com.blockforge.chestmarketplus.shop;

import com.blockforge.chestmarketplus.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ItemMatcher {

    private ItemMatcher() {}

    public static boolean matches(ItemStack template, ItemStack candidate) {
        return ItemUtils.isSimilarExact(template, candidate);
    }

    public static boolean isBlacklisted(Material material, java.util.List<String> blacklist) {
        if (blacklist == null || blacklist.isEmpty()) return false;
        String materialName = material.name();
        for (String entry : blacklist) {
            if (entry.equalsIgnoreCase(materialName)) return true;
        }
        return false;
    }

    public static boolean isWhitelisted(Material material, java.util.List<String> whitelist) {
        if (whitelist == null || whitelist.isEmpty()) return true; // Empty whitelist = allow all
        String materialName = material.name();
        for (String entry : whitelist) {
            if (entry.equalsIgnoreCase(materialName)) return true;
        }
        return false;
    }
}
