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
     * Places a block in the world at the given location.
     * @param location the location
     * @param type the block type
     * @throws IllegalStateException if the block's location is not set
     */
    void place(Location location, CustomBlockType type);

    /**
     * Removes a block from the world at the given location.
     * @param location the location
     * @param setAir if true, the block's location will be set to air
     */
    void remove(Location location, boolean setAir);

    /**
     * Removes a block from the world if it has been placed.
     * @param location the location
     * @param setAir if true, the block's location will be set to air
     * @return if the block was removed
     */
    boolean removeIfPlaced(Location location, boolean setAir);

    /**
     * Tries to move a block from a specified position to another.
     * This should never throw but will return {@code false} if the {@code to} location is occupied
     * or no block is present at the {@code from} location.
     * @param from from location
     * @param to to location
     * @return if the block was moved
     */
    boolean move(Location from, Location to);

    /**
     * See {@link CustomBlockManager#move(Location, Location)}.<br>
     * Tries to move a block from a specified position to another.
     * This should never throw but will return {@code false} if the {@code to} location is occupied
     * or no block is present at the {@code from} location.<br>
     * If moved virtually, the physical block will be left untouched. This is useful when moving a block
     * that is physically being moved otherwise (e.g. a piston pushing).
     * @param from from location
     * @param to to location
     * @param virtual if the physical block should be left untouched
     * @return if the block was moved
     */
    boolean move(Location from, Location to, boolean virtual);

    /**
     * Returns if a block is placed at the given location.
     * @param location the location
     * @return if a block is placed
     */
    boolean isPlaced(Location location);

    /**
     * Returns if a block of a given type is placed at the given location.
     * @param location the location
     * @param type the type
     * @return if a block of the given type is placed
     */
    boolean isPlaced(Location location, CustomBlockType type);

    /**
     * Returns the block placed at the given location, if one is present.
     * @param location the location
     * @return the block if present
     */
    Optional<CustomBlockType> get(Location location);

}
