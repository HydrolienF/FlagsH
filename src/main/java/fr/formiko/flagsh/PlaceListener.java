package fr.formiko.flagsh;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceListener implements Listener {

    /**
     * React to player placing a wall banner in sneak by creating a flag.
     * 
     * @param event BlockPlaceEvent triggered when a player places a block
     */
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        // if player is placing a wall banner while sneaking
        if (FlagsH.ALL_WALL_BANNERS.contains(event.getBlock().getType()) && event.getPlayer().isSneaking()) {
            if (event.getBlockPlaced().hasMetadata("flag")) {
                FlagsH.extendsFlag(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst(), event.getItemInHand());
            } else {
                event.getPlayer().sendMessage("Creating flag");
                FlagsH.createFlag(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst(), event.getItemInHand(), 1f);
            }
            event.getBlockPlaced().setType(Material.AIR);
        } else {
            // break if there is a flag in the block placed
            FlagsH.removeFlagIfNeeded(event.getBlock());
        }
    }
}
