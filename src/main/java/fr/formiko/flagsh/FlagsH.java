package fr.formiko.flagsh;

import java.util.List;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FlagsH {
    public static FlagsHPlugin plugin;
    public static final List<Material> ALL_BANNERS = List.of(Material.WHITE_BANNER, Material.ORANGE_BANNER, Material.MAGENTA_BANNER,
            Material.LIGHT_BLUE_BANNER, Material.YELLOW_BANNER, Material.LIME_BANNER, Material.PINK_BANNER, Material.GRAY_BANNER,
            Material.LIGHT_GRAY_BANNER, Material.CYAN_BANNER, Material.PURPLE_BANNER, Material.BLUE_BANNER, Material.BROWN_BANNER,
            Material.GREEN_BANNER, Material.RED_BANNER, Material.BLACK_BANNER);
    public static final List<Material> ALL_WALL_BANNERS = List.of(Material.WHITE_WALL_BANNER, Material.ORANGE_WALL_BANNER,
            Material.MAGENTA_WALL_BANNER, Material.LIGHT_BLUE_WALL_BANNER, Material.YELLOW_WALL_BANNER, Material.LIME_WALL_BANNER,
            Material.PINK_WALL_BANNER, Material.GRAY_WALL_BANNER, Material.LIGHT_GRAY_WALL_BANNER, Material.CYAN_WALL_BANNER,
            Material.PURPLE_WALL_BANNER, Material.BLUE_WALL_BANNER, Material.BROWN_WALL_BANNER, Material.GREEN_WALL_BANNER,
            Material.RED_WALL_BANNER, Material.BLACK_WALL_BANNER);
    public static final List<Material> ALL_FENCES = List.of(Material.ACACIA_FENCE, Material.BAMBOO_FENCE, Material.BIRCH_FENCE,
            Material.CHERRY_FENCE, Material.CRIMSON_FENCE, Material.DARK_OAK_FENCE, Material.JUNGLE_FENCE, Material.OAK_FENCE,
            Material.SPRUCE_FENCE, Material.MANGROVE_FENCE_GATE, Material.NETHER_BRICK_FENCE, Material.WARPED_FENCE);
    public static final List<Material> ALL_GLACE_PANES = List.of(Material.GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE, Material.BROWN_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE,
            Material.GRAY_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.LIGHT_GRAY_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE, Material.PINK_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE,
            Material.RED_STAINED_GLASS_PANE, Material.WHITE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.IRON_BARS,
            Material.CHAIN, Material.BAMBOO, Material.LIGHTNING_ROD);
    public static final List<Material> ALL_WALLS = List.of(Material.BLACKSTONE_WALL, Material.BRICK_WALL, Material.COBBLESTONE_WALL,
            Material.ANDESITE_WALL, Material.DEEPSLATE_BRICK_WALL, Material.DEEPSLATE_TILE_WALL, Material.DIORITE_WALL,
            Material.GRANITE_WALL, Material.NETHER_BRICK_WALL, Material.STONE_BRICK_WALL, Material.POLISHED_BLACKSTONE_BRICK_WALL,
            Material.POLISHED_BLACKSTONE_WALL, Material.POLISHED_DEEPSLATE_WALL, Material.PRISMARINE_WALL, Material.RED_NETHER_BRICK_WALL,
            Material.RED_SANDSTONE_WALL, Material.SANDSTONE_WALL, Material.MOSSY_COBBLESTONE_WALL, Material.MOSSY_STONE_BRICK_WALL,
            Material.END_STONE_BRICK_WALL, Material.COBBLED_DEEPSLATE_WALL, Material.MUD_BRICK_WALL);

    private FlagsH() {}


    // create ---------------------------------------------------------------------------------------------------------
    public static void createFlag(Player p, Block banner, Block behind, ItemStack itemStack, float size) {
        Flag f = new Flag(banner, p.isSneaking(), behind);
        f.create(itemStack);
        plugin.getFlags().add(f);

    }

    /** Return the offset to hit the wall depending on the block behind. */
    public static float getOffsetToHitWall(Material behind) {
        if (FlagsH.ALL_WALLS.contains(behind))
            return 0.42f;
        else if (FlagsH.ALL_FENCES.contains(behind))
            return 0.55f;
        else if (FlagsH.ALL_GLACE_PANES.contains(behind))
            return 0.6f;
        else
            return 0.18f;
    }


    // extends --------------------------------------------------------------------------------------------------------
    /**
     * Extends the flag if possible.
     *
     * @param p            the player who want to extend the flag
     * @param banner       the block of the banner
     * @param behind       the block behind the banner
     * @param bannerPlaced true if the banner have been placed on the block banner, false if it's in the player hand.
     */
    public static void extendsFlag(Flag flag, Block bannerPlaced, Player playerToRemoveItemFrom) {
        if (flag.getSize() >= plugin.getConfig().getDouble("maxFlagSize")) {
            if (bannerPlaced != null) {
                bannerPlaced.breakNaturally();
            }
            flag.playSound(Sound.ENTITY_VILLAGER_NO);
        } else {
            flag.extend((float) Math.min(flag.getSize() + plugin.getConfig().getDouble("increasingSizeStep"),
                    plugin.getConfig().getDouble("maxFlagSize")));
            if (playerToRemoveItemFrom != null && playerToRemoveItemFrom.getGameMode() != GameMode.CREATIVE) {
                playerToRemoveItemFrom.getInventory().getItemInMainHand()
                        .setAmount(playerToRemoveItemFrom.getInventory().getItemInMainHand().getAmount() - 1);
            }
        }
    }


    // Usefull methods ------------------------------------------------------------------------------------------------

    public static Flag getFlagAt(int x, int y, int z, World world) {
        for (Flag flag : plugin.getFlags()) {
            if (flag.getX() == x && flag.getY() == y && flag.getZ() == z && flag.getWorldId().equals(world.getUID())) {
                return flag;
            }
        }
        return null;
    }
    public static Flag getFlagAt(Location loc) { return getFlagAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld()); }


    public static Flag getFlagLinkedToEntity(UUID uuid) {
        for (Flag flag : plugin.getFlags()) {
            if (flag.getInteractionsIds().contains(uuid)) {
                return flag;
            }
        }
        return null;
    }
    public static Flag getFlagLinkedToEntity(Entity entity) { return getFlagLinkedToEntity(entity.getUniqueId()); }

}
