package dev.lunaa.moonbreak.block;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Optional;

public class CustomBlockManager {

    private final HashMap<Long, HashMap<Location, CustomBlockType>> placedBlocks = new HashMap<>();

    public HashMap<Long, HashMap<Location, CustomBlockType>> getPlacedBlocks() {
        return placedBlocks;
    }

    public void place(CustomBlockImpl block) {
        Location location = stripLocation(block.location().orElseThrow());
        long chunkKey = location.getChunk().getChunkKey();
        HashMap<Location, CustomBlockType> blocks = placedBlocks.get(chunkKey);

        if (blocks.containsKey(location)) {
            throw new IllegalStateException("Tried placing block at an occupied location: " + location);
        }

        location.getBlock().setType(block.type().material());
        blocks.put(location, block.type());
    }

    public void remove(Location location, boolean setAir) {
        location = stripLocation(location);
        if (!isPlaced(location)) return;
        if (setAir) location.getBlock().setType(Material.AIR);

        long chunkKey = location.getChunk().getChunkKey();
        placedBlocks.get(chunkKey).remove(location);
    }

    public boolean isPlaced(Location location) {
        location = stripLocation(location);
        long chunkKey = location.getChunk().getChunkKey();
        return placedBlocks.get(chunkKey).containsKey(location);
    }

    public Optional<CustomBlock> getBlock(Location location) {
        location = stripLocation(location);
        HashMap<Location, CustomBlockType> blocks = placedBlocks.get(location.getChunk().getChunkKey());
        if (blocks.containsKey(location)) return Optional.empty();
        return Optional.of(new CustomBlockImpl(blocks.get(location)));
    }

    private Location stripLocation(Location location) {
        location = location.toBlockLocation();
        return location.setRotation(0, 0);
    }
}
