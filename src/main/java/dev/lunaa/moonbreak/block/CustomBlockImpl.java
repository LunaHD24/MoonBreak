package dev.lunaa.moonbreak.block;

import dev.lunaa.moonbreak.MoonBreak;
import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class CustomBlockImpl implements CustomBlock {

    private CustomBlockType type;
    private @Nullable Location location;
    private boolean isPlaced = false;

    public CustomBlockImpl(CustomBlockType type) {
        this.type = type;
    }

    public CustomBlockImpl(CustomBlockType type, Location location) {
        this(type);
        this.location = location.clone();
    }

    @Override
    public CustomBlockType type() {
        return type;
    }

    @Override
    public void type(CustomBlockType type) {
        this.type = type;
        update();
    }

    @Override
    public Optional<Location> location() {
        return location == null ? Optional.empty() : Optional.of(location.clone());
    }

    @Override
    public void location(Location location) {
        this.location = location.clone();
        update();
    }

    @Override
    public boolean isPlaced() {
        return isPlaced;
    }

    @Override
    public void placeInWorld() {
        if (location == null) throw new IllegalStateException("Location cannot be null");
        MoonBreak.instance().blockManager().place(this);
        isPlaced = true;
    }

    @Override
    public void update() {
        if (location == null) throw new IllegalStateException("Location cannot be null");
        if (!isPlaced) return;
        MoonBreak.instance().blockManager().remove(location, true);
        placeInWorld();
    }
}
