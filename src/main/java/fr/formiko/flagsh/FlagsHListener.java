package fr.formiko.flagsh;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FlagsHListener implements Listener {

    /**
     * React to player placing a wall banner.
     * If the player is sneaking, create a flag else a banner.
     * 
     * @param event BlockPlaceEvent triggered when a player places a block
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlace(@NotNull BlockPlaceEvent event) {
        if (isPlayerForbidenToInteract(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        if (FlagsH.ALL_WALL_BANNERS.contains(event.getBlock().getType())) {
            Flag flag = FlagsH.getFlagAt(event.getBlock().getLocation());
            if (flag == null) {
                boolean flagNotBanner = event.getPlayer().isSneaking();
                if ((flagNotBanner && !FlagsH.getPlugin().getConfig().getBoolean("flagEnable"))
                        || (!flagNotBanner && !FlagsH.getPlugin().getConfig().getBoolean("bannerEnable"))) {
                    return;
                }
                FlagsH.createFlag(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst(), event.getItemInHand());
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
    @EventHandler(ignoreCancelled = true)
    public void onInteractWithFlagEntity(@NotNull PlayerInteractEntityEvent event) {
        if (isPlayerForbidenToInteract(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
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
    @EventHandler(ignoreCancelled = true)
    public void onHitFlagEntity(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p && isPlayerForbidenToInteract(p)) {
            event.setCancelled(true);
            return;
        }
        Flag flag = FlagsH.getFlagLinkedToEntity(event.getEntity());
        if (flag != null) {
            event.setCancelled(true);
            flag.remove();
        }
    }

    private boolean isPlayerForbidenToInteract(Player player) {
        return FlagsH.getPlugin().getConfig().getList("forbidenInteractGamemodes", List.of()).contains(player.getGameMode().toString());
    }
}