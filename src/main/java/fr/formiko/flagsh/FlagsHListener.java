package fr.formiko.flagsh;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class FlagsHListener implements Listener {

    /**
     * React to player placing a wall banner in sneak by creating a flag.
     * 
     * @param event BlockPlaceEvent triggered when a player places a block
     */
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        // TODO replace "&& event.getPlayer().isSneaking()" to make banner display.
        // if player is placing a wall banner while sneaking
        if (FlagsH.ALL_WALL_BANNERS.contains(event.getBlock().getType()) && event.getPlayer().isSneaking()) {
            if (event.getBlockPlaced().hasMetadata("flag")) {
                FlagsH.extendsFlag(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst(), true);
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


    @EventHandler
    public void onInteractWithFlagEntity(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        // if player click with a banner on hand on a flag : extend the flag
        if (event.getRightClicked().hasMetadata("flagCoord") && FlagsH.ALL_BANNERS.contains(item.getType())) {
            event.getPlayer().sendMessage("Interacting with flag via entity");
            FlagsH.interactWithFlag(event.getPlayer(), event.getRightClicked());
        }
    }

    @EventHandler
    public void onHitFlagEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) {
            p.sendMessage("Player hit flag entity ? " + event.getEntity().hasMetadata("flagCoord"));
        }
        if (event.getDamager() instanceof Player && event.getEntity().hasMetadata("flagCoord")) {
            event.setCancelled(true);
            FlagsH.removeFlagIfNeeded(event.getEntity());
        }
    }
}