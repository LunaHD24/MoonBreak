package dev.lunaa.moonbreak.registry;

import dev.lunaa.moonbreak.block.CustomBlockType;
import dev.lunaa.moonbreak.tool.CustomToolType;
import net.kyori.adventure.key.Key;

import java.util.Optional;

/**
 * Represents a registry used for registering resources like {@link CustomBlockType} and {@link CustomToolType}
 * @param <T> resource type to register
 */
public interface ResourceRegistry<T extends Registrable> {

    /**
     * Registers an entry with the specified key
     * @param key the key
     * @param entry the entry
     */
    void register(Key key, T entry);

    /**
     * Returns an entry of given key, if registered
     * @param key the key
     * @return the entry if registered
     */
    Optional<T> getEntry(Key key);

    /**
     * Returns a key of given entry, if registered
     * @param entry the entry
     * @return the key if registered
     */
    Optional<Key> getKey(T entry);
}
