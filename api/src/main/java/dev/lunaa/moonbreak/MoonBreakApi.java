package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.registry.Registrable;
import dev.lunaa.moonbreak.registry.ResourceRegistry;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;

/**
 * Central entrypoint for the MoonBreak API to work. Usage of any fields or methods is highly discouraged.
 */
@ApiStatus.Internal
public final class MoonBreakApi {

    private static final MoonBreakApi instance = new MoonBreakApi();
    protected @MonotonicNonNull InternalProvider provider;

    private MoonBreakApi() {}

    protected static MoonBreakApi instance() {
        return instance;
    }

    public static InternalProvider provider() {
        return instance().provider;
    }

    public static <T extends Registrable> ResourceRegistry<T> newResourceRegistry() {
        return (ResourceRegistry<T>) provider().newResourceRegistry();
    }
}
