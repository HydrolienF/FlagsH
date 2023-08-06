package fr.formiko.flagsh;

import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Matrix4f;

public class FlagsH {
    public static JavaPlugin plugin;
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


    // remove ---------------------------------------------------------------------------------------------------------
    public static void removeFlagIfNeeded(Block block, boolean removeSize) {
        if (!FlagsH.ALL_WALL_BANNERS.contains(block.getType())) {
            return;
        }
        plugin.getLogger().info("You broke a wall banner in " + block.getLocation() + " " + block.getType().toString());
        // get metadata from the broken block
        if (block.hasMetadata("flag")) {
            plugin.getLogger().info("flag metadata: " + block.getMetadata("flag").get(0).asString());
            String[] t = block.getMetadata("flag").get(0).asString().split(",");
            for (String s : t) {
                plugin.getLogger().info("removing entity " + s);
                UUID id = UUID.fromString(s);
                block.getWorld().getEntity(id).remove();
            }
            block.removeMetadata("flag", plugin);
            if (removeSize) {
                block.removeMetadata("flagSize", plugin);
            }
        }
    }
    public static void removeFlagIfNeeded(Block block) { removeFlagIfNeeded(block, true); }

    // create ---------------------------------------------------------------------------------------------------------
    public static void createFlag(Player p, Block banner, Block behind, ItemStack itemStack, float size) {
        BlockData data = banner.getBlockData();
        p.sendMessage("You placed a wall banner in sneak in " + banner.getLocation() + " " + banner.getType().toString() + " on "
                + behind.getType().toString() + " at " + behind.getLocation());
        p.sendMessage("blocdata: " + data.getAsString());


        float offsetToHitTheWall = getOffsetToHitWall(behind) - (0.335f * (size - 1f));
        float offsetToMergeTextureTogether = 0.02f * size;
        boolean offsetToHitTheWallInX = false;
        float yaw = 0;
        float offsetX = 0;
        float offsetZ = 0;
        if (behind.getX() > banner.getX()) {
            // p.sendMessage("behind.getX() > banner.getX()");
            yaw = 0;
            offsetZ = offsetToMergeTextureTogether;
            offsetToHitTheWallInX = true;
        } else if (behind.getX() < banner.getX()) {
            // p.sendMessage("behind.getX() < banner.getX()");
            yaw = 180;
            offsetZ = -offsetToMergeTextureTogether;
            offsetToHitTheWallInX = true;
            offsetToHitTheWall = -offsetToHitTheWall;
        } else if (behind.getZ() > banner.getZ()) {
            // p.sendMessage("behind.getZ() > banner.getZ()");
            yaw = 90;
            offsetX = -offsetToMergeTextureTogether;
        } else if (behind.getZ() < banner.getZ()) {
            // p.sendMessage("behind.getZ() < banner.getZ()");
            yaw = -90;
            offsetX = offsetToMergeTextureTogether;
            offsetToHitTheWall = -offsetToHitTheWall;
        }

        // p.sendMessage("yaw: " + yaw + "");

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


        // remove the placed banner without droping the item
        // banner.setType(Material.AIR);
        banner.setMetadata("flag", new FixedMetadataValue(plugin, id1.getUniqueId() + "," + id2.getUniqueId()));
        banner.setMetadata("flagSize", new FixedMetadataValue(plugin, size));
        // TODO change the banner texture to an invisible one so that player can still break it or extends the flag but won't see the
        // banner texture

        if (banner.getBlockData() instanceof Directional oldDirectional) {
            p.sendMessage("directional: " + oldDirectional.getFacing().toString());
            BlockFace blockFace = oldDirectional.getFacing();
            banner.setType(Material.BLACK_WALL_BANNER);
            Directional newDirectional = (Directional) banner.getBlockData();
            newDirectional.setFacing(blockFace);
            p.sendMessage("new directional: " + newDirectional.getFacing().toString());
            banner.setBlockData(newDirectional);
        }

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

    // extends --------------------------------------------------------------------------------------------------------
    public static void extendsFlag(Player p, Block banner, Block behind, ItemStack itemStack) {
        removeFlagIfNeeded(banner, false);
        createFlag(p, banner, behind, itemStack,
                (float) Math.min(banner.getMetadata("flagSize").get(0).asFloat() + plugin.getConfig().getDouble("increasingSizeStep"),
                        plugin.getConfig().getDouble("maxFlagSize")));
    }

}
