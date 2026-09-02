package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.CustomBlockLoader;
import dev.lunaa.moonbreak.block.CustomBlockManagerImpl;
import dev.lunaa.moonbreak.listener.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.logging.Logger;

public final class MoonBreak extends JavaPlugin {

    private static @MonotonicNonNull MoonBreak instance;
    private @MonotonicNonNull static Logger logger;
    private @MonotonicNonNull InternalProviderImpl internalProvider;
    private @MonotonicNonNull CustomBlockManagerImpl blockManager;
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
        getServer().getOnlinePlayers().forEach(BreakingService::removeBreakSpeedModifier);
        blockLoader.saveAllBlocks();
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

        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new PlayerMoveListener(), this);
        pm.registerEvents(new PlayerBreakBlockListener(), this);
        pm.registerEvents(new PlayerInteractListener(), this);
        pm.registerEvents(new PlayerInteractAtEntityListener(), this);

        pm.registerEvents(new BlockDamageListener(), this);
        pm.registerEvents(new BlockBreakProgressUpdateListener(), this);
        pm.registerEvents(new BlockDamageAbortListener(), this);

        pm.registerEvents(new EntityDamageByEntityListener(), this);

        pm.registerEvents(new ChunkLoadListener(), this);
        pm.registerEvents(new ChunkUnloadListener(), this);

        pm.registerEvents(new ServerTickStartListener(), this);

        pm.registerEvents(new CustomBlockChangeListeners(), this);
    }

    private void initializeFields() {
        internalProvider = new InternalProviderImpl();
        blockManager = new CustomBlockManagerImpl();
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

    public CustomBlockManagerImpl blockManager() {
        return blockManager;
    }

    public CustomBlockLoader blockLoader() {
        return blockLoader;
    }

    public BreakingService breakingService() {
        return breakingService;
    }
}
