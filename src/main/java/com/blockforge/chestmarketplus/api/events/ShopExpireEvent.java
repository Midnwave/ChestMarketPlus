package com.blockforge.chestmarketplus.api.events;

import com.blockforge.chestmarketplus.api.Shop;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopExpireEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Shop shop;

    public ShopExpireEvent(Shop shop) {
        this.shop = shop;
    }

    public Shop getShop() { return shop; }

    @Override
    public HandlerList getHandlers() { return HANDLER_LIST; }

    public static HandlerList getHandlerList() { return HANDLER_LIST; }
}
