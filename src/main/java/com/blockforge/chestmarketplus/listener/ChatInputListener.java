package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInputListener implements Listener {

    private final ChestMarketPlus plugin;
    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> timeoutTasks = new ConcurrentHashMap<>();

    public ChatInputListener(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void awaitInput(Player player, Consumer<String> callback) {
        pending.put(player.getUniqueId(), callback);

        int timeoutSeconds = plugin.getConfigManager().getSettings().getChatInputTimeout();
        if (timeoutSeconds > 0) {
            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Consumer<String> expired = pending.remove(player.getUniqueId());
                timeoutTasks.remove(player.getUniqueId());
                if (expired != null && player.isOnline()) {
                    MessageUtils.sendMessage(player,
                            plugin.getLocaleManager().getPrefixedMessage("chat-input-timeout"));
                }
            }, timeoutSeconds * 20L);
            timeoutTasks.put(player.getUniqueId(), task);
        }
    }

    public void cancel(Player player) {
        pending.remove(player.getUniqueId());
        BukkitTask task = timeoutTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    // lowest priority so we cancel before discordsrv or any other chat plugin sees it
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Consumer<String> callback = pending.remove(event.getPlayer().getUniqueId());
        if (callback == null) return;

        // Input arrived — cancel the timeout
        BukkitTask task = timeoutTasks.remove(event.getPlayer().getUniqueId());
        if (task != null) task.cancel();

        event.setCancelled(true);
        String input = event.getMessage().trim();
        plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(input));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancel(event.getPlayer());
    }
}
