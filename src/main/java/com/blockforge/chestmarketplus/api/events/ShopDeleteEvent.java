package com.blockforge.chestmarketplus.api.events;

import com.blockforge.chestmarketplus.api.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopDeleteEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final Shop shop;

    public ShopDeleteEvent(Player player, Shop shop) {
        this.player = player;
        this.shop = shop;
    }

    public Player getPlayer() { return player; }
    public Shop getShop() { return shop; }

    @Override
    public HandlerList getHandlers() { return HANDLER_LIST; }

    public static HandlerList getHandlerList() { return HANDLER_LIST; }
}
