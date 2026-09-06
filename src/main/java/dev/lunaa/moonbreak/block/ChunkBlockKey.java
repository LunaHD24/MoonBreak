package dev.lunaa.moonbreak.block;

import org.bukkit.Location;

/**
 * Represents a location inside a chunk
 * @param x chunk x coordinate (0-15)
 * @param y chunk y coordinate (overworld: -64-320; nether/end: 0-256; values may be changed by datapacks)
 * @param z chunk z coordinate (0-15)
 */
public record ChunkBlockKey(byte x, short y, byte z) {

    public static ChunkBlockKey from(Location location) {
        byte x = (byte) (location.getBlockX() & 0xF);
        short y = (short) location.getBlockY();
        byte z = (byte) (location.getBlockZ() & 0xF);

        return new ChunkBlockKey(x, y, z);
    }

}
