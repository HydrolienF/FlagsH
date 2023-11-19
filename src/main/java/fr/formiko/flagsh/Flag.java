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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class Flag implements Serializable {
    private static final long serialVersionUID = 1L;
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

    /**
     * Create a flag or banner.
     * It will be display with ItemDisplay entities.
     * It will have an hitbox with Interaction entities.
     * 
     * @param x                  x coordinate of the flag
     * @param y                  y coordinate of the flag
     * @param z                  z coordinate of the flag
     * @param worldId            world id of the flag
     * @param flagNotBanner      true if the flag is a flag, false if it's a banner
     * @param yaw                yaw of the flag
     * @param offsetToFitTheWall offset to place flag against the wall
     */
    public Flag(int x, int y, int z, @NotNull UUID worldId, boolean flagNotBanner, int yaw, float offsetToFitTheWall) {
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
    public Flag(@NotNull Block block, boolean flagNotBanner, @NotNull Block behind) {
        this(block.getX(), block.getY(), block.getZ(), block.getWorld().getUID(), flagNotBanner,
                getYawFromBehindAndBannerBlocks(block, behind), FlagsH.getOffsetToHitWall(behind.getType()) + (flagNotBanner ? 0f : 0.3f));
    }

    public @NotNull List<UUID> getItemDisplaysIds() { return itemDisplaysIds; }
    public @NotNull List<UUID> getInteractionsIds() { return interactionsIds; }
    public final int getX() { return x; }
    public final int getY() { return y; }
    public final int getZ() { return z; }
    public final int getYaw() { return yaw; }
    public final Location getLocation() { return new Location(getWorld(), getX(), getY(), getZ()); }
    public final @NotNull UUID getWorldId() { return worldId; }
    public final World getWorld() { return FlagsH.getPlugin().getServer().getWorld(worldId); }
    public float getSize() { return size; }
    public final boolean isFlag() { return flagNotBanner; }
    public final boolean isBanner() { return !flagNotBanner; }
    public @Nullable ItemStack getItemStack() {
        return !itemDisplaysIds.isEmpty() && getWorld().getEntity(itemDisplaysIds.get(0)) instanceof ItemDisplay itemDisplay
                ? itemDisplay.getItemStack()
                : null;
    }


    /**
     * Create the flag minecraft entities that will be displayed & used as hitbox.
     * 
     * @param itemStack item witch texture will be used
     */
    public void create(@NotNull ItemStack itemStack) {
        float offsetToHitTheWall = offsetToFitTheWall - ((isFlag() ? 0.335f : 0.05f) * (size - 1f));
        boolean offsetToHitTheWallInX = false;
        if (yaw == 0) {
            offsetToHitTheWallInX = true;
        } else if (yaw == 180) {
            offsetToHitTheWallInX = true;
            offsetToHitTheWall = -offsetToHitTheWall;
        } else if (yaw == -90) {
            offsetToHitTheWall = -offsetToHitTheWall;
        }


        if (isFlag()) {
            addItemDisplayForFlag(itemStack, offsetToHitTheWall, offsetToHitTheWallInX);
            addInteractionForFlag();
        } else {
            addItemDisplayForBanner(itemStack, offsetToHitTheWall, offsetToHitTheWallInX);
            addInteractionForBanner();
        }

    }

    private void addItemDisplayForFlag(@NotNull ItemStack itemStack, float offsetToHitTheWall, boolean offsetToHitTheWallInX) {
        float offsetX = 0;
        float offsetZ = 0;
        float offsetToMergeTextureTogether = 0.01f * size;
        // Add offset to 1st banner
        if (offsetToHitTheWallInX) {
            offsetX += offsetToHitTheWall;
            offsetZ += offsetToMergeTextureTogether;
        } else {
            offsetZ += offsetToHitTheWall;
            offsetX += offsetToMergeTextureTogether;
        }
        ItemDisplay id1 = createBannerDisplay(itemStack,
                new Location(getWorld(), getX() + offsetX + 0.5f, getY() + 0.5f, getZ() + offsetZ + 0.5f), yaw, true, size);
        itemDisplaysIds.add(id1.getUniqueId());

        // Remove offset to 2nd banner
        if (offsetToHitTheWallInX) {
            offsetX -= 2 * offsetToHitTheWall;
            offsetZ -= 2 * offsetToMergeTextureTogether;
        } else {
            offsetZ -= 2 * offsetToHitTheWall;
            offsetX -= 2 * offsetToMergeTextureTogether;
        }
        ItemDisplay id2 = createBannerDisplay(itemStack,
                new Location(getWorld(), getX() - offsetX + 0.5f, getY() + 0.5f, getZ() - offsetZ + 0.5f), yaw, false, size);
        itemDisplaysIds.add(id2.getUniqueId());
    }

    private void addItemDisplayForBanner(ItemStack itemStack, float offsetToHitTheWall, boolean offsetToHitTheWallInX) {
        float offsetX = 0;
        float offsetZ = 0;
        // Add offset to 1st banner
        if (offsetToHitTheWallInX) {
            offsetX += offsetToHitTheWall;
        } else {
            offsetZ += offsetToHitTheWall;
        }
        ItemDisplay id1 = createBannerDisplay(itemStack,
                new Location(getWorld(), getX() + offsetX + 0.5f, getY() + 1f - (getSize() * 1.335f), getZ() + offsetZ + 0.5f), yaw - 90f,
                true, size);
        itemDisplaysIds.add(id1.getUniqueId());
    }

    /**
     * Add hitboxs to the flag.
     * Hitboxs will be used to extend or destory the flag.
     */
    private void addInteractionForFlag() {
        for (int i = 0; i < 11; i++) {
            float hitboxSize = 0.2f * size;
            Location interactionLoc = new Location(getWorld(), getX() + 0.5f, getY() + 0.5f - size / 2, getZ() + 0.5f);

            float offsetOfHitbox = hitboxSize * i - 0.7f - getSize() * 0.2f;
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
    }
    /**
     * Add hitboxs to the banner.
     * Hitboxs will be used to extend or destory the banner.
     */
    private void addInteractionForBanner() {
        for (int i = 0; i < 5; i++) {
            float hitboxSize = 0.2f * size;
            Location interactionLoc = new Location(getWorld(), getX() + hitboxSize / 2, getY() + 0.8f - (size * 1.8f),
                    getZ() + hitboxSize / 2);

            float offsetOfHitbox = hitboxSize * i;
            if (yaw == 0) {
                interactionLoc.setZ(interactionLoc.getZ() - offsetOfHitbox + 1f - hitboxSize + 0.5f * (size - 1f));
                interactionLoc.setX(interactionLoc.getX() + 1f - hitboxSize + offsetToFitTheWall - 0.18f * 2);
            } else if (yaw == 180) {
                interactionLoc.setZ(interactionLoc.getZ() + offsetOfHitbox - 0.5f * (size - 1f));
                interactionLoc.setX(interactionLoc.getX() - offsetToFitTheWall + 0.18f * 2);
            } else if (yaw == 90) {
                interactionLoc.setX(interactionLoc.getX() - offsetOfHitbox + 1f - hitboxSize + 0.5f * (size - 1f));
                interactionLoc.setZ(interactionLoc.getZ() + 1f - hitboxSize + offsetToFitTheWall - 0.18f * 2);
            } else if (yaw == -90) {
                interactionLoc.setX(interactionLoc.getX() + offsetOfHitbox - 0.5f * (size - 1f));
                interactionLoc.setZ(interactionLoc.getZ() - offsetToFitTheWall + 0.18f * 2);
            }
            Interaction interaction = createInteraction(interactionLoc, hitboxSize, 0.95f * size * 2);
            interactionsIds.add(interaction.getUniqueId());
        }
    }

    /**
     * Extends the flag.
     * 
     * @param newSize new size of the flag
     */
    public void extend(float newSize) {
        ItemStack itemStack = getItemStack();
        removeEntities();
        size = newSize;
        create(itemStack);
        playSound(Sound.BLOCK_WOOL_PLACE);
    }

    /**
     * Remove the flag.
     * It remove the entities and remove the flag from the list of flags.
     */
    public void remove() {
        ItemStack item = getItemStack().clone();
        item.setAmount((int) (1 + (getSize() - 1) / FlagsH.getPlugin().getConfig().getDouble("increasingSizeStep")));
        getWorld().dropItem(getLocation(), item);
        removeEntities();
        FlagsH.getPlugin().getFlags().remove(this);
        playSound(Sound.BLOCK_WOOD_BREAK);
    }

    // private methods ------------------------------------------------------------------------------------------------
    /**
     * Calculate the yaw of the flag from the banner and the block behind coordinates.
     * 
     * @param banner banner block
     * @param behind block behind the banner
     * @return
     */
    private static int getYawFromBehindAndBannerBlocks(@NotNull Block banner, @NotNull Block behind) {
        if (behind.getX() > banner.getX()) {
            return 0;
        } else if (behind.getX() < banner.getX()) {
            return 180;
        } else if (behind.getZ() > banner.getZ()) {
            return 90;
        } else if (behind.getZ() < banner.getZ()) {
            return -90;
        } else {
            FlagsH.getPlugin().getLogger()
                    .warning("Flag.getYawFromBehindAndBannerBlocks() : behind and banner blocks are at the same location.");
            return 0;
        }
    }

    /**
     * Remove the entities of the flag.
     */
    private void removeEntities() {
        for (UUID id : itemDisplaysIds) {
            FlagsH.getPlugin().getServer().getEntity(id).remove();
        }
        for (UUID id : interactionsIds) {
            FlagsH.getPlugin().getServer().getEntity(id).remove();
        }
        itemDisplaysIds.clear();
        interactionsIds.clear();
    }

    /**
     * Play a sound at the flag location.
     * 
     * @param sound sound to play
     */
    public void playSound(@NotNull Sound sound) {
        getWorld().playSound(new Location(getWorld(), x, y, z), sound, SoundCategory.BLOCKS, 1, 0);
    }

    /** Create an interaction at the given location. */
    private static Interaction createInteraction(@NotNull Location location, float width, float height) {
        Interaction interaction = location.getWorld().spawn(location, Interaction.class);
        interaction.setInteractionWidth(width);
        interaction.setInteractionHeight(height);
        interaction.setResponsive(true);
        interaction.setPersistent(true);
        return interaction;
    }
    /** Create item display, rotate it and place it where the banner is. */
    private @NotNull ItemDisplay createBannerDisplay(@NotNull ItemStack itemStack, @NotNull Location location, float yaw, boolean isFirst,
            float size) {
        // BlockDisplay don't work with banners
        ItemDisplay itemDisplay = getWorld().spawn(location, ItemDisplay.class);
        itemDisplay.setItemStack(itemStack);
        if (isFlag()) {
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
        } else {
            // @formatter:off
            itemDisplay.setTransformationMatrix(new Matrix4f(
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1/size));
            // @formatter:on
        }

        itemDisplay.setRotation(yaw, 0);
        return itemDisplay;
    }

    @Override
    public @NotNull String toString() { return (flagNotBanner ? "Flag" : "Banner") + " (" + x + ", " + y + ", " + z + ") size: " + size; }
}
