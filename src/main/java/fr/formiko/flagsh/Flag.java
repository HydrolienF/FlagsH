package fr.formiko.flagsh;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.joml.Matrix4f;

public class Flag implements Serializable {
    private final List<UUID> itemDisplaysIds;
    private final List<UUID> interactionsIds;
    private final int x;
    private final int y;
    private final int z;
    private final int yaw;
    private final float offsetToFitTheWall;
    private final UUID worldId;
    private float size;
    private final boolean flagNotBanner; // false = banner, true = flag
    // private final ItemStack itemStack;

    public Flag(int x, int y, int z, UUID worldId, boolean flagNotBanner, int yaw, float offsetToFitTheWall) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldId = worldId;
        this.flagNotBanner = flagNotBanner;
        this.yaw = yaw;
        this.offsetToFitTheWall = offsetToFitTheWall;
        size = 1;
        itemDisplaysIds = new ArrayList<>();
        interactionsIds = new ArrayList<>();
    }
    public Flag(Block block, boolean flagNotBanner, Block behind) {
        this(block.getX(), block.getY(), block.getZ(), block.getWorld().getUID(), flagNotBanner,
                getYawFromBehindAndBannerBlocks(block, behind), FlagsH.getOffsetToHitWall(behind.getType()));
    }

    public List<UUID> getItemDisplaysIds() { return itemDisplaysIds; }
    public List<UUID> getInteractionsIds() { return interactionsIds; }
    public final int getX() { return x; }
    public final int getY() { return y; }
    public final int getZ() { return z; }
    public final int getYaw() { return yaw; }
    public final Location getLocation() { return new Location(getWorld(), getX(), getY(), getZ()); }
    public final UUID getWorldId() { return worldId; }
    public final World getWorld() { return FlagsH.plugin.getServer().getWorld(worldId); }
    public float getSize() { return size; }
    public final boolean isFlag() { return flagNotBanner; }
    public final boolean isBanner() { return !flagNotBanner; }
    public ItemStack getItemStack() {
        return !itemDisplaysIds.isEmpty() && getWorld().getEntity(itemDisplaysIds.get(0)) instanceof ItemDisplay itemDisplay
                ? itemDisplay.getItemStack()
                : null;
    }


    public void create(ItemStack itemStack) {
        float offsetToHitTheWall = offsetToFitTheWall - (0.335f * (size - 1f));
        float offsetToMergeTextureTogether = 0.02f * size;
        boolean offsetToHitTheWallInX = false;
        float offsetX = 0;
        float offsetZ = 0;
        if (yaw == 0) {
            offsetZ = offsetToMergeTextureTogether;
            offsetToHitTheWallInX = true;
        } else if (yaw == 180) {
            offsetZ = -offsetToMergeTextureTogether;
            offsetToHitTheWallInX = true;
            offsetToHitTheWall = -offsetToHitTheWall;
        } else if (yaw == 90) {
            offsetX = -offsetToMergeTextureTogether;
        } else if (yaw == -90) {
            offsetX = offsetToMergeTextureTogether;
            offsetToHitTheWall = -offsetToHitTheWall;
        }


        // Add to 1st banner
        if (offsetToHitTheWallInX) {
            offsetX += offsetToHitTheWall;
        } else {
            offsetZ += offsetToHitTheWall;
        }

        for (int i = 0; i < 10; i++) {
            float hitboxSize = 0.2f * size;
            Location interactionLoc = new Location(getWorld(), getX() + offsetX + 0.5f, getY() + 0.5f - size / 2, getZ() + offsetZ + 0.5f);
            float offsetOfHitbox = hitboxSize * (i - 2);
            if (yaw == 0) {
                interactionLoc.setX(interactionLoc.getX() - offsetOfHitbox);
            } else if (yaw == 180) {
                interactionLoc.setX(interactionLoc.getX() + offsetOfHitbox);
            } else if (yaw == 90) {
                interactionLoc.setZ(interactionLoc.getZ() - offsetOfHitbox);
            } else if (yaw == -90) {
                interactionLoc.setZ(interactionLoc.getZ() + offsetOfHitbox);
            }
            Interaction interaction = createInteraction(interactionLoc, hitboxSize, 0.95f * size);
            interactionsIds.add(interaction.getUniqueId());
        }
        ItemDisplay id1 = createBannerDisplay(itemStack,
                new Location(getWorld(), getX() + offsetX + 0.5f, getY() + 0.5f, getZ() + offsetZ + 0.5f), yaw, true, size);


        // Remove to 2nd banner
        if (offsetToHitTheWallInX) {
            offsetX -= 2 * offsetToHitTheWall;
        } else {
            offsetZ -= 2 * offsetToHitTheWall;
        }
        ItemDisplay id2 = createBannerDisplay(itemStack,
                new Location(getWorld(), getX() - offsetX + 0.5f, getY() + 0.5f, getZ() - offsetZ + 0.5f), yaw, false, size);


        itemDisplaysIds.add(id1.getUniqueId());
        itemDisplaysIds.add(id2.getUniqueId());
    }

    public void extend(float newSize) {
        ItemStack itemStack = getItemStack();
        removeEntities();
        size = newSize;
        create(itemStack);
        playSound(Sound.BLOCK_WOOL_PLACE);
    }

    public void remove() {
        ItemStack item = getItemStack().clone();
        item.setAmount((int) (1 + (getSize() - 1) / FlagsH.plugin.getConfig().getDouble("increasingSizeStep")));
        getWorld().dropItem(getLocation(), item);
        removeEntities();
        FlagsH.plugin.getFlags().remove(this);
        playSound(Sound.BLOCK_WOOD_BREAK);
    }

    // private methods ------------------------------------------------------------------------------------------------
    private static int getYawFromBehindAndBannerBlocks(Block banner, Block behind) {
        if (behind.getX() > banner.getX()) {
            return 0;
        } else if (behind.getX() < banner.getX()) {
            return 180;
        } else if (behind.getZ() > banner.getZ()) {
            return 90;
        } else if (behind.getZ() < banner.getZ()) {
            return -90;
        } else {
            FlagsH.plugin.getLogger()
                    .warning("Flag.getYawFromBehindAndBannerBlocks() : behind and banner blocks are at the same location.");
            return 0;
        }
    }

    private void removeEntities() {
        for (UUID id : itemDisplaysIds) {
            FlagsH.plugin.getServer().getEntity(id).remove();
        }
        for (UUID id : interactionsIds) {
            FlagsH.plugin.getServer().getEntity(id).remove();
        }
        itemDisplaysIds.clear();
        interactionsIds.clear();
    }

    public void playSound(Sound sound) { getWorld().playSound(new Location(getWorld(), x, y, z), sound, SoundCategory.BLOCKS, 1, 0); }

    private static Interaction createInteraction(Location location, float width, float height) {
        Interaction interaction = location.getWorld().spawn(location, Interaction.class);
        interaction.setInteractionWidth(width);
        interaction.setInteractionHeight(height);
        interaction.setResponsive(true);
        interaction.setPersistent(true);
        return interaction;
    }
    /** Create 2 item display, rotate them and place them where the banner is. */
    private ItemDisplay createBannerDisplay(ItemStack itemStack, Location location, float yaw, boolean isFirst, float size) {
        // BlockDisplay don't work with banners
        ItemDisplay itemDisplay = getWorld().spawn(location, ItemDisplay.class);
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
}
