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
import org.bukkit.inventory.EquipmentSlot;

/**
 * Listener for FlagsH plugin.
 */
public class FlagsHListener implements Listener {

    /**
     * React to player placing a wall banner.
     * If the player is sneaking, create a flag else a banner.
     * 
     * @param event BlockPlaceEvent triggered when a player places a block
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlace(@Nonnull BlockPlaceEvent event) {
        // if a banner is placed
        if (FlagsH.ALL_WALL_BANNERS.contains(event.getBlock().getType())) {
            // if player is not allowed to interact with flag at this location, cancel the event
            if (isPlayerForbiddenToInteract(event.getPlayer(), event.getBlockPlaced().getLocation())) {
                FlagsHPlugin.getInstance().debug(() -> "Player " + event.getPlayer().getName() + " is not allowed to interact with flag at "
                        + event.getBlockPlaced().getLocation());
                event.setCancelled(true);
                return;
            }
            Flag flag = FlagsH.getFlagAt(event.getBlock().getLocation());
            if (flag == null) {
                boolean customFlagPlaced = createNewFlag(event);
                event.setCancelled(customFlagPlaced); // Do not cancel if we need vanilla flag to be placed.
                FlagsHPlugin.getInstance().debug(() -> "Flag placed by " + event.getPlayer().getName() + " at "
                        + event.getBlockPlaced().getLocation() + " ? " + (customFlagPlaced ? "yes" : "no"));
            } else {
                FlagsH.extendsFlag(flag, event.getBlockPlaced(), event.getPlayer());
                event.setCancelled(true);
                FlagsHPlugin.getInstance()
                        .debug("Flag extended by " + event.getPlayer().getName() + " at " + event.getBlockPlaced().getLocation());
            }
        }
    }

    /**
     * Create a new flag or banner depending on the player sneaking status and the plugin configuration.
     * 
     * @param event BlockPlaceEvent from {@code onPlace}
     * @return true if a flag have been created, false if a vanilla banner should be placed.
     */
    private boolean createNewFlag(@Nonnull BlockPlaceEvent event) {
        boolean flagNotBanner = event.getPlayer().isSneaking();

        // Special case for off hand
        if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            if (FlagsH.getPlugin().getConfig().get("offHandMod").equals("VANILLA")) {
                FlagsHPlugin.getInstance().debug("Off hand mod is VANILLA, canceling flag creation");
                return false;
            } else if (FlagsH.getPlugin().getConfig().get("offHandMod").equals("INVERTED")) {
                flagNotBanner = !flagNotBanner;
            }
        }

        // Disabled flag or banner
        if ((flagNotBanner && !FlagsH.getPlugin().getConfig().getBoolean("flagEnable"))
                || (!flagNotBanner && !FlagsH.getPlugin().getConfig().getBoolean("bannerEnable"))) {
            FlagsHPlugin.getInstance().debug("Flag or banner creation is disabled, canceling flag creation");
            return false;
        }

        FlagsH.createFlag(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst(), event.getItemInHand(), flagNotBanner);
        return true;
    }

    /**
     * React to player right clicking on a flag entity with a banner in hand.
     * 
     * @param event PlayerInteractEntityEvent triggered when a player interacts with an entity
     */
    @EventHandler(ignoreCancelled = true)
    public void onInteractWithFlagEntity(@Nonnull PlayerInteractEntityEvent event) {
        // if player click with a banner on hand on a flag : extend the flag
        if (FlagsH.ALL_BANNERS.contains(event.getPlayer().getInventory().getItemInMainHand().getType())
                || FlagsH.ALL_BANNERS.contains(event.getPlayer().getInventory().getItemInOffHand().getType())) {

            Flag flag = FlagsH.getFlagLinkedToEntity(event.getRightClicked());
            if (flag != null) {
                // Extend the flag if the player is allowed to interact with it
                if (!isPlayerForbiddenToInteract(event.getPlayer(), event.getRightClicked().getLocation())) {
                    FlagsH.extendsFlag(flag, null, event.getPlayer());
                    FlagsHPlugin.getInstance().debug(
                            () -> "Flag extended by " + event.getPlayer().getName() + " at " + event.getRightClicked().getLocation());
                } else {
                    FlagsHPlugin.getInstance().debug(() -> "Player " + event.getPlayer().getName() + " is not allowed to extend flag at "
                            + event.getRightClicked().getLocation());
                }
                // Cancel the event no matter what
                event.setCancelled(true);
            }
        }
    }

    /**
     * React to player hitting a flag entity.
     */
    @EventHandler(ignoreCancelled = true)
    public void onHitFlagEntity(@Nonnull EntityDamageByEntityEvent event) {
        Flag flag = FlagsH.getFlagLinkedToEntity(event.getEntity());
        if (flag != null) {
            if (event.getDamager() instanceof Player p && !isPlayerForbiddenToInteract(p, event.getEntity().getLocation())) {
                flag.remove();
                FlagsHPlugin.getInstance().debug(() -> "Flag removed by " + p.getName() + " at " + event.getEntity().getLocation());
            } else {
                FlagsHPlugin.getInstance().debug(() -> "Player " + event.getDamager().getName() + " is not allowed to hit flag at "
                        + event.getEntity().getLocation());
            }
            event.setCancelled(true);
        }
    }

    private Method getCachePermission;
    private Object[] effectiveParameters;
    private boolean testTowny = true;
    private boolean isPlayerForbiddenToInteract(Player player, Location locationToInteract) {
        if (testTowny) {
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