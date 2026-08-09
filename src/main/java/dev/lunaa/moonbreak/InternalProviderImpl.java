package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.CustomBlock;
import dev.lunaa.moonbreak.block.CustomBlockImpl;
import dev.lunaa.moonbreak.block.CustomBlockType;
import dev.lunaa.moonbreak.block.CustomBlockTypeImpl;
import dev.lunaa.moonbreak.registry.ResourceRegistry;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolImpl;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public record InternalProviderImpl(ResourceRegistry resourceRegistry) implements InternalProvider {

    @Override
    public CustomToolType.Builder toolTypeBuilder() {
        return new CustomToolTypeImpl.BuilderImpl();
    }

    @Override
    public CustomTool toolOfType(CustomToolType type) {
        return new CustomToolImpl(type);
    }

    @Override
    public Optional<CustomTool> toolFromItem(ItemStack item) {
        return CustomToolImpl.from(item);
    }

    @Override
    public CustomBlockType customBlockTypeFactory(Material material, float hardness) {
        return new CustomBlockTypeImpl(material, hardness);
    }

    @Override
    public CustomBlockType customBlockTypeFactory(Material material, Material copyHardness) {
        if (!CustomBlockTypeImpl.isValidMaterial(copyHardness))
            throw new IllegalArgumentException("Material is not valid: " + material.name() + ". Must be a solid, non-instaminable block.");
        return new CustomBlockTypeImpl(material, copyHardness.getHardness());
    }

    @Override
    public CustomBlock blockOfType(CustomBlockType type) {
        return new CustomBlockImpl(type);
    }

    @Override
    public CustomBlock blockOfType(CustomBlockType type, Location location) {
        return new CustomBlockImpl(type, location);
    }

}