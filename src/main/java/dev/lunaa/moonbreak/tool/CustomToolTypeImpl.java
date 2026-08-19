package dev.lunaa.moonbreak.tool;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        boolean affectedByFloating
) implements CustomToolType {

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
                    affectedByFloating
            );
        }
    }
}
