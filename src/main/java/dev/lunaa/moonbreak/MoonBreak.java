package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.CustomBlockLoader;
import dev.lunaa.moonbreak.block.CustomBlockManager;
import dev.lunaa.moonbreak.listener.*;
import dev.lunaa.moonbreak.registry.Registrable;
import dev.lunaa.moonbreak.registry.ResourceRegistryImpl;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public final class MoonBreak extends JavaPlugin {

    private static final HashMap<UUID, Double> previousBaseBlockBreakSpeeds = new HashMap<>();

    private static @MonotonicNonNull MoonBreak instance;
    private @MonotonicNonNull static Logger logger;
    private @MonotonicNonNull ResourceRegistryImpl<Registrable> resourceRegistry;
    private @MonotonicNonNull InternalProviderImpl internalProvider;
    private @MonotonicNonNull CustomBlockManager blockManager;
    private @MonotonicNonNull CustomBlockLoader blockLoader;
    private @MonotonicNonNull BreakingService breakingService;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        registerEvents();
        initializeFields();
        initializeServices();
        initializeApi();
    }

    @Override
    public void onDisable() {
        previousBaseBlockBreakSpeeds.forEach((uuid, blockBreakSpeed) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).setBaseValue(blockBreakSpeed);
        });
    }

    public static MoonBreak instance() {
        return instance;
    }

    public static Logger logger() {
        return logger;
    }

    public static void disablePlugin() {
        instance.getServer().getPluginManager().disablePlugin(instance);
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new PlayerMoveListener(), this);
        pm.registerEvents(new PlayerBreakBlockListener(), this);

        pm.registerEvents(new ChunkLoadListener(), this);
        pm.registerEvents(new ChunkUnloadListener(), this);

        pm.registerEvents(new ServerTickStartListener(), this);

        pm.registerEvents(new CustomBlockChangeListeners(), this);
    }

    private void initializeFields() {
        resourceRegistry = new ResourceRegistryImpl<>();
        internalProvider = new InternalProviderImpl(resourceRegistry);
        blockManager = new CustomBlockManager();
        blockLoader = new CustomBlockLoader(blockManager);
        breakingService = new BreakingService();
    }

    private void initializeServices() {
        blockLoader.init();
        CustomBlockChangeListeners.init(blockManager);
    }

    private void initializeApi() {
        MoonBreakApi.instance().provider = internalProvider;
    }

    public void previousBaseBlockBreakSpeed(Player player, double blockBreakSpeed) {
        previousBaseBlockBreakSpeeds.put(player.getUniqueId(), blockBreakSpeed);
    }

    public double previousBaseBlockBreakSpeed(Player player) {
        return previousBaseBlockBreakSpeeds.get(player.getUniqueId());
    }

    public ResourceRegistryImpl<Registrable> resourceRegistry() {
        return resourceRegistry;
    }

    public CustomBlockManager blockManager() {
        return blockManager;
    }

    public CustomBlockLoader blockLoader() {
        return blockLoader;
    }

    public BreakingService breakingService() {
        return breakingService;
    }
}
