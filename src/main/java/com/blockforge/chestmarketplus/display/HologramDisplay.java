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

    /** Max visible characters in the scrolling name window. */
    private static final int SCROLL_WIDTH = 16;

    private final ChestMarketPlus plugin;
    private final Shop shop;

    private Entity textDisplay;
    private Entity itemDisplay;

    private ArmorStand textStand;
    private ArmorStand nameStand;
    private Item droppedItem;

    private float rotationAngle = 0;
    private boolean useDisplayEntities;

    /** Current scroll start position (in stripped characters). */
    private int scrollPos = 0;
    /** Counts task invocations for scroll timing. */
    private int scrollTick = 0;

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
                td.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                td.setSeeThrough(false);
                td.setDefaultBackground(false);
                td.setShadowed(true);
                td.setPersistent(false);
                // Scale text to a readable size (default is too large)
                org.bukkit.util.Transformation tt = td.getTransformation();
                td.setTransformation(new org.bukkit.util.Transformation(
                        tt.getTranslation(), tt.getLeftRotation(),
                        new org.joml.Vector3f(0.55f, 0.55f, 0.55f),
                        tt.getRightRotation()));
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

    /**
     * Advances the scroll ticker. Called every 2 ticks by DisplayManager.
     * Triggers a display update when the configured scroll delay elapses.
     */
    public void tickScroll() {
        int speed = plugin.getConfigManager().getSettings().getScrollingTextSpeed();
        if (speed <= 0) return;

        String rawName = stripFormatting(ItemUtils.getDisplayName(shop.getItemTemplate()));
        if (rawName.length() <= SCROLL_WIDTH) return;

        // Task runs every 2 ticks; convert config speed (ticks) to call count
        int callsPerStep = Math.max(1, speed / 2);
        scrollTick++;
        if (scrollTick >= callsPerStep) {
            scrollTick = 0;
            scrollPos = (scrollPos + 1) % (rawName.length() + 3); // +3 for " | " separator
            update();
        }
    }

    private String buildDisplayText() {
        Settings settings = plugin.getConfigManager().getSettings();
        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        String displayName = getScrolledName(itemName);

        if (shop.isOutOfStock() && !shop.isAdmin()) {
            return settings.getOutOfStockText() + "\n"
                    + MessageUtils.colorize("<gray>" + displayName + "\n")
                    + MessageUtils.colorize("<gray>" + shop.getOwnerName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(MessageUtils.colorize("<white><bold>" + displayName)).append("\n");

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

    /**
     * Returns a SCROLL_WIDTH-wide scrolling window of the item name when it is
     * longer than the window; returns the full name otherwise.
     */
    private String getScrolledName(String name) {
        String raw = stripFormatting(name);
        if (raw.length() <= SCROLL_WIDTH) return name;

        // Seamless loop: "Waxed Weathered Cut... | Waxed Weathered Cut..."
        String loop = raw + " | " + raw;
        int start = scrollPos % (raw.length() + 3);
        int end = Math.min(start + SCROLL_WIDTH, loop.length());
        return loop.substring(start, end);
    }

    /** Strips legacy §x color codes and MiniMessage &lt;tags&gt; for length measurement. */
    private String stripFormatting(String s) {
        return s.replaceAll("§.", "").replaceAll("<[^>]+>", "").trim();
    }
}
