package dev.lunaa.moonbreak.block;

import dev.lunaa.moonbreak.MoonBreakApi;
import org.bukkit.Location;

import java.util.Optional;

/**
 * Represents a way to manage all CustomBlocks currently placed and loaded
 */
public interface CustomBlockManager {

    /**
     * Returns the manager instance
     * @return the instance
     */
    static CustomBlockManager manager() {
        return MoonBreakApi.provider().blockManager();
    }

    /**
     * Places a block in the world at its given location.
     * @param block the block
     * @throws IllegalStateException if the block's location is not set
     */
    void place(CustomBlock block);

    /**
     * Removes a block from the world at the given location.
     * @param location the location
     * @param setAir if true, the block's location will be set to air
     */
    void remove(Location location, boolean setAir);

    /**
     * Removes a block from the world if it has been placed.
     * @param block the block
     * @param setAir if true, the block's location will be set to air
     * @return if the block was removed
     */
    boolean removeIfPlaced(CustomBlock block, boolean setAir);

    /**
     * Returns if a block is placed at the given location.
     * @param location the location
     * @return if a block is placed
     */
    boolean isPlaced(Location location);

    /**
     * Returns if a block of a given type is placed at the given location.
     * @param location the lcoation
     * @param type the type
     * @return if a block of the given type is placed
     */
    boolean isPlaced(Location location, CustomBlockType type);

    /**
     * Returns the block placed at the given location, if one is present.
     * @param location the location
     * @return the block if present
     */
    Optional<CustomBlock> get(Location location);

}
