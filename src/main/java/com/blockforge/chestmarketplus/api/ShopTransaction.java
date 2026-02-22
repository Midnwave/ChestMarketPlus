package com.blockforge.chestmarketplus.api;

import java.util.UUID;

public class ShopTransaction {

    private int id;
    private int shopId;
    private UUID buyerUuid;
    private String buyerName;
    private String action;
    private String itemType;
    private int quantity;
    private double priceTotal;
    private double taxAmount;
    private long createdAt;

    public ShopTransaction() {}

    public ShopTransaction(int shopId, UUID buyerUuid, String buyerName,
                           String action, String itemType, int quantity,
                           double priceTotal, double taxAmount) {
        this.shopId = shopId;
        this.buyerUuid = buyerUuid;
        this.buyerName = buyerName;
        this.action = action;
        this.itemType = itemType;
        this.quantity = quantity;
        this.priceTotal = priceTotal;
        this.taxAmount = taxAmount;
        this.createdAt = System.currentTimeMillis() / 1000;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public UUID getBuyerUuid() { return buyerUuid; }
    public void setBuyerUuid(UUID buyerUuid) { this.buyerUuid = buyerUuid; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceTotal() { return priceTotal; }
    public void setPriceTotal(double priceTotal) { this.priceTotal = priceTotal; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
