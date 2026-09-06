package dev.lunaa.moonbreak.block.storage;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.block.WorldChunkKey;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class ChunkIndexStorage {

    private static final String SAVE_FAILED = "Could not save chunk index. Altered chunks since last save will not be recognized when clearing!";
    private static final String LOAD_FAILED = "Could not load chunk index. Disabling plugin.";

    private final Set<WorldChunkKey> worldChunkKeys = new HashSet<>();
    private final Path filePath;

    public ChunkIndexStorage(Path filePath) {
        this.filePath = filePath;
    }

    public boolean ensureFilePathExists() {
        if (Files.exists(filePath.getParent())) return true;
        try {
            Files.createDirectories(filePath.getParent());
            return true;
        } catch (IOException e) {
            MoonBreak.logger().log(Level.WARNING, SAVE_FAILED, e);
            return false;
        }
    }

    public void clear() {
        worldChunkKeys.clear();
    }

    public void addChunk(WorldChunkKey worldChunkKey) {
        worldChunkKeys.add(worldChunkKey);
    }

    public void removeChunk(WorldChunkKey worldChunkKey) {
        worldChunkKeys.remove(worldChunkKey);
    }

    public Set<WorldChunkKey> chunksWithBlocks() {
        return new HashSet<>(worldChunkKeys);
    }

    public void saveSync(Set<WorldChunkKey> snapshot) {
        if (!ensureFilePathExists()) return;

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filePath.toFile()))) {
            for (WorldChunkKey worldChunkKey : snapshot) {
                out.writeLong(worldChunkKey.worldId().getMostSignificantBits());
                out.writeLong(worldChunkKey.worldId().getLeastSignificantBits());
                out.writeLong(worldChunkKey.chunkKey());
            }
        } catch (IOException e) {
            MoonBreak.logger().log(Level.WARNING, SAVE_FAILED, e);
        }
    }

    public void saveSync() {
        saveSync(new HashSet<>(worldChunkKeys));
    }

    public void saveAsync() {
        HashSet<WorldChunkKey> snapshot = new HashSet<>(worldChunkKeys);
        Bukkit.getAsyncScheduler().runNow(MoonBreak.instance(), _ -> saveSync(snapshot));
    }

    public void loadSync() {
        worldChunkKeys.clear();
        if (!Files.exists(filePath)) return;

        byte[] bytes;
        try (FileInputStream in = new FileInputStream(filePath.toFile())) {
            bytes = in.readAllBytes();
        } catch (IOException e) {
            MoonBreak.logger().log(Level.SEVERE, LOAD_FAILED, e);
            Bukkit.getScheduler().scheduleSyncDelayedTask(MoonBreak.instance(), MoonBreak::disablePlugin);
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        while (buffer.remaining() >= 24) {
            UUID worldId = new UUID(buffer.getLong(), buffer.getLong());
            long chunkKey = buffer.getLong();

            World world = Bukkit.getWorld(worldId);
            if (world == null) {
                MoonBreak.logger().warning("Tried loading chunk index. Could not find world with id " + worldId + " - skipping");
                continue;
            }

            worldChunkKeys.add(new WorldChunkKey(worldId, chunkKey));
        }
    }

    public void loadAsync() {
        Bukkit.getAsyncScheduler().runNow(MoonBreak.instance(), _ -> loadSync());
    }

}
