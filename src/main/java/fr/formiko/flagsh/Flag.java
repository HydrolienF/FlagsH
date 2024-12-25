package fr.formiko.flagsh;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Matrix4f;

public class Flag implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id; // final (But Jackson need it not final)
    private List<UUID> itemDisplaysIds; // final (But Jackson need it not final)
    private List<UUID> interactionsIds; // final (But Jackson need it not final)
    private int x; // final (But Jackson need it not final)
    private int y; // final (But Jackson need it not final)
    private int z; // final (But Jackson need it not final)
    private int yaw; // final (But Jackson need it not final)
    private float offsetToFitTheWall; // final (But Jackson need it not final)
    private UUID worldId; // final (But Jackson need it not final)
    private float size;
    private boolean flagNotBanner; // false = banner, true = flag // final (But Jackson need it not final)

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
    public Flag(int x, int y, int z, @Nonnull UUID worldId, boolean flagNotBanner, int yaw, float offsetToFitTheWall) {
        this.id = UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldId = worldId;
        this.flagNotBanner = flagNotBanner;
        this.yaw = yaw;
        this.offsetToFitTheWall = offsetToFitTheWall;
        this.size = 1;
        this.itemDisplaysIds = new ArrayList<>();
        this.interactionsIds = new ArrayList<>();
    }
    public Flag(@Nonnull Block block, boolean flagNotBanner, @Nonnull Block behind) {
        this(block.getX(), block.getY(), block.getZ(), block.getWorld().getUID(), flagNotBanner,
                getYawFromBehindAndBannerBlocks(block, behind), FlagsH.getOffsetToHitWall(behind.getType()) + (flagNotBanner ? 0f : 0.3f));
    }
    // Only for jackson
    private Flag() {}

    public @Nonnull List<UUID> getItemDisplaysIds() { return itemDisplaysIds; }
    public @Nonnull List<UUID> getInteractionsIds() { return interactionsIds; }
    public final int getX() { return x; }
    public final int getY() { return y; }
    public final int getZ() { return z; }
    public final int getYaw() { return yaw; }
    public final Location getLocation() { return new Location(getWorld(), getX(), getY(), getZ()); }
    public final @Nonnull UUID getWorldId() { return worldId; }
    public final @Nullable World getWorld() { return FlagsH.getPlugin().getServer().getWorld(worldId); }
    public float getSize() { return size; }
    public final boolean isFlag() { return flagNotBanner; }
    public final boolean isBanner() { return !flagNotBanner; }
    public @Nullable ItemStack getItemStack() {
        return !itemDisplaysIds.isEmpty() && getWorld() != null
                && getWorld().getEntity(itemDisplaysIds.get(0)) instanceof ItemDisplay itemDisplay ? itemDisplay.getItemStack() : null;
    }


    /**
     * Create the flag minecraft entities that will be displayed & used as hitbox.
     * 
     * @param itemStack item which texture will be used
     */
    public void create(@Nonnull ItemStack itemStack) {
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

        // String json = toJson();
        // FlagsH.getPlugin().debug("Create a flag:" + json);
        
        // For all Entity in itemDisplaysIds & interactionsIds add a PersistentDataContainer with the flag data
        getEntitiesStream().forEach(e -> {
            if (e != null) {
                e.getPersistentDataContainer().set(FlagsH.getFlagDataNamespacedKey(), PersistentDataType.STRING, toJson());
            }
        });
    }

    private Stream<Entity> getEntitiesStream() {
        return Stream.concat(itemDisplaysIds.stream(), interactionsIds.stream()).map(FlagsH.getPlugin().getServer()::getEntity);
    }

    private void addItemDisplayForFlag(@Nonnull ItemStack itemStack, float offsetToHitTheWall, boolean offsetToHitTheWallInX) {
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
        ItemStack itemStack = getItemStack();
        if (itemStack == null) {
            FlagsH.getPlugin().getLogger().warning(
                    "Flag.remove(): itemStack of the flag is null. It might be cause by a change of the world or a /kill. This flag won't drop any item.");
            removeEntities();
            boolean removed = FlagsH.getPlugin().getFlags().remove(this);
            if (!removed) {
                FlagsH.getPlugin().getLogger().warning("Flag.remove(): flag not removed from the list of flags. flag: " + this);
            }
        } else {
            ItemStack item = itemStack.clone();
            item.setAmount((int) (1 + (getSize() - 1) / FlagsHConfig.increasingSizeStep()));
            getWorld().dropItem(getLocation(), item);
            removeEntities();
            boolean removed = FlagsH.getPlugin().getFlags().remove(this);
            if (!removed) {
                FlagsH.getPlugin().getLogger().warning("Flag.remove(): flag not removed from the list of flags. flag: " + this);
            }
            playSound(Sound.BLOCK_WOOD_BREAK);
        }
    }

    // private methods ------------------------------------------------------------------------------------------------
    /**
     * Calculate the yaw of the flag from the banner and the block behind coordinates.
     * 
     * @param banner banner block
     * @param behind block behind the banner
     * @return
     */
    private static int getYawFromBehindAndBannerBlocks(@Nonnull Block banner, @Nonnull Block behind) {
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
                    .warning("Flag.getYawFromBehindAndBannerBlocks(): behind and banner blocks are at the same location.");
            return 0;
        }
    }

    /**
     * Remove the entities of the flag.
     */
    private void removeEntities() {
        getEntitiesStream().forEach(e -> {
            if (e == null) {
                FlagsH.getPlugin().getLogger().warning(() -> String.format(
                        "Flag.removeEntities(): An entity is null an haven't been removed. It might be cause by a change of the world or a /kill. flag: %s",
                        this));
            } else {
                e.remove();
            }
        });
        itemDisplaysIds.clear();
        interactionsIds.clear();
    }

    /**
     * Play a sound at the flag location.
     * 
     * @param sound sound to play
     */
    public void playSound(@Nonnull Sound sound) {
        getWorld().playSound(new Location(getWorld(), x, y, z), sound, SoundCategory.BLOCKS, 1, 0);
    }

    /** Create an interaction at the given location. */
    private static Interaction createInteraction(@Nonnull Location location, float width, float height) {
        Interaction interaction = location.getWorld().spawn(location, Interaction.class);
        interaction.setInteractionWidth(width);
        interaction.setInteractionHeight(height);
        interaction.setResponsive(true);
        interaction.setPersistent(true);
        return interaction;
    }
    /** Create item display, rotate it and place it where the banner is. */
    private @Nonnull ItemDisplay createBannerDisplay(@Nonnull ItemStack itemStack, @Nonnull Location location, float yaw, boolean isFirst,
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
    public @Nonnull String toString() { return (flagNotBanner ? "Flag" : "Banner") + " (" + x + ", " + y + ", " + z + ") size: " + size; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Flag flag) {
            return flag.id != null && id != null && flag.id.equals(id);
        }
        return false;
    }

    public static @Nonnull Flag fromJson(@Nonnull String json) {
        try {
            return FlagsH.getObjectMapper().readValue(json, Flag.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Flag.fromJson(): " + e.getMessage());
        }
    }
    public @Nonnull String toJson() {
        try {
            return FlagsH.getObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            throw new IllegalArgumentException("Flag.toJson(): " + e.getMessage());
        }
    }
}
