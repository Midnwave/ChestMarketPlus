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

    // -------------------------------------------------------------------------
    // Shulker-box-aware stock helpers
    // -------------------------------------------------------------------------

    /** True if the given material is any colour of shulker box. */
    public static boolean isShulkerBox(Material material) {
        return org.bukkit.Tag.SHULKER_BOXES.isTagged(material);
    }

    /**
     * Opens the virtual inventory stored inside a shulker box ItemStack.
     * Returns null if the item has no BlockStateMeta or is not a ShulkerBox.
     */
    private static org.bukkit.inventory.Inventory getShulkerInventory(ItemStack item) {
        if (item == null || !isShulkerBox(item.getType())) return null;
        if (!(item.getItemMeta() instanceof org.bukkit.inventory.meta.BlockStateMeta bsm)) return null;
        if (!(bsm.getBlockState() instanceof org.bukkit.block.ShulkerBox shulker)) return null;
        return shulker.getInventory();
    }

    /**
     * Returns true if every non-air slot in the shulker inventory contains
     * exactly the template item (and at least one slot is non-empty).
     * Mixed shulker boxes (containing anything else) return false.
     */
    private static boolean isShulkerPurelyTemplate(org.bukkit.inventory.Inventory shulkerInv, ItemStack template) {
        boolean hasAny = false;
        for (ItemStack item : shulkerInv.getContents()) {
            if (item == null || item.getType().isAir()) continue;
            if (!isSimilarExact(item, template)) return false;
            hasAny = true;
        }
        return hasAny;
    }

    /**
     * Like {@link #countMatchingItems} but also counts items stored inside
     * shulker boxes that contain only the template item.
     */
    public static int countMatchingItemsWithShulkers(org.bukkit.inventory.Inventory inventory, ItemStack template) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;
            if (isSimilarExact(item, template)) {
                count += item.getAmount();
            } else if (isShulkerBox(item.getType())) {
                org.bukkit.inventory.Inventory shulkerInv = getShulkerInventory(item);
                if (shulkerInv != null && isShulkerPurelyTemplate(shulkerInv, template)) {
                    count += countMatchingItems(shulkerInv, template);
                }
            }
        }
        return count;
    }

    /**
     * Like {@link #removeMatchingItems} but, after draining loose items, also
     * drains from shulker boxes in the inventory that contain only the template
     * item.  The shulker box item's NBT is updated in-place so the chest
     * reflects the correct remaining contents.
     */
    public static int removeMatchingItemsWithShulkers(org.bukkit.inventory.Inventory inventory,
                                                      ItemStack template, int amount) {
        // Phase 1 – drain loose items first
        int removed = removeMatchingItems(inventory, template, amount);
        int remaining = amount - removed;
        if (remaining <= 0) return amount;

        // Phase 2 – drain from qualifying shulker boxes
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack slotItem = contents[i];
            if (slotItem == null || !isShulkerBox(slotItem.getType())) continue;
            if (!(slotItem.getItemMeta() instanceof org.bukkit.inventory.meta.BlockStateMeta bsm)) continue;
            if (!(bsm.getBlockState() instanceof org.bukkit.block.ShulkerBox shulker)) continue;
            org.bukkit.inventory.Inventory shulkerInv = shulker.getInventory();
            if (!isShulkerPurelyTemplate(shulkerInv, template)) continue;

            int drained = removeMatchingItems(shulkerInv, template, remaining);
            remaining -= drained;

            // Write updated inventory back into the item NBT and replace slot
            bsm.setBlockState(shulker);
            slotItem.setItemMeta(bsm);
            inventory.setItem(i, slotItem);
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
