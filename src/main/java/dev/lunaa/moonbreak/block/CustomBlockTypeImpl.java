package dev.lunaa.moonbreak.block;

import org.bukkit.Material;

import java.util.Set;

public record CustomBlockTypeImpl(Material material, float hardness) implements CustomBlockType {

    private static final Set<Material> ILLEGAL_MATERIALS = Set.of(
            Material.DRAGON_EGG
    );

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
                && material.isSolid()
                && !ILLEGAL_MATERIALS.contains(material);
    }

}
