package dev.lunaa.moonbreak.item;

import dev.lunaa.moonbreak.MoonBreak;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.function.Function;

public final class ItemProperty<T> {

    public static final ItemProperty<Key> ITEM_ID = key(Key.key(MoonBreak.instance(), "item_id"));

    private final NamespacedKey key;
    private final Function<T, String> parseFrom;
    private final Function<String, T> parseTo;

    public ItemProperty(Key key, Function<T, String> parseFrom, Function<String, T> parseTo) {
        this.key = new NamespacedKey(key.namespace(), key.value());
        this.parseTo = parseTo;
        this.parseFrom = parseFrom;
    }

    public static ItemProperty<String> string(Key key) {
        return new ItemProperty<>(key, value -> value, value -> value);
    }

    public static ItemProperty<Integer> integer(Key key) {
        return new ItemProperty<>(key, Object::toString, Integer::parseInt);
    }

    public static ItemProperty<Key> key(Key key) {
        return new ItemProperty<>(key, Key::asString, Key::key);
    }

    public void set(ItemMeta meta, T value) {
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, parseFrom.apply(value));
    }

    public void set(ItemStack item, T value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, parseFrom.apply(value));
        item.setItemMeta(meta);
    }

    public Optional<T> get(ItemMeta meta) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String value = pdc.get(key, PersistentDataType.STRING);

        if (value == null) return Optional.empty();
        return Optional.of(parseTo.apply(value));
    }

    public Optional<T> get(ItemStack item) {
        PersistentDataContainerView pdc = item.getPersistentDataContainer();
        String value = pdc.get(key, PersistentDataType.STRING);

        if (value == null) return Optional.empty();
        return Optional.of(parseTo.apply(value));
    }

    public boolean has(ItemMeta meta) {
        return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    public boolean has(ItemStack item) {
        return item.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

}