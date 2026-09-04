package dev.lunaa.moonbreak.block;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Optional;

public class CustomBlockManagerImpl implements CustomBlockManager {

    private final HashMap<WorldChunkKey, HashMap<ChunkBlockKey, CustomBlockType>> placedBlocks = new HashMap<>();

    public HashMap<WorldChunkKey, HashMap<ChunkBlockKey, CustomBlockType>> getPlacedBlocks() {
        return placedBlocks;
    }

    public void place(Location location, CustomBlockType type, boolean virtual) {
        WorldChunkKey worldChunkKey = WorldChunkKey.from(location);
        ChunkBlockKey chunkBlockKey = ChunkBlockKey.from(location);

        HashMap<ChunkBlockKey, CustomBlockType> blocks = placedBlocks.computeIfAbsent(worldChunkKey, _ -> new HashMap<>());
        if (blocks.containsKey(chunkBlockKey)) {
            throw new IllegalStateException("Tried placing block at an occupied location: " + location);
        }

        if (!virtual) {
            if (location.getBlock().getType() != type.material()) location.getBlock().setType(type.material());
        }
        blocks.put(chunkBlockKey, type);
    }

    @Override
    public void place(Location location, CustomBlockType type) {
        place(location, type, false);
    }

    @Override
    public void remove(Location location, boolean setAir) {
        WorldChunkKey worldChunkKey = WorldChunkKey.from(location);
        HashMap<ChunkBlockKey, CustomBlockType> blocks = placedBlocks.get(worldChunkKey);
        if (blocks == null) return;

        ChunkBlockKey chunkBlockKey = ChunkBlockKey.from(location);
        if (blocks.remove(chunkBlockKey) != null) {
            if (blocks.isEmpty()) placedBlocks.remove(worldChunkKey);
            if (setAir) location.getBlock().setType(Material.AIR);
        }
    }

    @Override
    public boolean removeIfPlaced(Location location, boolean setAir) {
        if (!isPlaced(location)) return false;
        remove(location, setAir);
        return true;
    }

    @Override
    public boolean move(Location from, Location to) {
        return move(from, to, false);
    }

    @Override
    public boolean move(Location from, Location to, boolean virtual) {
        if (!isPlaced(from) || isPlaced(to)) return false;

        CustomBlockType type = get(from).orElseThrow();
        place(to, type, virtual);
        remove(from, !virtual);

        return true;
    }

    @Override
    public boolean isPlaced(Location location) {
        WorldChunkKey worldChunkKey = WorldChunkKey.from(location);
        if (!placedBlocks.containsKey(worldChunkKey)) return false;

        ChunkBlockKey chunkBlockKey = ChunkBlockKey.from(location);
        return placedBlocks.get(worldChunkKey).containsKey(chunkBlockKey);
    }

    @Override
    public boolean isPlaced(Location location, CustomBlockType type) {
        WorldChunkKey worldChunkKey = WorldChunkKey.from(location);
        if (!placedBlocks.containsKey(worldChunkKey)) return false;

        ChunkBlockKey chunkBlockKey = ChunkBlockKey.from(location);
        HashMap<ChunkBlockKey, CustomBlockType> blocks = placedBlocks.get(worldChunkKey);
        return blocks.containsKey(chunkBlockKey) && blocks.get(chunkBlockKey) == type;
    }

    @Override
    public Optional<CustomBlockType> get(Location location) {
        WorldChunkKey worldChunkKey = WorldChunkKey.from(location);
        if (!placedBlocks.containsKey(worldChunkKey)) return Optional.empty();

        ChunkBlockKey chunkBlockKey = ChunkBlockKey.from(location);
        HashMap<ChunkBlockKey, CustomBlockType> blocks = placedBlocks.get(worldChunkKey);
        return Optional.ofNullable(blocks.get(chunkBlockKey));
    }
}
