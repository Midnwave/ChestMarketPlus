package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInputListener implements Listener {

    private final ChestMarketPlus plugin;
    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    public ChatInputListener(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void awaitInput(Player player, Consumer<String> callback) {
        pending.put(player.getUniqueId(), callback);
    }

    public void cancel(Player player) {
        pending.remove(player.getUniqueId());
    }

    // lowest priority so we cancel before discordsrv or any other chat plugin sees it
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Consumer<String> callback = pending.remove(event.getPlayer().getUniqueId());
        if (callback == null) return;
        event.setCancelled(true);
        String input = event.getMessage().trim();
        plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(input));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }
}
