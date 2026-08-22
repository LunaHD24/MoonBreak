package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.CustomBlockLoader;
import dev.lunaa.moonbreak.block.CustomBlockManagerImpl;
import dev.lunaa.moonbreak.listener.*;
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

    public void previousBaseBlockBreakSpeed(Player player, double blockBreakSpeed) {
        previousBaseBlockBreakSpeeds.put(player.getUniqueId(), blockBreakSpeed);
    }

    public double previousBaseBlockBreakSpeed(Player player) {
        return previousBaseBlockBreakSpeeds.get(player.getUniqueId());
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
