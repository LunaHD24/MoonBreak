package dev.lunaa.moonbreak.block;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Optional;

public class CustomBlockManagerImpl implements CustomBlockManager {

    private final HashMap<Long, HashMap<Location, CustomBlockType>> placedBlocks = new HashMap<>();

    public HashMap<Long, HashMap<Location, CustomBlockType>> getPlacedBlocks() {
        return placedBlocks;
    }

    public void place(Location location, CustomBlockType type, boolean virtual) {
        location = stripLocation(location);
        long chunkKey = Chunk.getChunkKey(location);
        boolean contains = placedBlocks.containsKey(chunkKey);

        HashMap<Location, CustomBlockType> blocks = contains ? placedBlocks.get(chunkKey) : new HashMap<>();
        if (contains) {
            if (blocks.containsKey(location)) {
                throw new IllegalStateException("Tried placing block at an occupied location: " + location);
            }
        } else {
            placedBlocks.put(chunkKey, blocks);
        }

        if (!virtual) location.getBlock().setType(type.material());
        blocks.put(location, type);
    }

    @Override
    public void place(Location location, CustomBlockType type) {
        place(location, type, false);
    }

    @Override
    public void remove(Location location, boolean setAir) {
        location = stripLocation(location);
        if (!isPlaced(location)) return;

        long chunkKey = Chunk.getChunkKey(location);
        if (!placedBlocks.containsKey(chunkKey)) return;

        HashMap<Location, CustomBlockType> blocks = placedBlocks.get(chunkKey);
        blocks.remove(location);
        if (blocks.isEmpty()) placedBlocks.remove(chunkKey);

        if (setAir) location.getBlock().setType(Material.AIR);
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
        from = stripLocation(from);
        to = stripLocation(to);
        if (!isPlaced(from) || isPlaced(to)) return false;

        CustomBlockType type = get(from).orElseThrow();
        place(to, type, virtual);
        remove(from, !virtual);

        return true;
    }

    @Override
    public boolean isPlaced(Location location) {
        location = stripLocation(location);
        long chunkKey = Chunk.getChunkKey(location);

        if (!placedBlocks.containsKey(chunkKey)) return false;
        return placedBlocks.get(chunkKey).containsKey(location);
    }

    @Override
    public boolean isPlaced(Location location, CustomBlockType type) {
        location = stripLocation(location);
        long chunkKey = Chunk.getChunkKey(location);

        if (!placedBlocks.containsKey(chunkKey)) return false;
        if (!placedBlocks.get(chunkKey).containsKey(location)) return false;
        return placedBlocks.get(chunkKey).get(location) == type;
    }

    @Override
    public Optional<CustomBlockType> get(Location location) {
        location = stripLocation(location);
        long chunkKey = Chunk.getChunkKey(location);
        if (!placedBlocks.containsKey(chunkKey)) return Optional.empty();

        HashMap<Location, CustomBlockType> blocks = placedBlocks.get(chunkKey);
        if (!blocks.containsKey(location)) return Optional.empty();

        return Optional.of(blocks.get(location));
    }

    private Location stripLocation(Location location) {
        location = location.toBlockLocation();
        return location.setRotation(0, 0);
    }
}
