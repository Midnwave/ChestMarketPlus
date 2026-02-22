package com.blockforge.chestmarketplus.listener;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.api.ShopType;
import com.blockforge.chestmarketplus.config.Settings;
import com.blockforge.chestmarketplus.shop.ShopCreator;
import com.blockforge.chestmarketplus.util.ItemUtils;
import com.blockforge.chestmarketplus.util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.entity.Player;

import java.util.List;

public class SignListener implements Listener {

    private final ChestMarketPlus plugin;

    public SignListener(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String line0 = event.getLine(0);
        if (line0 == null) return;

        String cleaned = line0.trim();
        Settings settings = plugin.getConfigManager().getSettings();
        List<String> triggers = settings.getTriggerWords();

        boolean isTrigger = false;
        for (String trigger : triggers) {
            String triggerClean = trigger.replace("[", "").replace("]", "").trim();
            String lineClean = cleaned.replace("[", "").replace("]", "").trim();
            if (lineClean.equalsIgnoreCase(triggerClean)) {
                isTrigger = true;
                break;
            }
        }

        if (!isTrigger) return;

        Block signBlock = event.getBlock();
        Location chestLoc = findAdjacentChest(signBlock);

        if (chestLoc == null) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("no-chest-adjacent"));
            event.setCancelled(true);
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("hold-item"));
            event.setCancelled(true);
            return;
        }

        String line1 = event.getLine(1);
        String line2 = event.getLine(2);
        String line3 = event.getLine(3);

        double buyPrice = parsePrice(line2);
        double sellPrice = parsePrice(line3);

        ShopType type;
        if (buyPrice > 0 && sellPrice > 0) {
            type = ShopType.BUY_SELL;
        } else if (buyPrice > 0) {
            type = ShopType.BUY;
        } else if (sellPrice > 0) {
            type = ShopType.SELL;
        } else {
            buyPrice = parsePrice(line1);
            if (buyPrice > 0) {
                type = ShopType.BUY;
            } else {
                MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("invalid-price"));
                event.setCancelled(true);
                return;
            }
        }

        ShopCreator creator = new ShopCreator(plugin);
        ShopCreator.CreateResult result = creator.createFromSign(
                player, type, buyPrice, sellPrice, chestLoc, signBlock.getLocation());

        if (!result.isSuccess()) {
            MessageUtils.sendMessage(player, result.getErrorMessage());
            event.setCancelled(true);
            return;
        }

        Shop shop = result.getShop();

        String itemName = ItemUtils.getDisplayName(shop.getItemTemplate());
        String colorTag = getShopColorTag(shop.getShopType(), settings);

        event.setLine(0, MessageUtils.colorize(colorTag + "[ChestMarket+]"));
        event.setLine(1, MessageUtils.colorize("<white>" + truncate(itemName, 15)));
        event.setLine(2, shop.getBuyPrice() != null
                ? MessageUtils.colorize("<green>B: " + settings.getCurrencySymbol() + String.format("%.2f", shop.getBuyPrice()))
                : MessageUtils.colorize("<gray>B: N/A"));
        event.setLine(3, shop.getSellPrice() != null
                ? MessageUtils.colorize("<red>S: " + settings.getCurrencySymbol() + String.format("%.2f", shop.getSellPrice()))
                : MessageUtils.colorize("<gray>S: N/A"));

        MessageUtils.sendMessage(player, plugin.getLocaleManager().getPrefixedMessage("shop-created",
                "{item}", itemName,
                "{price}", shop.getBuyPrice() != null
                        ? settings.formatPrice(shop.getBuyPrice())
                        : settings.formatPrice(shop.getSellPrice())));
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

    private double parsePrice(String line) {
        if (line == null || line.trim().isEmpty()) return 0;
        String cleaned = line.trim()
                .replaceAll("(?i)^[BS]:\\s*", "")
                .replace("$", "")
                .replace(",", "")
                .trim();
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getShopColorTag(ShopType type, Settings settings) {
        return switch (type) {
            case BUY -> settings.getBuyColor();
            case SELL -> settings.getSellColor();
            case BUY_SELL -> settings.getBothColor();
        };
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 2) + "..";
    }
}
