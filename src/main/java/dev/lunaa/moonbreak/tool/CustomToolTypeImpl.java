package dev.lunaa.moonbreak.tool;

import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
        private float speed = 0f;
        private Collection<Material> correctToolFor = new ArrayList<>();
        private boolean includeVanillaMineables = true;
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
        public Builder onBlockDamage(BiConsumer<BlockDamageEvent, CustomTool> eventHook) {
            eventHooks.put(EventHook.BLOCK_DAMAGE, eventHook);
            return this;
        }

        @Override
        public Builder onBlockDamageUpdate(BiConsumer<BlockBreakProgressUpdateEvent, CustomTool> eventHook) {
            eventHooks.put(EventHook.BLOCK_DAMAGE_UPDATE, eventHook);
            return this;
        }

        @Override
        public Builder onBlockDamageAbort(BiConsumer<BlockDamageAbortEvent, CustomTool> eventHook) {
            eventHooks.put(EventHook.BLOCK_DAMAGE_ABORT, eventHook);
            return this;
        }

        @Override
        public Builder onPreBlockBreak(BiConsumer<BlockBreakEvent, CustomTool> eventHook) {
            eventHooks.put(EventHook.PRE_BLOCK_BREAK, eventHook);
            return this;
        }

        @Override
        public Builder onPostBlockBreak(BiConsumer<BlockBreakEvent, CustomTool> eventHook) {
            eventHooks.put(EventHook.POST_BLOCK_BREAK, eventHook);
            return this;
        }

        @Override
        public Builder onInteract(BiConsumer<PlayerInteractEvent, CustomTool> eventHook) {
            eventHooks.put(EventHook.INTERACT, eventHook);
            return this;
        }

        @Override
        public Builder onInteractAtEntity(BiConsumer<PlayerInteractAtEntityEvent, CustomTool> eventHook) {
            eventHooks.put(EventHook.INTERACT_AT_ENTITY, eventHook);
            return this;
        }

        @Override
        public Builder onEntityDamageByEntity(BiConsumer<EntityDamageByEntityEvent, CustomTool> eventHook) {
            eventHooks.put(EventHook.ENTITY_DAMAGE_BY_ENTITY, eventHook);
            return this;
        }

        @Override
        public CustomToolType build() {
            if (name == null) throw new IllegalArgumentException("Name cannot be null");
            if (material == null || material == Material.AIR) throw new IllegalArgumentException("Material cannot be null or AIR");
            if (maxDurability == -1) maxDurability = material.getMaxDurability();
            if (maxDurability < 1) throw new IllegalArgumentException("Max durability must be greater than 0");
            if (miningLevel < 0) throw new IllegalArgumentException("Mining level cannot be negative");
            if (speed < 0) throw new IllegalArgumentException("Speed cannot be negative");
            return new CustomToolTypeImpl(
                    name,
                    Collections.unmodifiableList(lore),
                    material,
                    maxDurability,
                    miningLevel,
                    speed,
                    Collections.unmodifiableCollection(correctToolFor),
                    includeVanillaMineables,
                    affectedByWrongTool,
                    affectedByUnderwater,
                    affectedByFloating,
                    Collections.unmodifiableMap(eventHooks)
            );
        }
    }
}
