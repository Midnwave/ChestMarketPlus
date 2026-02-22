package com.blockforge.chestmarketplus.api.events;

import com.blockforge.chestmarketplus.api.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopTransactionEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final Shop shop;
    private final String action; // "BUY" or "SELL"
    private final int quantity;
    private final double totalPrice;
    private boolean cancelled;

    public ShopTransactionEvent(Player player, Shop shop, String action, int quantity, double totalPrice) {
        this.player = player;
        this.shop = shop;
        this.action = action;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public Player getPlayer() { return player; }
    public Shop getShop() { return shop; }
    public String getAction() { return action; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @Override
    public HandlerList getHandlers() { return HANDLER_LIST; }

    public static HandlerList getHandlerList() { return HANDLER_LIST; }
}
