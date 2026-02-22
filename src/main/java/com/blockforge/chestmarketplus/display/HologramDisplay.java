package com.blockforge.chestmarketplus.display;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class HologramDisplay {

    private final ChestMarketPlus plugin;
    private final Shop shop;

    private Entity textDisplay;
    private Entity itemDisplay;

    private ArmorStand textStand;
    private ArmorStand nameStand;
    private Item droppedItem;

    private float rotationAngle = 0;
    private boolean useDisplayEntities;

    public HologramDisplay(ChestMarketPlus plugin, Shop shop) {
        this.plugin = plugin;
        this.shop = shop;
        this.useDisplayEntities = plugin.getPlatformDetector().hasDisplayEntities();
    }

    public void spawn() {
        Location chestLoc = shop.getChestLocation();
        if (chestLoc == null) return;

        if (useDisplayEntities) {
            spawnDisplayEntities(chestLoc);
        } else {
            spawnArmorStands(chestLoc);
        }
    }

    private void spawnDisplayEntities(Location chestLoc) {
        try {
            Location textLoc = chestLoc.clone().add(0.5, 1.1, 0.5);
            Location itemLoc = chestLoc.clone().add(0.5, 2.2, 0.5);

            textDisplay = chestLoc.getWorld().spawnEntity(textLoc, EntityType.TEXT_DISPLAY);
            if (textDisplay instanceof org.bukkit.entity.TextDisplay td) {
                td.setText(MessageUtils.colorize(buildDisplayText()));
                td.setBillboard(org.bukkit.entity.Display.Billboard.VERTICAL);
                td.setSeeThrough(false);
                td.setDefaultBackground(false);
                td.setShadowed(true);
                td.setPersistent(false);
            }

            itemDisplay = chestLoc.getWorld().spawnEntity(itemLoc, EntityType.ITEM_DISPLAY);
            if (itemDisplay instanceof org.bukkit.entity.ItemDisplay id) {
                id.setItemStack(shop.getItemTemplate().clone());
                id.setBillboard(org.bukkit.entity.Display.Billboard.VERTICAL);
                id.setPersistent(false);
                org.bukkit.util.Transformation t = id.getTransformation();
                id.setTransformation(new org.bukkit.util.Transformation(
                        t.getTranslation(),
                        t.getLeftRotation(),
                        new org.joml.Vector3f(0.5f, 0.5f, 0.5f),
                        t.getRightRotation()
                ));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn display entities for shop #" + shop.getId()
                    + ", falling back to armor stands: " + e.getMessage());
            useDisplayEntities = false;
            spawnArmorStands(chestLoc);
        }
    }

    private void spawnArmorStands(Location chestLoc) {
        Location textLoc = chestLoc.clone().add(0.5, 1.1, 0.5);
        Location nameLoc = chestLoc.clone().add(0.5, 0.8, 0.5);

        textStand = (ArmorStand) chestLoc.getWorld().spawnEntity(textLoc, EntityType.ARMOR_STAND);
        configureArmorStand(textStand);
        textStand.setCustomName(MessageUtils.colorize(buildDisplayText()));
        textStand.setCustomNameVisible(true);

        nameStand = (ArmorStand) chestLoc.getWorld().spawnEntity(nameLoc, EntityType.ARMOR_STAND);
        configureArmorStand(nameStand);
        nameStand.setCustomName(MessageUtils.colorize("&7" + shop.getOwnerName()));
        nameStand.setCustomNameVisible(true);

        Location itemLoc = chestLoc.clone().add(0.5, 2.3, 0.5);
        ItemStack displayItem = shop.getItemTemplate().clone();
        displayItem.setAmount(1);
        droppedItem = chestLoc.getWorld().dropItem(itemLoc, displayItem);
        droppedItem.setPickupDelay(Integer.MAX_VALUE);
        droppedItem.setVelocity(new Vector(0, 0, 0));
        droppedItem.setGravity(false);
        droppedItem.setPersistent(false);
        droppedItem.setCustomNameVisible(false);
    }

    private void configureArmorStand(ArmorStand stand) {
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setInvulnerable(true);
        stand.setPersistent(false);
        stand.setCanPickupItems(false);
    }

    public void update() {
        String text = buildDisplayText();

        if (useDisplayEntities && textDisplay instanceof org.bukkit.entity.TextDisplay td) {
            td.setText(MessageUtils.colorize(text));

            if (itemDisplay instanceof org.bukkit.entity.ItemDisplay id) {
                if (shop.isOutOfStock() && !shop.isAdmin()) {
                    id.setItemStack(new ItemStack(org.bukkit.Material.BARRIER));
                } else {
                    id.setItemStack(shop.getItemTemplate().clone());
                }
            }
        } else if (textStand != null) {
            textStand.setCustomName(MessageUtils.colorize(text));
        }
    }

    public void tickRotation() {
        if (!useDisplayEntities) return;
        if (!(itemDisplay instanceof org.bukkit.entity.ItemDisplay id)) return;

        float speed = (float) plugin.getConfigManager().getSettings().getItemRotationSpeed();
        rotationAngle += speed;
        if (rotationAngle >= 360) rotationAngle -= 360;

        try {
            org.bukkit.util.Transformation t = id.getTransformation();
            float rad = (float) Math.toRadians(rotationAngle);
            org.joml.Quaternionf rotation = new org.joml.Quaternionf().rotateY(rad);
            id.setTransformation(new org.bukkit.util.Transformation(
                    t.getTranslation(),
                    rotation,
                    t.getScale(),
                    t.getRightRotation()
            ));
        } catch (Exception ignored) {}
    }

    public void remove() {
        if (textDisplay != null && !textDisplay.isDead()) textDisplay.remove();
        if (itemDisplay != null && !itemDisplay.isDead()) itemDisplay.remove();
        if (textStand != null && !textStand.isDead()) textStand.remove();
        if (nameStand != null && !nameStand.isDead()) nameStand.remove();
        if (droppedItem != null && !droppedItem.isDead()) droppedItem.remove();
    }

    public void showForPlayer(org.bukkit.entity.Player player) {
        if (textDisplay != null) player.showEntity(plugin, textDisplay);
        if (itemDisplay != null) player.showEntity(plugin, itemDisplay);
        if (textStand != null) player.showEntity(plugin, textStand);
        if (nameStand != null) player.showEntity(plugin, nameStand);
        if (droppedItem != null) player.showEntity(plugin, droppedItem);
    }

    public void hideForPlayer(org.bukkit.entity.Player player) {
        if (textDisplay != null) player.hideEntity(plugin, textDisplay);
        if (itemDisplay != null) player.hideEntity(plugin, itemDisplay);
        if (textStand != null) player.hideEntity(plugin, textStand);
        if (nameStand != null) player.hideEntity(plugin, nameStand);
        if (droppedItem != null) player.hideEntity(plugin, droppedItem);
    }

    private String buildDisplayText() {
        Settings settings = plugin.getConfigManager().getSettings();
        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());

        if (shop.isOutOfStock() && !shop.isAdmin()) {
            return MessageUtils.colorize(settings.getOutOfStockText()) + "\n"
                    + MessageUtils.colorize("<gray>" + itemName + "\n")
                    + MessageUtils.colorize("<gray>" + shop.getOwnerName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(MessageUtils.colorize("<white><bold>" + itemName)).append("\n");

        if (shop.getBuyPrice() != null) {
            sb.append(MessageUtils.colorize("<green>B: " + settings.formatPrice(shop.getBuyPrice())));
        }
        if (shop.getBuyPrice() != null && shop.getSellPrice() != null) {
            sb.append(MessageUtils.colorize(" <gray>| "));
        }
        if (shop.getSellPrice() != null) {
            sb.append(MessageUtils.colorize("<red>S: " + settings.formatPrice(shop.getSellPrice())));
        }
        sb.append("\n");

        String stockText = shop.isAdmin() ? "Unlimited" : String.valueOf(shop.getCurrentStock());
        sb.append(MessageUtils.colorize("<gray>Stock: <white>" + stockText)).append("\n");
        sb.append(MessageUtils.colorize("<gray>" + shop.getOwnerName()));

        return sb.toString();
    }
}
