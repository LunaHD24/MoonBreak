package dev.lunaa.moonbreak.block;

import dev.lunaa.moonbreak.MoonBreakApi;
import org.bukkit.Location;

import java.util.Optional;

/**
 * Represents an instance of a CustomBlock, holding information about its location and placement.
 * Based on a {@link CustomBlockType} it can be used to place the represented block in the world.
 */
public interface CustomBlock {

    /**
     * Creates a new CustomBlock instance without a location, based on a given BlockType
     * @param type the type
     * @return the CustomBlock
     */
    static CustomBlock of(CustomBlockType type) {
        return MoonBreakApi.provider().blockOfType(type);
    }

    /**
     * Creates a new CustomBlock instance with a location, based on a given BlockType
     * @param type the type
     * @return the CustomBlock
     */
    static CustomBlock of(CustomBlockType type, Location location) {
        return MoonBreakApi.provider().blockOfType(type, location);
    }

    /**
     * Returns the BlockType of this CustomBlock
     * @return the BlockType
     */
    CustomBlockType type();

    /**
     * Sets the BlockType of this CustomBlock
     * @param type the BlockType
     */
    void type(CustomBlockType type);

    /**
     * Returns the current location, if not null
     * @return the current location, if present
     */
    Optional<Location> location();

    /**
     * Sets the new location of this CustomBlock and updates the location in-world,
     * if this CustomBlock has been placed (see {@link isPlaced()}).
     * @param location the location
     */
    void location(Location location);

    /**
     * Returns if this block is placed in-world
     * @return if it is placed
     */
    boolean isPlaced();

    /**
     * Places this CustomBlock at it's specified location
     * @throws IllegalStateException if the current location is null
     */
    void placeInWorld();

    /**
     * Updates the in-world location of this CustomBlock if it has been placed (see {@link CustomBlock#isPlaced()})
     */
    void update();
}
