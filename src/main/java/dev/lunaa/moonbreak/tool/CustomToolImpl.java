package dev.lunaa.moonbreak.tool;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.item.ItemProperty;
import dev.lunaa.moonbreak.registry.BuiltinRegistries;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class CustomToolImpl implements CustomTool {

    private static final ItemProperty<Integer> DURABILITY = ItemProperty.integer(Key.key(MoonBreak.instance(), "durability"));

    private final Key id;
    private final CustomToolType type;
    private int durability;
    private boolean unbreakable;
    private @MonotonicNonNull ItemStack item;
    private boolean isDirty = true;

    public CustomToolImpl(CustomToolType type) {
        this.type = type;
        this.durability = type.maxDurability();
        this.unbreakable = false;

        Optional<Key> optionalId = MoonBreak.instance().resourceRegistry().getKey(type);
        optionalId.orElseThrow(() -> new IllegalStateException("Tool type not registered"));
        id = optionalId.get();
    }

    public static Optional<CustomTool> from(ItemStack item) {
        Optional<Key> itemId = ItemProperty.ITEM_ID.get(item);
        if (itemId.isEmpty()) return Optional.empty();

        Optional<CustomToolType> optionalType = BuiltinRegistries.TOOL_TYPE.getEntry(itemId.get());
        if (optionalType.isEmpty()) return Optional.empty();

        CustomToolImpl tool = new CustomToolImpl(optionalType.get());
        tool.durability(DURABILITY.get(item).orElseThrow(() -> new IllegalStateException("Tool does not have durability value")));

        return Optional.of(tool);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack itemStack() {
        if (broken()) return ItemStack.empty();
        if (!isDirty) return item.clone();

        ItemStack item = new ItemStack(type.material());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(type.name());
        meta.lore(type.lore());
        meta.setUnbreakable(unbreakable);

        if (meta instanceof Damageable damageable) {
            short maxDurability = item.getType().getMaxDurability();
            double sizedDamage = (double) maxDurability / type.maxDurability();
            int durability = (int) Math.round(sizedDamage*this.durability);
            damageable.setDamage(maxDurability - durability);
        }

        meta.setTool(getToolComponent(item.getType(), meta));
        item.setItemMeta(meta);
        ItemProperty.ITEM_ID.set(item, id);
        DURABILITY.set(item, durability);

        this.item = item;
        isDirty = false;
        return item.clone();
    }

    @SuppressWarnings("UnstableApiUsage")
    private ToolComponent getToolComponent(Material material, ItemMeta meta) {
        ToolComponent toolComponent = meta.getTool();
        toolComponent.setRules(Collections.emptyList());

        if (!type.affectedByWrongTool()) {
            toolComponent.addRule(Arrays.stream(Material.values())
                    .filter(mat -> !mat.isLegacy())
                    .filter(Material::isBlock)
                    .toList(), type.speed(), true);
            return toolComponent;
        }

        toolComponent.addRule(type.correctToolFor(), type.speed(), true);

        switch (type.miningLevel()) {
            case 0 -> {
                toolComponent.addRule(Tag.INCORRECT_FOR_GOLD_TOOL, type.speed(), false);
                toolComponent.addRule(Tag.INCORRECT_FOR_WOODEN_TOOL, type.speed(), false);
            }
            case 1 -> {
                toolComponent.addRule(Tag.INCORRECT_FOR_COPPER_TOOL, type.speed(), false);
                toolComponent.addRule(Tag.INCORRECT_FOR_STONE_TOOL, type.speed(), false);
                toolComponent.addRule(Tag.NEEDS_STONE_TOOL, type.speed(), true);
            }
            case 2 -> {
                toolComponent.addRule(Tag.INCORRECT_FOR_IRON_TOOL, type.speed(), false);
                toolComponent.addRule(Tag.NEEDS_IRON_TOOL, type.speed(), true);
            }
            case 3 -> {
                toolComponent.addRule(Tag.INCORRECT_FOR_DIAMOND_TOOL, type.speed(), false);
                toolComponent.addRule(Tag.NEEDS_DIAMOND_TOOL, type.speed(), true);
            }
            case 4 -> {
                toolComponent.addRule(Tag.INCORRECT_FOR_NETHERITE_TOOL, type.speed(), false);
                toolComponent.addRule(Tag.NEEDS_DIAMOND_TOOL, type.speed(), true);
            }
        }

        if (!type.includeVanillaMineables()) return toolComponent;

        Optional<VanillaTool> optionalVanillaTool = Arrays.stream(VanillaTool.values())
                .filter(vanillaTool -> material.name().endsWith(vanillaTool.name()))
                .findFirst();
        optionalVanillaTool.ifPresent(vanillaTool -> toolComponent.addRule(
                Objects.requireNonNull(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("mineable/" + vanillaTool.name().toLowerCase()), Material.class)),
                type.speed(),
                true
        ));

        return toolComponent;
    }

    @Override
    public CustomToolType type() {
        return type;
    }

    @Override
    public int durability() {
        return durability;
    }

    @Override
    public void durability(int durability) {
        this.durability = durability;
        isDirty = true;
    }

    @Override
    public boolean unbreakable() {
        return unbreakable;
    }

    @Override
    public void unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        isDirty = true;
    }

    private enum VanillaTool {
        PICKAXE,
        AXE,
        SHOVEL,
        HOE
    }
}