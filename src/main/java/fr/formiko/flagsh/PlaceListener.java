package fr.formiko.flagsh;

import org.bukkit.Material;
import org.bukkit.block.Block;
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
        // Place only if player is placing a wall banner while sneaking
        if (!FlagsH.ALL_WALL_BANNERS.contains(event.getBlock().getType()) || event.isCancelled() || !event.getPlayer().isSneaking()) {
            return;
        }
        if (FlagsH.ALL_WALL_BANNERS.contains(event.getBlockAgainst().getType())) {
            event.getPlayer().sendMessage("FlagsH: extending flag");
            int difX = event.getBlock().getX() - event.getBlockAgainst().getX();
            int difY = event.getBlock().getY() - event.getBlockAgainst().getY();
            int difZ = event.getBlock().getZ() - event.getBlockAgainst().getZ();
            Block behind = event.getBlockAgainst().getRelative(-difX, -difY, -difZ);
            FlagsH.extendsFlag(event.getPlayer(), event.getBlockAgainst(), behind, event.getItemInHand(), event.getBlockPlaced());
            event.getBlockPlaced().setType(Material.AIR);
        } else {
            event.getPlayer().sendMessage("FlagsH: creating flag");
            FlagsH.createFlag(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst(), event.getItemInHand(), 1f);
        }


    }
}
