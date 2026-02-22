package com.blockforge.chestmarketplus.hook;

import com.blockforge.chestmarketplus.ChestMarketPlus;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;

public class WorldGuardHook {

    private final ChestMarketPlus plugin;
    private StateFlag chestShopFlag;

    public WorldGuardHook(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        String flagName = plugin.getConfigManager().getSettings().getWorldGuardFlagName();
        boolean defaultValue = plugin.getConfigManager().getSettings().isWorldGuardDefaultValue();

        try {
            StateFlag flag = new StateFlag(flagName, defaultValue);
            registry.register(flag);
            chestShopFlag = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get(flagName);
            if (existing instanceof StateFlag sf) {
                chestShopFlag = sf;
            } else {
                plugin.getLogger().warning("WorldGuard flag '" + flagName + "' exists but is not a StateFlag!");
            }
        }
    }

    public boolean canCreateShop(Location location) {
        if (chestShopFlag == null) return true;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

        return set.testState(null, chestShopFlag);
    }

    public StateFlag getChestShopFlag() {
        return chestShopFlag;
    }
}
