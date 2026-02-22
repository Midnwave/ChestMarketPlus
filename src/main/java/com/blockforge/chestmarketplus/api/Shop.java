package com.blockforge.chestmarketplus.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Shop {

    private int id;
    private UUID ownerUuid;
    private String ownerName;

    private String world;
    private int x, y, z;

    private int signX, signY, signZ;

    private ShopType shopType;
    private ItemStack itemTemplate;
    private Double buyPrice;
    private Double sellPrice;
    private int maxQuantity;
    private boolean admin;
    private boolean active;
    private long createdAt;
    private Long expiresAt;

    private transient int currentStock;
    private transient Object display;

    public Shop() {}

    public Shop(int id, UUID ownerUuid, String ownerName,
                String world, int x, int y, int z,
                int signX, int signY, int signZ,
                ShopType shopType, ItemStack itemTemplate,
                Double buyPrice, Double sellPrice, int maxQuantity,
                boolean admin, boolean active, long createdAt, Long expiresAt) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.signX = signX;
        this.signY = signY;
        this.signZ = signZ;
        this.shopType = shopType;
        this.itemTemplate = itemTemplate;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.maxQuantity = maxQuantity;
        this.admin = admin;
        this.active = active;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Location getChestLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) return null;
        return new Location(w, x, y, z);
    }

    public Location getSignLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) return null;
        return new Location(w, signX, signY, signZ);
    }

    public boolean isExpired() {
        if (expiresAt == null) return false;
        return System.currentTimeMillis() / 1000 > expiresAt;
    }

    public long getTimeUntilExpiry() {
        if (expiresAt == null) return Long.MAX_VALUE;
        return expiresAt - (System.currentTimeMillis() / 1000);
    }

    public boolean isOutOfStock() {
        if (admin) return false;
        return currentStock <= 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public UUID getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(UUID ownerUuid) { this.ownerUuid = ownerUuid; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getZ() { return z; }
    public void setZ(int z) { this.z = z; }

    public int getSignX() { return signX; }
    public void setSignX(int signX) { this.signX = signX; }

    public int getSignY() { return signY; }
    public void setSignY(int signY) { this.signY = signY; }

    public int getSignZ() { return signZ; }
    public void setSignZ(int signZ) { this.signZ = signZ; }

    public ShopType getShopType() { return shopType; }
    public void setShopType(ShopType shopType) { this.shopType = shopType; }

    public ItemStack getItemTemplate() { return itemTemplate; }
    public void setItemTemplate(ItemStack itemTemplate) { this.itemTemplate = itemTemplate; }

    public Double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(Double buyPrice) { this.buyPrice = buyPrice; }

    public Double getSellPrice() { return sellPrice; }
    public void setSellPrice(Double sellPrice) { this.sellPrice = sellPrice; }

    public int getMaxQuantity() { return maxQuantity; }
    public void setMaxQuantity(int maxQuantity) { this.maxQuantity = maxQuantity; }

    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public Object getDisplay() { return display; }
    public void setDisplay(Object display) { this.display = display; }

    public String getLocationKey() {
        return world + ":" + x + ":" + y + ":" + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shop shop = (Shop) o;
        return id == shop.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
