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
        if (FlagsH.ALL_WALL_BANNERS.contains(event.getBlock().getType())) {
            Flag flag = FlagsH.getFlagAt(event.getBlock().getLocation());
            if (flag == null) {
                boolean flagNotBanner = event.getPlayer().isSneaking();
                if ((flagNotBanner && !FlagsH.plugin.getConfig().getBoolean("flagEnable"))
                        || (!flagNotBanner && !FlagsH.plugin.getConfig().getBoolean("bannerEnable"))) {
                    return;
                }
                FlagsH.createFlag(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst(), event.getItemInHand(), 1f);
            } else {
                FlagsH.extendsFlag(flag, event.getBlockPlaced(), null);
            }
            event.getBlockPlaced().setType(Material.AIR);
        }
    }

    /**
     * React to player right clicking on a flag entity with a banner in hand.
     * 
     * @param event
     */
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

    /**
     * React to player hitting a flag entity.
     * 
     * @param event
     */
    @EventHandler
    public void onHitFlagEntity(EntityDamageByEntityEvent event) {
        Flag flag = FlagsH.getFlagLinkedToEntity(event.getEntity());
        if (flag != null) {
            event.setCancelled(true);
            flag.remove();
        }
    }
}