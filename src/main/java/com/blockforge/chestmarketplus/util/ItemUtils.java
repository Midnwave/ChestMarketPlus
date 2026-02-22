package com.blockforge.chestmarketplus.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public final class ItemUtils {

    private ItemUtils() {}

    public static String serializeItemStack(ItemStack item) {
        if (item == null) return "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);
            boos.writeObject(item);
            boos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack", e);
        }
    }

    public static ItemStack deserializeItemStack(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);
            ItemStack item = (ItemStack) bois.readObject();
            bois.close();
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ItemStack", e);
        }
    }

    public static String getDisplayName(ItemStack item) {
        if (item == null) return "Unknown";
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }
        return formatMaterialName(item.getType());
    }

    public static String formatMaterialName(Material material) {
        String name = material.name().replace('_', ' ');
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(' ');
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    public static boolean isSimilarExact(ItemStack a, ItemStack b) {
        if (a == null || b == null) return a == b;
        if (a.getType() != b.getType()) return false;

        // compare full item meta for exact match including name, lore, enchants, custom model data, nbt
        ItemMeta metaA = a.hasItemMeta() ? a.getItemMeta() : null;
        ItemMeta metaB = b.hasItemMeta() ? b.getItemMeta() : null;

        if (metaA == null && metaB == null) return true;
        if (metaA == null || metaB == null) return false;

        return metaA.equals(metaB);
    }

    public static int countMatchingItems(org.bukkit.inventory.Inventory inventory, ItemStack template) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && isSimilarExact(item, template)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static int removeMatchingItems(org.bukkit.inventory.Inventory inventory, ItemStack template, int amount) {
        int remaining = amount;
        ItemStack[] contents = inventory.getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && isSimilarExact(item, template)) {
                int toRemove = Math.min(remaining, item.getAmount());
                if (toRemove >= item.getAmount()) {
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - toRemove);
                }
                remaining -= toRemove;
            }
        }

        return amount - remaining;
    }

    public static int addItems(org.bukkit.inventory.Inventory inventory, ItemStack template, int amount) {
        int remaining = amount;

        while (remaining > 0) {
            ItemStack toAdd = template.clone();
            int stackSize = Math.min(remaining, template.getMaxStackSize());
            toAdd.setAmount(stackSize);

            var leftovers = inventory.addItem(toAdd);
            if (leftovers.isEmpty()) {
                remaining -= stackSize;
            } else {
                // some items couldn't fit
                int leftover = leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
                remaining -= (stackSize - leftover);
                break;
            }
        }

        return amount - remaining;
    }
}
