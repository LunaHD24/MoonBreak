package dev.lunaa.moonbreak.block;

import dev.lunaa.moonbreak.MoonBreakApi;
import dev.lunaa.moonbreak.registry.Registrable;
import dev.lunaa.moonbreak.tool.CustomTool;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.function.BiConsumer;

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

    interface Builder {

        Builder material(Material material);

        Builder hardness(float hardness);

        Builder onBlockDamage(BiConsumer<BlockDamageEvent, CustomTool> action);

        Builder onBlockDamageUpdate(BiConsumer<BlockBreakProgressUpdateEvent, CustomTool> action);

        Builder onBlockDamageAbort(BiConsumer<BlockDamageAbortEvent, CustomTool> action);

        Builder onPreBlockBreak(BiConsumer<BlockBreakEvent, CustomTool> action);

        Builder onPostBlockBreak(BiConsumer<BlockBreakEvent, CustomTool> action);

        Builder onInteract(BiConsumer<PlayerInteractEvent, CustomTool> action);

        Builder onInteractAtEntity(BiConsumer<PlayerInteractAtEntityEvent, CustomTool> action);

        Builder onEntityDamage(BiConsumer<EntityDamageByEntityEvent, CustomTool> action);

        CustomBlockType build();
    }
}
