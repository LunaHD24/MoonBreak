package dev.lunaa.moonbreak.block;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Optional;

public class CustomBlockManagerImpl implements CustomBlockManager{

    private final HashMap<Long, HashMap<Location, CustomBlockType>> placedBlocks = new HashMap<>();

    public HashMap<Long, HashMap<Location, CustomBlockType>> getPlacedBlocks() {
        return placedBlocks;
    }

    @Override
    public void place(CustomBlock block) {
        Location location = stripLocation(block.location().orElseThrow(() -> new IllegalStateException("Location cannot be null")));
        long chunkKey = location.getChunk().getChunkKey();
        HashMap<Location, CustomBlockType> blocks = placedBlocks.get(chunkKey);

        if (blocks.containsKey(location)) {
            throw new IllegalStateException("Tried placing block at an occupied location: " + location);
        }

        location.getBlock().setType(block.type().material());
        blocks.put(location, block.type());

        CustomBlockImpl impl = (CustomBlockImpl) block;
        impl.setPlaced(true);
    }

    @Override
    public void remove(Location location, boolean setAir) {
        location = stripLocation(location);
        if (!isPlaced(location)) return;
        if (setAir) location.getBlock().setType(Material.AIR);

        long chunkKey = location.getChunk().getChunkKey();
        CustomBlockImpl impl = (CustomBlockImpl) placedBlocks.get(chunkKey).get(location);
        if (impl == null) return;
        impl.setPlaced(false);
    }

    @Override
    public boolean removeIfPlaced(CustomBlock block, boolean setAir) {
        if (!block.isPlaced()) return false;
        remove(block.location().orElseThrow(), setAir);
        return true;
    }

    @Override
    public boolean isPlaced(Location location) {
        location = stripLocation(location);
        long chunkKey = location.getChunk().getChunkKey();
        return placedBlocks.get(chunkKey).containsKey(location);
    }

    @Override
    public boolean isPlaced(Location location, CustomBlockType type) {
        location = stripLocation(location);
        long chunkKey = location.getChunk().getChunkKey();
        if (!placedBlocks.get(chunkKey).containsKey(location)) return false;
        return placedBlocks.get(chunkKey).get(location) == type;
    }

    @Override
    public Optional<CustomBlock> get(Location location) {
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
