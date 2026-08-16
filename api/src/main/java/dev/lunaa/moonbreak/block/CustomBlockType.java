package dev.lunaa.moonbreak.block;

import dev.lunaa.moonbreak.MoonBreakApi;
import dev.lunaa.moonbreak.registry.Registrable;
import org.bukkit.Material;

/**
 * Represents a BlockType which holds information about the hardness and placing material of said block
 */
public interface CustomBlockType extends Registrable {

    /**
     * Defines a BlockType consisting of a placing material and a block hardness
     * @param material the placement material
     * @param hardness the block's hardness
     * @return the BlockType
     */
    static CustomBlockType of(Material material, float hardness) {
        return MoonBreakApi.provider().customBlockTypeFactory(material, hardness);
    }

    /**
     * Defines a BlockType consisting of a placing material and a block hardness copied from another block
     * @param material the placement material
     * @param copyHardness the material to copy the hardness from
     * @return the BlockType
     */
    static CustomBlockType of(Material material, Material copyHardness) {
        return MoonBreakApi.provider().customBlockTypeFactory(material, copyHardness);
    }

    /**
     * Returns the block type's material
     * @return the material
     */
    Material material();

    /**
     * Returns the block type's hardness
     * @return the hardness
     */
    float hardness();
}
