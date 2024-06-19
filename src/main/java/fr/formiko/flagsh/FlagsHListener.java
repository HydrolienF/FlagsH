package fr.formiko.flagsh;

import java.lang.reflect.Method;
import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.Location;
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
     * React to player placing a wall banner.
     * If the player is sneaking, create a flag else a banner.
     * 
     * @param event BlockPlaceEvent triggered when a player places a block
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlace(@Nonnull BlockPlaceEvent event) {
        if (isPlayerForbidenToInteract(event.getPlayer(), event.getBlockPlaced().getLocation())) {
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
                FlagsH.extendsFlag(flag, event.getBlockPlaced(), event.getPlayer());
            }
            event.setCancelled(true);
        }
    }

    /**
     * React to player right clicking on a flag entity with a banner in hand.
     * 
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onInteractWithFlagEntity(@Nonnull PlayerInteractEntityEvent event) {
        if (isPlayerForbidenToInteract(event.getPlayer(), event.getRightClicked().getLocation())) {
            return;
        }
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        // if player click with a banner on hand on a flag : extend the flag
        if (FlagsH.ALL_BANNERS.contains(item.getType())) {
            Flag flag = FlagsH.getFlagLinkedToEntity(event.getRightClicked());
            if (flag != null) {
                FlagsH.extendsFlag(flag, null, event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

    /**
     * React to player hitting a flag entity.
     * 
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onHitFlagEntity(@Nonnull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p && isPlayerForbidenToInteract(p, event.getEntity().getLocation())) {
            return;
        }
        Flag flag = FlagsH.getFlagLinkedToEntity(event.getEntity());
        if (flag != null) {
            flag.remove();
            event.setCancelled(true);
        }
    }

    private Method getCachePermission;
    private Object[] effectiveParameters;
    private boolean testTowny = true;
    private boolean isPlayerForbidenToInteract(Player player, Location locationToInteract) {
        if(testTowny){
            try {
                if (getCachePermission == null) {
                    Class<?> playerCacheUtil = Class.forName("com.palmergames.bukkit.towny.utils.PlayerCacheUtil");
                    Class<?> actionType = Class.forName("com.palmergames.bukkit.towny.object.TownyPermission$ActionType");
                    Class<?>[] formalParameters = {Player.class, Location.class, Material.class, actionType};
                    Object build = actionType.getEnumConstants()[0];

                    effectiveParameters = new Object[] {player, locationToInteract, Material.RED_BANNER, build};
                    getCachePermission = playerCacheUtil.getMethod("getCachePermission", formalParameters);
                } else {
                    effectiveParameters[0] = player;
                    effectiveParameters[1] = locationToInteract;
                }
                Object o = getCachePermission.invoke(null, effectiveParameters);
                if (o instanceof Boolean canBuild && !canBuild) {
                    // Player can't interact with flag because of Towny permission
                    return true;
                }
            } catch (Exception e) {
                // Towny not found
                FlagsH.getPlugin().getLogger().info("Towny not found, skipping Towny permission check for flag interaction");
                testTowny = false;
            }
        }
        return FlagsH.getPlugin().getConfig().getList("forbidenInteractGamemodes", List.of()).contains(player.getGameMode().toString());
    }
}