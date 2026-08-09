package dev.lunaa.moonbreak.registry;

import dev.lunaa.moonbreak.MoonBreakApi;
import dev.lunaa.moonbreak.block.CustomBlockType;
import dev.lunaa.moonbreak.tool.CustomToolType;

/**
 * A collection of all accessible registries
 */
public final class BuiltinRegistries {

    public static final ResourceRegistry<CustomBlockType> BLOCK_TYPE = MoonBreakApi.resourceRegistry();
    public static final ResourceRegistry<CustomToolType> TOOL_TYPE = MoonBreakApi.resourceRegistry();

}
