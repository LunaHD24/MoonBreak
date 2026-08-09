package dev.lunaa.moonbreak.block;

import org.bukkit.Material;

public record CustomBlockTypeImpl(Material material, float hardness) implements CustomBlockType {

    public CustomBlockTypeImpl {
        if (hardness <= 0) throw new IllegalArgumentException("Hardness must be greater than 0");
        if (!isValidMaterial(material)) {
            throw new IllegalArgumentException("Material is not valid: " + material.name() + ". Must be a solid, non-instaminable block.");
        }
    }

    public static boolean isValidMaterial(Material material) {
        return !material.isLegacy()
                && material.isBlock()
                && material.getHardness() > 0
                && material.isSolid();
    }

}
