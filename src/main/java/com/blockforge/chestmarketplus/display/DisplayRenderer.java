package com.blockforge.chestmarketplus.display;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class DisplayRenderer implements Listener {

    private final ChestMarketPlus plugin;

    public DisplayRenderer(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        String world = event.getWorld().getName();
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        plugin.getDisplayManager().onChunkLoad(world, chunkX, chunkZ);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        String world = event.getWorld().getName();
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        plugin.getDisplayManager().onChunkUnload(world, chunkX, chunkZ);
    }
}
