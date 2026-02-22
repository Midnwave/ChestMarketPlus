package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class PlayerListener implements Listener {

    private final ChestMarketPlus plugin;

    public PlayerListener(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    plugin.getDatabaseManager().getPlayerDataRepository()
                            .ensurePlayer(player.getUniqueId(), player.getName());

                    List<String> pending = plugin.getDatabaseManager().getPlayerDataRepository()
                            .getPendingNotifications(player.getUniqueId());

                    if (!pending.isEmpty()) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!player.isOnline()) return;

                                MessageUtils.sendMessage(player,
                                        plugin.getLocaleManager().getPrefixedMessage("pending-notifications",
                                                "{count}", String.valueOf(pending.size())));

                                for (String msg : pending) {
                                    MessageUtils.sendMessage(player,
                                            plugin.getLocaleManager().getPrefix() + msg);
                                }
                            }
                        }.runTaskLater(plugin, 60L);

                        plugin.getDatabaseManager().getPlayerDataRepository()
                                .clearPendingNotifications(player.getUniqueId());
                    }

                    if (player.hasPermission("chestmarket.notify.update")) {
                        plugin.getUpdateChecker().notifyPlayer(player);
                    }

                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to process player join for " + player.getName(), e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
