package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.*;
import dev.lunaa.moonbreak.registry.ResourceRegistry;
import dev.lunaa.moonbreak.registry.ResourceRegistryImpl;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolImpl;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class InternalProviderImpl implements InternalProvider {

    @Override
    public ResourceRegistry newResourceRegistry() {
        return new ResourceRegistryImpl<>();
    }

    @Override
    public CustomToolType.Builder toolTypeBuilder() {
        return new CustomToolTypeImpl.BuilderImpl();
    }

    @Override
    public CustomTool toolOfType(CustomToolType type) {
        return new CustomToolImpl(type);
    }

    @Override
    public Optional<CustomTool> toolFromPlayer(Player player) {
        return CustomToolImpl.fromPlayer(player);
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
            throw new IllegalArgumentException("Material is not valid: " + copyHardness.name() + ". Must be a solid, non-instaminable block.");
        return new CustomBlockTypeImpl(material, copyHardness.getHardness());
    }

    @Override
    public CustomBlockManager blockManager() {
        return MoonBreak.instance().blockManager();
    }

}