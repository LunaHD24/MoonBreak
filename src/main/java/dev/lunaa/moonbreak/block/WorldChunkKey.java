package dev.lunaa.moonbreak.block;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public record WorldChunkKey(UUID worldId, long chunkKey) {

    public static WorldChunkKey from(Location location) {
        return new WorldChunkKey(location.getWorld().getUID(), Chunk.getChunkKey(location));
    }

    public static WorldChunkKey from(Chunk chunk) {
        return new WorldChunkKey(chunk.getWorld().getUID(), chunk.getChunkKey());
    }

    public static Location location(WorldChunkKey worldChunkKey, ChunkBlockKey chunkBlockKey) throws IllegalArgumentException {
        World world = Bukkit.getWorld(worldChunkKey.worldId());
        if (world == null) throw new IllegalArgumentException("World does not exist");

        Chunk chunk = world.getChunkAt(worldChunkKey.chunkKey(), false);
        return new Location(
                world,
                chunk.getX() * 16 + chunkBlockKey.x(),
                chunkBlockKey.y(),
                chunk.getZ() * 16 + chunkBlockKey.z()
        );
    }

    public Location location(ChunkBlockKey chunkBlockKey) {
        return location(this, chunkBlockKey);
    }

}
