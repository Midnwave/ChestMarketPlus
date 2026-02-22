package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.gui.ShopCreationGui;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SignListener implements Listener {

    private final ChestMarketPlus plugin;

    public SignListener(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block signBlock = event.getBlock();

        // prevent editing signs that belong to existing shops
        Shop existing = plugin.getShopManager().getShopBySignLocation(signBlock.getLocation());
        if (existing != null) {
            event.setCancelled(true);
            return;
        }

        String line0 = event.getLine(0);
        if (line0 == null) return;

        Settings settings = plugin.getConfigManager().getSettings();
        if (!isTriggerWord(line0.trim(), settings.getTriggerWords())) return;

        // check crouch requirement
        if (settings.isRequireCrouchForSign() && !player.isSneaking()) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("must-crouch-sign"));
            event.setCancelled(true);
            return;
        }

        Location chestLoc = findAdjacentChest(signBlock);
        if (chestLoc == null) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("no-chest-adjacent"));
            event.setCancelled(true);
            return;
        }

        if (plugin.getShopManager().getShopByLocation(chestLoc) != null) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("shop-already-exists"));
            event.setCancelled(true);
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("hold-item"));
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("chestmarket.create")) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("no-permission"));
            event.setCancelled(true);
            return;
        }

        // cancel sign text, we will set it after creation
        event.setCancelled(true);

        // open creation gui or dialog
        ItemStack template = itemInHand.clone();
        template.setAmount(1);
        Location signLoc = signBlock.getLocation();

        ShopCreationGui gui = new ShopCreationGui(plugin, player, template, chestLoc, signLoc);
        plugin.getGuiManager().openCreationGui(player, gui);
    }

    private boolean isTriggerWord(String input, List<String> triggers) {
        String cleaned = input.replace("[", "").replace("]", "").trim();
        for (String trigger : triggers) {
            String triggerClean = trigger.replace("[", "").replace("]", "").trim();
            if (cleaned.equalsIgnoreCase(triggerClean)) return true;
        }
        return false;
    }

    private Location findAdjacentChest(Block signBlock) {
        if (signBlock.getBlockData() instanceof WallSign wallSign) {
            Block attached = signBlock.getRelative(wallSign.getFacing().getOppositeFace());
            if (attached.getState() instanceof Chest) {
                return attached.getLocation();
            }
        }

        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        for (BlockFace face : faces) {
            Block adjacent = signBlock.getRelative(face);
            if (adjacent.getState() instanceof Chest) {
                return adjacent.getLocation();
            }
        }
        return null;
    }
}
