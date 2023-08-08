package fr.formiko.flagsh;

import java.util.List;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Matrix4f;

public class FlagsH {
    public static JavaPlugin plugin;
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

    // remove ---------------------------------------------------------------------------------------------------------
    public static void removeFlagIfNeeded(Block block, boolean removeForReal) {
        // plugin.getLogger().info("You broke a wall banner in " + block.getLocation() + " " + block.getType().toString());
        // get metadata from the broken block
        if (block.hasMetadata("flag")) {
            ItemStack item = null;
            if (removeForReal) {
                playSound(block, Sound.BLOCK_WOOD_BREAK);
                item = bannerBlockToItemStack(block);
            }
            // remove each DisplayItem entity that represents the flag
            String[] t = block.getMetadata("flag").get(0).asString().split(",");
            for (String s : t) {
                // plugin.getLogger().info("removing entity " + s);
                block.getWorld().getEntity(UUID.fromString(s)).remove();
            }
            block.getWorld().getEntity(UUID.fromString(block.getMetadata("flagInteraction").get(0).asString())).remove();

            // drop item & forget metadata
            if (removeForReal) {
                item.setAmount((int) (1
                        + (block.getMetadata("flagSize").get(0).asFloat() - 1) / plugin.getConfig().getDouble("increasingSizeStep")));
                block.getWorld().dropItem(block.getLocation(), item);
                block.removeMetadata("flag", plugin);
                block.removeMetadata("flagSize", plugin);
                block.removeMetadata("flagInteraction", plugin);
            }
        }
    }
    public static void removeFlagIfNeeded(Block block) { removeFlagIfNeeded(block, true); }
    public static void removeFlagIfNeeded(Entity entity) {
        removeFlagIfNeeded(coordToBlock(entity.getMetadata("flagCoord").get(0).asString(), entity.getWorld()));
    }

    // create ---------------------------------------------------------------------------------------------------------
    public static void createFlag(Player p, Block banner, Block behind, ItemStack itemStack, float size) {
        if (!p.isSneaking()) {
            return;
        }
        // TODO if p.isSneaking() ... else place a banner


        float offsetToHitTheWall = getOffsetToHitWall(behind) - (0.335f * (size - 1f));
        float offsetToMergeTextureTogether = 0.02f * size;
        boolean offsetToHitTheWallInX = false;
        float yaw = 0;
        float offsetX = 0;
        float offsetZ = 0;
        float offsetXOrZInteraction = -0.5f;
        if (behind.getX() > banner.getX()) {
            yaw = 0;
            offsetZ = offsetToMergeTextureTogether;
            offsetToHitTheWallInX = true;
        } else if (behind.getX() < banner.getX()) {
            yaw = 180;
            offsetZ = -offsetToMergeTextureTogether;
            offsetToHitTheWallInX = true;
            offsetToHitTheWall = -offsetToHitTheWall;
            offsetXOrZInteraction = -offsetXOrZInteraction;
        } else if (behind.getZ() > banner.getZ()) {
            yaw = 90;
            offsetX = -offsetToMergeTextureTogether;
        } else if (behind.getZ() < banner.getZ()) {
            yaw = -90;
            offsetX = offsetToMergeTextureTogether;
            offsetToHitTheWall = -offsetToHitTheWall;
            offsetXOrZInteraction = -offsetXOrZInteraction;
        }

        Location interactionLoc = new Location(banner.getWorld(),
                banner.getX() + offsetX + 0.5f + (offsetToHitTheWallInX ? offsetXOrZInteraction : 0), banner.getY() + 0.5f - size / 2,
                banner.getZ() + offsetZ + 0.5f + (!offsetToHitTheWallInX ? offsetXOrZInteraction : 0));

        // Add to 1st banner
        if (offsetToHitTheWallInX) {
            offsetX += offsetToHitTheWall;
        } else {
            offsetZ += offsetToHitTheWall;
        }
        ItemDisplay id1 = createBannerDisplay(banner, itemStack,
                new Location(banner.getWorld(), banner.getX() + offsetX + 0.5f, banner.getY() + 0.5f, banner.getZ() + offsetZ + 0.5f), yaw,
                true, size);


        // Remove to 2nd banner
        if (offsetToHitTheWallInX) {
            offsetX -= 2 * offsetToHitTheWall;
        } else {
            offsetZ -= 2 * offsetToHitTheWall;
        }
        ItemDisplay id2 = createBannerDisplay(banner, itemStack,
                new Location(banner.getWorld(), banner.getX() - offsetX + 0.5f, banner.getY() + 0.5f, banner.getZ() - offsetZ + 0.5f), yaw,
                false, size);


        Interaction interaction = createInteraction(interactionLoc, 0.2f * size * 10, 0.95f * size, banner, behind); // TODO remove "* 10"


        // remove the placed banner without droping the item
        // banner.setType(Material.AIR);
        banner.setMetadata("flag", new FixedMetadataValue(plugin, id1.getUniqueId() + "," + id2.getUniqueId()));
        banner.setMetadata("flagInteraction", new FixedMetadataValue(plugin, interaction.getUniqueId()));
        banner.setMetadata("flagSize", new FixedMetadataValue(plugin, size));

        // behind.setMetadata("flagOnIt", new FixedMetadataValue(plugin,
        // banner.getLocation().getX() + "," + banner.getLocation().getY() + "," + banner.getLocation().getZ()));

    }

    /** Return the offset to hit the wall depending on the block behind. */
    private static float getOffsetToHitWall(Block behind) {
        if (FlagsH.ALL_WALLS.contains(behind.getType()))
            return 0.42f;
        else if (FlagsH.ALL_FENCES.contains(behind.getType()))
            return 0.55f;
        else if (FlagsH.ALL_GLACE_PANES.contains(behind.getType()))
            return 0.6f;
        else
            return 0.18f;
    }


    /** Create 2 item display, rotate them and place them where the banner is. */
    private static ItemDisplay createBannerDisplay(Block banner, ItemStack itemStack, Location location, float yaw, boolean isFirst,
            float size) {
        // BlockDisplay don't work with banners
        ItemDisplay itemDisplay = banner.getWorld().spawn(location, ItemDisplay.class);
        itemDisplay.setItemStack(itemStack);
        if (isFirst) {
            // @formatter:off
            itemDisplay.setTransformationMatrix(new Matrix4f(
                    0, -1, 0, 0,
                    -1, 0, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1/size));
            // @formatter:on
        } else {
            // @formatter:off
            itemDisplay.setTransformationMatrix(new Matrix4f(
                    0, 1, 0, 0,
                    1, 0, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, -1/size));
            // @formatter:on
        }

        itemDisplay.setRotation(yaw, 0);
        return itemDisplay;
    }

    private static Interaction createInteraction(Location location, float width, float height, Block banner, Block behind) {
        Interaction interaction = location.getWorld().spawn(location, Interaction.class);
        interaction.setInteractionWidth(width);
        interaction.setInteractionHeight(height);
        interaction.setMetadata("flagCoord", new FixedMetadataValue(plugin, blockToCoord(banner)));
        interaction.setMetadata("behindCoord", new FixedMetadataValue(plugin, blockToCoord(behind)));
        interaction.setResponsive(true);
        interaction.setPersistent(true);
        interaction.setRotation(0f, 90f);

        return interaction;
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
    public static void extendsFlag(Player p, Block banner, Block behind, boolean bannerPlaced) {
        if (banner.getMetadata("flagSize").get(0).asFloat() == plugin.getConfig().getDouble("maxFlagSize")) {
            playSound(banner, Sound.ENTITY_VILLAGER_NO);
            if (bannerPlaced) {
                banner.breakNaturally();
            }
            p.sendMessage("Flag already at max size (" + plugin.getConfig().getDouble("maxFlagSize") + ")");
        } else {
            playSound(banner, Sound.BLOCK_WOOL_PLACE);
            ItemStack itemStack = bannerBlockToItemStack(banner);
            removeFlagIfNeeded(banner, false);
            createFlag(p, banner, behind, itemStack,
                    (float) Math.min(banner.getMetadata("flagSize").get(0).asFloat() + plugin.getConfig().getDouble("increasingSizeStep"),
                            plugin.getConfig().getDouble("maxFlagSize")));
            if (!bannerPlaced && p.getGameMode() != GameMode.CREATIVE) {
                p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            }
            p.sendMessage("Flag extended from " + plugin.getConfig().getDouble("increasingSizeStep"));
        }
    }


    public static void interactWithFlag(Player p, Entity entity) {
        extendsFlag(p, coordToBlock(entity.getMetadata("flagCoord").get(0).asString(), entity.getWorld()),
                coordToBlock(entity.getMetadata("behindCoord").get(0).asString(), entity.getWorld()), false);
    }


    // Usefull methods ------------------------------------------------------------------------------------------------
    private static String blockToCoord(Block block) {
        return ((int) block.getLocation().getX()) + "," + ((int) block.getLocation().getY()) + "," + ((int) block.getLocation().getZ());
    }
    private static Block coordToBlock(String coord, World world) {
        String[] split = coord.split(",");
        return world.getBlockAt(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    private static ItemStack bannerBlockToItemStack(Block block) {
        if (block.hasMetadata("flag")) {
            for (String s : block.getMetadata("flag").get(0).asString().split(",")) {
                if (block.getWorld().getEntity(UUID.fromString(s)) instanceof ItemDisplay itemDisplay) {
                    return itemDisplay.getItemStack();
                }
            }
        }
        return new ItemStack(Material.WHITE_BANNER);
    }

    private static void playSound(Block banner, Sound sound) {
        banner.getWorld().playSound(banner.getLocation(), sound, SoundCategory.BLOCKS, 1, 0);
    }
}
