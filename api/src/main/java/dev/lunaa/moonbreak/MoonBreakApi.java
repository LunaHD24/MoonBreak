package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.CustomBlockType;
import dev.lunaa.moonbreak.registry.BuiltinRegistries;
import dev.lunaa.moonbreak.registry.Registrable;
import dev.lunaa.moonbreak.registry.ResourceRegistry;
import dev.lunaa.moonbreak.tool.CustomToolType;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;

/**
 * Central entrypoint for the MoonBreak API to work. Usage of any fields or methods is discouraged.
 */
public final class MoonBreakApi {

    private static final MoonBreakApi instance = new MoonBreakApi();
    protected @MonotonicNonNull InternalProvider provider;

    private MoonBreakApi() {}

    @ApiStatus.Internal
    protected static MoonBreakApi instance() {
        return instance;
    }

    @ApiStatus.Internal
    public static InternalProvider provider() {
        return instance().provider;
    }

    /**
     * Usage is discouraged, use {@link BuiltinRegistries} instead.<br>
     * Returns the ResourceRegistry which can be used to register {@link CustomBlockType}s and {@link CustomToolType}s
     * @return the ResourceRegistry
     * @param <T> resource type to register
     */
    public static <T extends Registrable> ResourceRegistry<T> resourceRegistry() {
        return (ResourceRegistry<T>) provider().resourceRegistry();
    }
}
