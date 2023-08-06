package fr.formiko.flagsh;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) { FlagsH.removeFlagIfNeeded(event.getBlock()); }

    // @EventHandler
    // public void onBlockExplode(BlockExplodeEvent event) { removeFlagIfNeeded(event.getBlock()); }

    // @EventHandler
    // public void onBlockBurn(BlockBurnEvent event) { removeFlagIfNeeded(event.getBlock()); }

    // @EventHandler
    // public void onBlockDropItemEvent(BlockDropItemEvent event) { removeFlagIfNeeded(event.getBlock()); }

    // @EventHandler
    // public void onDamage(BlockDamageEvent event) { removeFlagIfNeeded(event.getBlock()); }


}
