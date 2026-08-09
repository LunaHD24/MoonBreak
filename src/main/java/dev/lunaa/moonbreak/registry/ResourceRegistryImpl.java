package dev.lunaa.moonbreak.registry;

import net.kyori.adventure.key.Key;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Optional;

public final class ResourceRegistryImpl<T extends Registrable> implements ResourceRegistry<T> {

    private final HashMap<Key, T> keyToValue = new HashMap<>();
    private final IdentityHashMap<T, Key> valueToKey = new IdentityHashMap<>();

    @Override
    public void register(Key key, T entry) {
        keyToValue.put(key, entry);
        valueToKey.put(entry, key);
    }

    @Override
    public Optional<T> getEntry(Key key) {
        return Optional.ofNullable(keyToValue.get(key));
    }

    @Override
    public Optional<Key> getKey(T entry) {
        return Optional.ofNullable(valueToKey.get(entry));
    }
}