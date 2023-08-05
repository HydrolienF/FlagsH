package fr.formiko.flagsh;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.joml.Matrix4f;

public class PlaceListener implements Listener {
    private List<Material> allWallBanners = List.of(Material.WHITE_WALL_BANNER, Material.ORANGE_WALL_BANNER, Material.MAGENTA_WALL_BANNER,
            Material.LIGHT_BLUE_WALL_BANNER, Material.YELLOW_WALL_BANNER, Material.LIME_WALL_BANNER, Material.PINK_WALL_BANNER,
            Material.GRAY_WALL_BANNER, Material.LIGHT_GRAY_WALL_BANNER, Material.CYAN_WALL_BANNER, Material.PURPLE_WALL_BANNER,
            Material.BLUE_WALL_BANNER, Material.BROWN_WALL_BANNER, Material.GREEN_WALL_BANNER, Material.RED_WALL_BANNER,
            Material.BLACK_WALL_BANNER);
    private List<Material> allFences = List.of(Material.ACACIA_FENCE, Material.BAMBOO_FENCE, Material.BIRCH_FENCE, Material.CHERRY_FENCE,
            Material.CRIMSON_FENCE, Material.DARK_OAK_FENCE, Material.JUNGLE_FENCE, Material.OAK_FENCE, Material.SPRUCE_FENCE,
            Material.MANGROVE_FENCE_GATE, Material.NETHER_BRICK_FENCE, Material.WARPED_FENCE);
    private List<Material> allGlacePane = List.of(Material.GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE,
            Material.BROWN_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            Material.LIME_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
            Material.PINK_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE,
            Material.WHITE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.IRON_BARS, Material.CHAIN, Material.BAMBOO,
            Material.LIGHTNING_ROD);
    private List<Material> allWalls = List.of(Material.BLACKSTONE_WALL, Material.BRICK_WALL, Material.COBBLESTONE_WALL,
            Material.ANDESITE_WALL, Material.DEEPSLATE_BRICK_WALL, Material.DEEPSLATE_TILE_WALL, Material.DIORITE_WALL,
            Material.GRANITE_WALL, Material.NETHER_BRICK_WALL, Material.STONE_BRICK_WALL, Material.POLISHED_BLACKSTONE_BRICK_WALL,
            Material.POLISHED_BLACKSTONE_WALL, Material.POLISHED_DEEPSLATE_WALL, Material.PRISMARINE_WALL, Material.RED_NETHER_BRICK_WALL,
            Material.RED_SANDSTONE_WALL, Material.SANDSTONE_WALL, Material.MOSSY_COBBLESTONE_WALL, Material.MOSSY_STONE_BRICK_WALL,
            Material.END_STONE_BRICK_WALL, Material.COBBLED_DEEPSLATE_WALL, Material.MUD_BRICK_WALL);


    /**
     * React to player placing a wall banner in sneak by creating a flag.
     * 
     * @param event BlockPlaceEvent triggered when a player places a block
     */
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        // Place only if player is placing a wall banner while sneaking
        if (!allWallBanners.contains(event.getBlock().getType()) || event.isCancelled() || !event.getPlayer().isSneaking())
            return;

        Player p = event.getPlayer();
        Block banner = event.getBlockPlaced();
        BlockData data = banner.getBlockData();
        Block behind = event.getBlockAgainst();
        ItemStack itemStack = event.getItemInHand();

        p.sendMessage("You placed a wall banner in sneak in " + banner.getLocation() + " " + banner.getType().toString() + " "
                + behind.getType().toString());
        p.sendMessage("blocdata: " + data.getAsString());


        float offsetToHitTheWall = getOffsetToHitWall(behind);
        float offsetToMergeTextureTogether = 0.02f;
        boolean offsetToHitTheWallInX = false;
        float yaw = 0;
        float offsetX = 0;
        float offsetZ = 0;
        if (behind.getX() > banner.getX()) {
            p.sendMessage("behind.getX() > banner.getX()");
            yaw = 0;
            offsetZ = offsetToMergeTextureTogether;
            offsetToHitTheWallInX = true;
        } else if (behind.getX() < banner.getX()) {
            p.sendMessage("behind.getX() < banner.getX()");
            yaw = 180;
            offsetZ = -offsetToMergeTextureTogether;
            offsetToHitTheWallInX = true;
            offsetToHitTheWall = -offsetToHitTheWall;
        } else if (behind.getZ() > banner.getZ()) {
            p.sendMessage("behind.getZ() > banner.getZ()");
            yaw = 90;
            offsetX = -offsetToMergeTextureTogether;
        } else if (behind.getZ() < banner.getZ()) {
            p.sendMessage("behind.getZ() < banner.getZ()");
            yaw = -90;
            offsetX = offsetToMergeTextureTogether;
            offsetToHitTheWall = -offsetToHitTheWall;
        }

        p.sendMessage("yaw: " + yaw + "");

        // Add to 1st banner
        if (offsetToHitTheWallInX) {
            offsetX += offsetToHitTheWall;
        } else {
            offsetZ += offsetToHitTheWall;
        }
        createBannerDisplay(banner, itemStack,
                new Location(banner.getWorld(), banner.getX() + offsetX + 0.5f, banner.getY() + 0.5f, banner.getZ() + offsetZ + 0.5f), yaw,
                true);


        // Remove to 2nd banner
        if (offsetToHitTheWallInX) {
            offsetX -= 2 * offsetToHitTheWall;
        } else {
            offsetZ -= 2 * offsetToHitTheWall;
        }
        createBannerDisplay(banner, itemStack,
                new Location(banner.getWorld(), banner.getX() - offsetX + 0.5f, banner.getY() + 0.5f, banner.getZ() - offsetZ + 0.5f), yaw,
                false);


        // remove the placed banner without droping the item
        banner.setType(Material.AIR);


        // TODO
        // Or use a custom interaction so that player can break the banner

        // Or make it break when block behind is broken

        // Interaction interaction = new Interaction();
        // interaction.setInteractionWidth(1);
        // interaction.setInteractionHeight(1);
    }

    /** Return the offset to hit the wall depending on the block behind. */
    private float getOffsetToHitWall(Block behind) {
        if (allWalls.contains(behind.getType()))
            return 0.42f;
        else if (allFences.contains(behind.getType()))
            return 0.55f;
        else if (allGlacePane.contains(behind.getType()))
            return 0.6f;
        else
            return 0.18f;
    }


    /** Create 2 item display, rotate them and place them where the banner is. */
    private ItemDisplay createBannerDisplay(Block banner, ItemStack itemStack, Location location, float yaw, boolean isFirst) {
        // BlockDisplay don't work with banners
        ItemDisplay itemDisplay = banner.getWorld().spawn(location, ItemDisplay.class);
        itemDisplay.setItemStack(itemStack);
        if (isFirst) {
            // @formatter:off
            itemDisplay.setTransformationMatrix(new Matrix4f(
                    0, -1, 0, 0,
                    -1, 0, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1));
            // @formatter:on
        } else {
            // @formatter:off
            itemDisplay.setTransformationMatrix(new Matrix4f(
                    0, 1, 0, 0,
                    1, 0, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1));
            // @formatter:on
        }

        itemDisplay.setRotation(yaw + (isFirst ? 0 : 180), 0);
        return itemDisplay;
    }
}
