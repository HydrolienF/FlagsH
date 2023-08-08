package fr.formiko.flagsh;

import org.bukkit.Material;
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
        // TODO replace "&& event.getPlayer().isSneaking()" to make bigger banner display.
        // if player is placing a wall banner while sneaking
        if (FlagsH.ALL_WALL_BANNERS.contains(event.getBlock().getType()) && event.getPlayer().isSneaking()) {
            Flag flag = FlagsH.getFlagAt(event.getBlock().getLocation());
            if (flag == null) {
                event.getPlayer().sendMessage("Creating flag");
                FlagsH.createFlag(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst(), event.getItemInHand(), 1f);
            } else {
                FlagsH.extendsFlag(flag, event.getBlockPlaced(), null);
            }
            event.getBlockPlaced().setType(Material.AIR);
        }
    }


    @EventHandler
    public void onInteractWithFlagEntity(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        // if player click with a banner on hand on a flag : extend the flag
        if (FlagsH.ALL_BANNERS.contains(item.getType())) {

            Flag flag = FlagsH.getFlagLinkedToEntity(event.getRightClicked());
            if (flag != null) {
                FlagsH.extendsFlag(flag, null, event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onHitFlagEntity(EntityDamageByEntityEvent event) {
        Flag flag = FlagsH.getFlagLinkedToEntity(event.getEntity());
        if (flag != null) {
            event.setCancelled(true);
            flag.remove();
        }
    }
}