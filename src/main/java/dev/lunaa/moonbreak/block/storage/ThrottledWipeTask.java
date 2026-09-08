package dev.lunaa.moonbreak.block.storage;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.block.WorldChunkKey;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ThrottledWipeTask implements Runnable {

    private final CustomBlockLoader loader;
    private final ChunkIndexStorage indexStorage;
    private final Queue<WorldChunkKey> queue;
    private final @Nullable Player player;
    private final int totalChunks;
    private BukkitTask task;
    private BossBar bossBar;
    private final AtomicInteger processedChunks = new AtomicInteger(0);

    public ThrottledWipeTask(CustomBlockLoader loader, ChunkIndexStorage indexStorage, @Nullable Player player) {
        Set<WorldChunkKey> wipeKeys = indexStorage.chunksWithBlocks();

        this.loader = loader;
        this.indexStorage = indexStorage;
        this.queue = new ArrayDeque<>(wipeKeys);
        this.player = player;
        this.totalChunks = wipeKeys.size();
    }

    public void start() {
        if (totalChunks == 0) {
            MoonBreak.logger().info("No chunks to wipe");
            return;
        }

        loader.setWiping(true);
        this.task = Bukkit.getScheduler().runTaskTimer(MoonBreak.instance(), this, 0L, 1L);
        MoonBreak.logger().info("Started wiping all chunks");

        if (player == null) return;
        bossBar = BossBar.bossBar(constructBossBarName(false), barProgress(), BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
        player.showBossBar(bossBar);
    }

    @Override
    public void run() {
        if (processedChunks.get() >= totalChunks) {
            finish();
            return;
        }

        int chunksPerTick = Bukkit.getAverageTickTime() >= 30 ? 5 : 15;
        for (int i=0; i<chunksPerTick; i++) {
            if (queue.isEmpty()) break;

            WorldChunkKey worldChunkKey = queue.poll();
            World world = Bukkit.getWorld(worldChunkKey.worldId());
            if (world == null) {
                indexStorage.removeChunk(worldChunkKey);
                processedChunks.incrementAndGet();
                continue;
            }

            int chunkX = (int) worldChunkKey.chunkKey();
            int chunkZ = (int) (worldChunkKey.chunkKey() >>> 32);
            world.getChunkAtAsync(chunkX, chunkZ).thenAccept(chunk -> {
                loader.wipeSavedChunkData(chunk);
                processedChunks.incrementAndGet();

                if (!chunk.isEntitiesLoaded()) world.unloadChunkRequest(chunkX, chunkZ);
            });
        }

        if (player == null) return;
        bossBar.name(constructBossBarName(false));
        bossBar.progress(barProgress());
    }

    private void finish() {
        if (task != null) task.cancel();

        indexStorage.clear();
        indexStorage.saveAsync();
        loader.setWiping(false);

        MoonBreak.logger().info("Wiped all chunks successfully");

        if (player == null) return;
        bossBar.name(constructBossBarName(true));
        bossBar.progress(barProgress());
        Bukkit.getScheduler().scheduleSyncDelayedTask(MoonBreak.instance(), () -> player.hideBossBar(bossBar), 3 * 20);
    }

    private TextComponent constructBossBarName(boolean finished) {
        return Component.text("Wiping chunks: ", finished ? NamedTextColor.GREEN : NamedTextColor.YELLOW).append(
                Component.text(processedChunks.get(), finished ? NamedTextColor.GREEN : NamedTextColor.YELLOW),
                Component.text("/", NamedTextColor.GRAY),
                Component.text(totalChunks, NamedTextColor.GREEN)
        );
    }

    private float barProgress() {
        return (float) processedChunks.get() / totalChunks;
    }
}
