package dev.lunaa.moonbreak.block;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomBlockManager {

    private final HashMap<Location, CustomBlockType> placedBlocks = new HashMap<>();

    public Map<Location, CustomBlockType> getPlacedBlocks() {
        return placedBlocks;
    }

    public void place(CustomBlockImpl block) {
        Location location = stripLocation(block.location().orElseThrow());
        if (placedBlocks.containsKey(location)) {
            throw new IllegalStateException("Tried placing block at an occupied location: " + location);
        }

        location.getBlock().setType(block.type().material());
        placedBlocks.put(location, block.type());
    }

    public void remove(Location location, boolean setAir) {
        location = stripLocation(location);
        if (!isPlaced(location)) return;
        if (setAir) location.getBlock().setType(Material.AIR);
        placedBlocks.remove(location);
    }

    public boolean isPlaced(Location location) {
        location = stripLocation(location);
        return placedBlocks.containsKey(location);
    }

    public Optional<CustomBlock> getBlock(Location location) {
        location = stripLocation(location);
        if (!placedBlocks.containsKey(location)) return Optional.empty();
        return Optional.of(new CustomBlockImpl(placedBlocks.get(location)));
    }

    private Location stripLocation(Location location) {
        location = location.toBlockLocation();
        return location.setRotation(0, 0);
    }
}
