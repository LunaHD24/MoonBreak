package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.CustomBlock;
import dev.lunaa.moonbreak.block.CustomBlockManager;
import dev.lunaa.moonbreak.block.CustomBlockType;
import dev.lunaa.moonbreak.registry.ResourceRegistry;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * Provides necessary data from the plugin to the API. Usage of any fields or methods is highly discouraged.
 */
@ApiStatus.Internal
public interface InternalProvider {

    ResourceRegistry newResourceRegistry();

    CustomToolType.Builder toolTypeBuilder();

    CustomTool toolOfType(CustomToolType type);

    Optional<CustomTool> toolFromItem(ItemStack item);

    CustomBlockType customBlockTypeFactory(Material material, float hardness);

    CustomBlockType customBlockTypeFactory(Material material, Material copyHardness);

    CustomBlock blockOfType(CustomBlockType type);

    CustomBlock blockOfType(CustomBlockType type, Location location);

    CustomBlockManager blockManager();
}
