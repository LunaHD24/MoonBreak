package dev.lunaa.moonbreak.tool;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public record CustomToolTypeImpl(
        Component name,
        List<Component> lore,
        Material material,
        int maxDurability,
        int miningLevel,
        float speed,
        Collection<Material> correctToolFor,
        boolean includeVanillaMineables,
        boolean overwriteVanillaMineables,
        boolean affectedByWrongTool,
        boolean affectedByUnderwater,
        boolean affectedByFloating,
        Map<EventHook<? extends Event>, BiConsumer<? extends Event, CustomTool>> eventHooks
) implements CustomToolType {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> Optional<BiConsumer<T, CustomTool>> hook(EventHook<T> eventHook) {
        return Optional.ofNullable((BiConsumer<T, CustomTool>) eventHooks.get(eventHook));
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void executeHook(EventHook<T> eventHook, T event, CustomTool tool) {
        if (!eventHooks.containsKey(eventHook)) return;
        ((BiConsumer<T, CustomTool>)eventHooks.get(eventHook)).accept(event, tool);
    }

    public static class BuilderImpl implements CustomToolType.Builder {
        private @Nullable Component name;
        private List<Component> lore = new ArrayList<>();
        private @Nullable Material material;
        private int maxDurability = -1;
        private int miningLevel = 0;
        private float speed = -1f;
        private Collection<Material> correctToolFor = new ArrayList<>();
        private boolean includeVanillaMineables = true;
        private boolean overwriteVanillaMineables = true;
        private boolean affectedByWrongTool = true;
        private boolean affectedByUnderwater = true;
        private boolean affectedByFloating = true;
        private HashMap<EventHook<? extends Event>, BiConsumer<? extends Event, CustomTool>> eventHooks = new HashMap<>();

        @Override
        public Builder copyFrom(CustomToolType type) {
            name = type.name();
            lore = new ArrayList<>(type.lore());
            material = type.material();
            maxDurability = type.maxDurability();
            miningLevel = type.miningLevel();
            speed = type.speed();
            correctToolFor = new ArrayList<>(type.correctToolFor());
            includeVanillaMineables = type.includeVanillaMineables();
            affectedByWrongTool = type.affectedByWrongTool();
            affectedByUnderwater = type.affectedByUnderwater();
            affectedByFloating = type.affectedByFloating();
            eventHooks = new HashMap<>(type.eventHooks());
            return this;
        }

        @Override
        public Builder name(Component name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder lore(List<Component> lore) {
            this.lore.addAll(lore);
            return this;
        }

        @Override
        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        @Override
        public Builder maxDurability(int maxDurability) {
            this.maxDurability = maxDurability;
            return this;
        }

        @Override
        public Builder miningLevel(int miningLevel) {
            this.miningLevel = miningLevel;
            return this;
        }

        @Override
        public Builder miningLevel(MiningLevel miningLevel) {
            this.miningLevel = miningLevel.miningLevel();
            return this;
        }

        @Override
        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }

        @Override
        public Builder speed(MiningSpeed speed) {
            this.speed = speed.miningSpeed();
            return this;
        }

        @Override
        public Builder correctToolFor(Collection<Material> materials) {
            this.correctToolFor.addAll(materials);
            return this;
        }

        @Override
        public Builder includeVanillaMineables(boolean includeVanillaMineables) {
            this.includeVanillaMineables = includeVanillaMineables;
            return this;
        }

        @Override
        public Builder overwriteVanillaMineables(boolean overwriteVanillaMineables) {
            this.overwriteVanillaMineables = overwriteVanillaMineables;
            return this;
        }

        @Override
        public Builder affectedByWrongTool(boolean affected) {
            this.affectedByWrongTool = affected;
            return this;
        }

        @Override
        public Builder affectedUnderwater(boolean affected) {
            this.affectedByUnderwater = affected;
            return this;
        }

        @Override
        public Builder affectedByFloating(boolean affected) {
            this.affectedByFloating = affected;
            return this;
        }

        @Override
        public <T extends Event> Builder onHook(EventHook<T> eventHook, BiConsumer<T, CustomTool> hookAction) {
            eventHooks.put(eventHook, hookAction);
            return this;
        }

        @Override
        public CustomToolType build() {
            if (name == null) throw new IllegalArgumentException("Name cannot be null");
            if (material == null || material == Material.AIR || !material.isItem()) throw new IllegalArgumentException("Material cannot be null, AIR or non-item");
            if (maxDurability == -1) maxDurability = material.getMaxDurability();
            if (maxDurability < 1) throw new IllegalArgumentException("Max durability must be greater than 0");
            if (miningLevel < 0) throw new IllegalArgumentException("Mining level cannot be negative");
            if (speed < 0) {
                this.speed = defaultMiningSpeed(material).orElseThrow(() -> new IllegalArgumentException("Speed cannot be negative. Must be supplied if the material does not have a default value."));
            }

            return new CustomToolTypeImpl(
                    name,
                    Collections.unmodifiableList(lore),
                    material,
                    maxDurability,
                    miningLevel,
                    speed,
                    Collections.unmodifiableCollection(correctToolFor),
                    includeVanillaMineables,
                    overwriteVanillaMineables,
                    affectedByWrongTool,
                    affectedByUnderwater,
                    affectedByFloating,
                    Collections.unmodifiableMap(eventHooks)
            );
        }

        private Optional<Float> defaultMiningSpeed(Material material) {
            if (!material.isItem()) return Optional.empty();
            Tool tool = material.getDefaultData(DataComponentTypes.TOOL);
            if (tool == null) return Optional.empty();
            return Optional.of(tool.defaultMiningSpeed());
        }
    }
}
