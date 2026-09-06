package dev.lunaa.moonbreak.block;

import dev.lunaa.moonbreak.MoonBreak;
import org.bukkit.*;

import java.util.*;

public class CustomBlockManagerImpl implements CustomBlockManager {

    private final HashMap<WorldChunkKey, HashMap<ChunkBlockKey, CustomBlockType>> placedBlocks = new HashMap<>();

    public void clearAllChunkData() {
        placedBlocks.clear();
    }

    public void clearChunkData(Chunk chunk) {
        WorldChunkKey worldChunkKey = WorldChunkKey.from(chunk);
        placedBlocks.remove(worldChunkKey);
    }

    public int countAllChunksWithBlocks() {
        return placedBlocks.size();
    }

    public int countAllBlocks() {
        int count = 0;
        for (Map.Entry<WorldChunkKey, HashMap<ChunkBlockKey, CustomBlockType>> entry : placedBlocks.entrySet()) {
            count += entry.getValue().size();
        }
        return count;
    }

    public ArrayList<Chunk> chunksWithBlocks() {
        ArrayList<Chunk> chunks = new ArrayList<>();
        Set<WorldChunkKey> worldChunkKeys = Set.copyOf(placedBlocks.keySet());
        if (worldChunkKeys.isEmpty()) return chunks;

        for (WorldChunkKey worldChunkKey : worldChunkKeys) {
            World world = Bukkit.getWorld(worldChunkKey.worldId());
            if (world == null) {
                MoonBreak.logger().warning("Tried obtaining chunks with blocks. World with the id " + worldChunkKey.worldId() + " was not found. Placed blocks will be deleted.");
                placedBlocks.remove(worldChunkKey);
                continue;
            }

            chunks.add(world.getChunkAt(worldChunkKey.chunkKey(), false));
        }

        return chunks;
    }

    @SuppressWarnings("unchecked")
    public Optional<HashMap<ChunkBlockKey, CustomBlockType>> blocksInChunk(Chunk chunk) {
        HashMap<ChunkBlockKey, CustomBlockType> blocks = placedBlocks.get(WorldChunkKey.from(chunk));
        if (blocks == null) return Optional.empty();
        return Optional.of((HashMap<ChunkBlockKey, CustomBlockType>) blocks.clone());
    }

    @SuppressWarnings("unchecked")
    public void placeChunkBlocks(Chunk chunk, HashMap<ChunkBlockKey, CustomBlockType> blocks) {
        placedBlocks.put(WorldChunkKey.from(chunk), (HashMap<ChunkBlockKey, CustomBlockType>) blocks.clone());
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
            if (setAir) location.getBlock().setType(Material.AIR);
            if (!blocks.isEmpty()) return;
            MoonBreak.instance().blockLoader().wipeSavedChunkData(location.getChunk());
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
