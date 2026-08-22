package dev.lunaa.moonbreak.block;

import dev.lunaa.moonbreak.MoonBreakApi;
import org.bukkit.Location;

import java.util.Optional;

public interface CustomBlockManager {

    static CustomBlockManager manager() {
        return MoonBreakApi.provider().blockManager();
    }

    void place(CustomBlock block);

    void remove(Location location, boolean setAir);

    boolean removeIfPlaced(CustomBlock block, boolean setAir);

    boolean isPlaced(Location location);

    boolean isPlaced(Location location, CustomBlockType type);

    Optional<CustomBlock> get(Location location);

}
