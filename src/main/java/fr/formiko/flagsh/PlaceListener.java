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
    private List<Material> allBanners = List.of(Material.WHITE_BANNER, Material.ORANGE_BANNER, Material.MAGENTA_BANNER,
            Material.LIGHT_BLUE_BANNER, Material.YELLOW_BANNER, Material.LIME_BANNER, Material.PINK_BANNER, Material.GRAY_BANNER,
            Material.LIGHT_GRAY_BANNER, Material.CYAN_BANNER, Material.PURPLE_BANNER, Material.BLUE_BANNER, Material.BROWN_BANNER,
            Material.GREEN_BANNER, Material.RED_BANNER, Material.BLACK_BANNER, Material.WHITE_WALL_BANNER, Material.ORANGE_WALL_BANNER,
            Material.MAGENTA_WALL_BANNER, Material.LIGHT_BLUE_WALL_BANNER, Material.YELLOW_WALL_BANNER, Material.LIME_WALL_BANNER,
            Material.PINK_WALL_BANNER, Material.GRAY_WALL_BANNER, Material.LIGHT_GRAY_WALL_BANNER, Material.CYAN_WALL_BANNER,
            Material.PURPLE_WALL_BANNER, Material.BLUE_WALL_BANNER, Material.BROWN_WALL_BANNER, Material.GREEN_WALL_BANNER,
            Material.RED_WALL_BANNER, Material.BLACK_WALL_BANNER);
    private List<Material> allWallBanners = List.of(Material.WHITE_WALL_BANNER, Material.ORANGE_WALL_BANNER, Material.MAGENTA_WALL_BANNER,
            Material.LIGHT_BLUE_WALL_BANNER, Material.YELLOW_WALL_BANNER, Material.LIME_WALL_BANNER, Material.PINK_WALL_BANNER,
            Material.GRAY_WALL_BANNER, Material.LIGHT_GRAY_WALL_BANNER, Material.CYAN_WALL_BANNER, Material.PURPLE_WALL_BANNER,
            Material.BLUE_WALL_BANNER, Material.BROWN_WALL_BANNER, Material.GREEN_WALL_BANNER, Material.RED_WALL_BANNER,
            Material.BLACK_WALL_BANNER);

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        // if is not a banner return
        if (!allWallBanners.contains(event.getBlock().getType()))
            return;
        if (!event.getPlayer().isSneaking())
            return;

        Player p = event.getPlayer();
        Block banner = event.getBlockPlaced();
        BlockData data = banner.getBlockData();
        Block behind = event.getBlockAgainst();
        ItemStack itemStack = event.getItemInHand();

        p.sendMessage("You placed a wall banner in sneak in " + banner.getLocation() + " " + banner.getType().toString() + " "
                + behind.getType().toString());
        p.sendMessage("blocdata: " + data.getAsString());


        float offsetToMergeTexture = 0.02f;
        float yaw = 0;
        float offsetX = 0;
        float offsetZ = 0;
        if (behind.getX() > banner.getX()) {
            p.sendMessage("behind.getX() > banner.getX()");
            yaw = 0;
            offsetZ = offsetToMergeTexture;
        } else if (behind.getX() < banner.getX()) {
            p.sendMessage("behind.getX() < banner.getX()");
            yaw = 180;
            offsetZ = -offsetToMergeTexture;
        } else if (behind.getZ() > banner.getZ()) {
            p.sendMessage("behind.getZ() > banner.getZ()");
            yaw = 90;
            offsetX = -offsetToMergeTexture;
        } else if (behind.getZ() < banner.getZ()) {
            p.sendMessage("behind.getZ() < banner.getZ()");
            yaw = -90;
            offsetX = offsetToMergeTexture;
        }


        p.sendMessage("yaw: " + yaw + "");

        Location location1 = new Location(banner.getWorld(), banner.getX() + offsetX + 0.5f, banner.getY() + 0.5f,
                banner.getZ() + offsetZ + 0.5f);
        Location location2 = new Location(banner.getWorld(), banner.getX() - offsetX + 0.5f, banner.getY() + 0.5f,
                banner.getZ() - offsetZ + 0.5f);


        // BlockDisplay don't work with banners
        ItemDisplay itemDisplay = banner.getWorld().spawn(location1, ItemDisplay.class);
        itemDisplay.setItemStack(itemStack);
        // @formatter:off
        itemDisplay.setTransformationMatrix(new Matrix4f(
                0, -1, 0, 0,
                -1, 0, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1));
        // @formatter:on

        itemDisplay.setRotation(yaw, 0);

        // BlockDisplay don't work with banners

        // Duplicate the texture to make it look like a flag
        itemDisplay = banner.getWorld().spawn(location2, ItemDisplay.class);
        itemDisplay.setItemStack(itemStack);
        // @formatter:off
        itemDisplay.setTransformationMatrix(new Matrix4f(
                0, 1, 0, 0,
                1, 0, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1));
        // @formatter:on

        itemDisplay.setRotation(yaw + 180, 0);

        // remove the placed banner without droping the item
        banner.setType(Material.AIR);


        // TODO
        // Or use a custom interaction so that player can break the banner

        // Or make it break when block behind is broken

        // Interaction interaction = new Interaction();
        // interaction.setInteractionWidth(1);
        // interaction.setInteractionHeight(1);
    }
}
