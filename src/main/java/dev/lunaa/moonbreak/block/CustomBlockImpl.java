package dev.lunaa.moonbreak.block;

import dev.lunaa.moonbreak.MoonBreak;
import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class CustomBlockImpl implements CustomBlock {

    private CustomBlockType type;
    private @Nullable Location actualLocation;
    private @Nullable Location newLocation;
    private boolean isPlaced = false;

    public CustomBlockImpl(CustomBlockType type) {
        this.type = type;
    }

    public CustomBlockImpl(CustomBlockType type, Location location) {
        this(type);
        this.actualLocation = location.clone();
        this.newLocation = actualLocation.clone();
    }

    protected void setPlaced(boolean placed) {
        this.isPlaced = placed;
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
        return newLocation == null ? Optional.empty() : Optional.of(newLocation.clone());
    }

    @Override
    public void location(Location location) {
        this.newLocation = location.clone();
        update();
    }

    @Override
    public boolean isPlaced() {
        return isPlaced;
    }

    @Override
    public void placeInWorld() {
        if (actualLocation == null) throw new IllegalStateException("Location cannot be null");
        MoonBreak.instance().blockManager().place(this);
        isPlaced = true;
    }

    @Override
    public void removeFromWorld(boolean setAir) {
        if (!isPlaced) return;
        MoonBreak.instance().blockManager().removeIfPlaced(this, setAir);
    }

    @Override
    public void update() {
        if (!isPlaced) {
            if (newLocation != null) actualLocation = newLocation.clone();
            return;
        }
        if (newLocation != null) actualLocation = newLocation.clone();
        MoonBreak.instance().blockManager().remove(Objects.requireNonNull(actualLocation).clone(),  true);
        placeInWorld();
    }
}
