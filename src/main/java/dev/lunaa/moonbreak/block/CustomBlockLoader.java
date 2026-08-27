package dev.lunaa.moonbreak.block;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.registry.BuiltinRegistries;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CustomBlockLoader {

    private static final Path CHUNK_INDEX_PATH = MoonBreak.instance().getDataPath().resolve("chunk_index.dat");
    private static final String BLOCK_NOT_REGISTERED = "Block type not registered";
    private static final String CHUNK_INDEX_SAVE_FAILED = "Could not save chunk index. Altered chunks since last save will not be recognized when clearing! Retrying in 5 seconds...";
    private static final NamespacedKey BLOCKS_KEY = new NamespacedKey(MoonBreak.instance(), "blocks");

    private final ConcurrentHashMap<Long, String> chunkIndex = new ConcurrentHashMap<>();
    private int chunkIndexSaveRetries = 1;
    private int chunkIndexLoadRetries = 2;
    private boolean deletionInProgress = false;

    private final CustomBlockManagerImpl blockManager;

    public CustomBlockLoader(CustomBlockManagerImpl blockManager) {
        this.blockManager = blockManager;
    }

    public void init() {
        loadChunkIndex();
    }

    public void saveChunkIndex() {
        if (deletionInProgress) return;

        List<String> lines = chunkIndex.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .toList();

        Bukkit.getAsyncScheduler().runNow(MoonBreak.instance(), (_) -> {
            try {
                Files.write(CHUNK_INDEX_PATH, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                chunkIndexSaveRetries = 2;
            } catch (IOException e) {
                MoonBreak.logger().log(Level.SEVERE, CHUNK_INDEX_SAVE_FAILED, e);
                if (chunkIndexSaveRetries > 0) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(MoonBreak.instance(), this::saveChunkIndex, 5 * 20);
                } else {
                    chunkIndexSaveRetries = 2;
                    return;
                }
                chunkIndexSaveRetries--;
            }
        });
    }

    private void loadChunkIndex() {
        Bukkit.getAsyncScheduler().runNow(MoonBreak.instance(), (_) -> {
            if (!Files.exists(CHUNK_INDEX_PATH)) {
                try {
                    Files.createDirectories(MoonBreak.instance().getDataPath());
                    Files.createFile(CHUNK_INDEX_PATH);
                } catch (IOException e) {
                    MoonBreak.logger().log(Level.WARNING, "Could not create index at " + CHUNK_INDEX_PATH, e);
                }
            }

            try (BufferedReader reader = Files.newBufferedReader(CHUNK_INDEX_PATH, Charset.defaultCharset())) {
                chunkIndex.clear();
                reader.lines().forEach(line -> {
                    if (line.isBlank()) return;
                    String[] split = line.split(":");
                    if (split.length != 2) return;
                    chunkIndex.put(Long.parseLong(split[0]), split[1]);
                });
                chunkIndexLoadRetries = 2;
            } catch (IOException e) {
                MoonBreak.logger().log(Level.SEVERE, "Could not load chunk index. Clearing chunk data will not work! Retrying in 5 seconds...");
                if (chunkIndexLoadRetries > 0) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(MoonBreak.instance(), this::loadChunkIndex, 5 * 20);
                } else {
                    MoonBreak.logger().severe("Failed loading chunk index. Disabling plugin.");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(MoonBreak.instance(), MoonBreak::disablePlugin);
                }
                chunkIndexLoadRetries--;
            } catch (NumberFormatException e) {
                MoonBreak.logger().severe("Cannot load chunk index due to invalid number at line " + chunkIndex.size() + ". Disabling plugin.");
                Bukkit.getScheduler().scheduleSyncDelayedTask(MoonBreak.instance(), MoonBreak::disablePlugin);
            }
        });
    }

    public void saveAllBlocks() {
        if (deletionInProgress) return;

        HashMap<Long, String> copyOfChunkIndex = new HashMap<>(chunkIndex);
        Set<Chunk> chunks = new HashSet<>();

        for (Map.Entry<Long, HashMap<Location, CustomBlockType>> entry : blockManager.getPlacedBlocks().entrySet()) {
            HashMap<Location, CustomBlockType> blocks = entry.getValue();
            if (blocks.isEmpty()) continue;

            World world = blocks.keySet().iterator().next().getWorld();
            if (world == null) {
                Long chunkKey = entry.getKey();
                int chunkX = chunkKey.intValue();
                int chunkZ = (int) (chunkKey >> 32);

                MoonBreak.logger().warning("World could not be found. Chunk at x:" + chunkX + ", z:" + chunkZ + " was not saved.");
                continue;
            }

            chunks.add(world.getChunkAt(entry.getKey()));
        }

        copyOfChunkIndex.forEach((key, value) -> {
            int chunkX = key.intValue();
            int chunkZ = (int) (key >> 32);
            World world = Bukkit.getWorld(value);

            if (world == null) {
                MoonBreak.logger().warning("World could not be found. Chunk at x:" + chunkX + ", z:" + chunkZ + " was not saved.");
                return;
            }

            if (world.isChunkLoaded(chunkX, chunkZ)) chunks.add(world.getChunkAt(chunkX, chunkZ));
        });

        chunks.forEach(this::saveChunk);
        saveChunkIndex();
    }

    public void saveChunk(Chunk chunk) {
        long chunkKey = chunk.getChunkKey();
        HashMap<Location, CustomBlockType> blocks = blockManager.getPlacedBlocks().get(chunkKey);
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();

        if (blocks == null || blocks.isEmpty()) {
            pdc.remove(BLOCKS_KEY);
            chunkIndex.remove(chunk.getChunkKey());
            return;
        }

        List<String> encodedStrings = blocks.entrySet().stream()
                .map(entry -> {
                    Location location = entry.getKey().toBlockLocation();
                    Optional<Key> optionalKey = BuiltinRegistries.BLOCK_TYPE.getKey(entry.getValue());
                    if (optionalKey.isEmpty()) throw new IllegalStateException(BLOCK_NOT_REGISTERED);

                    return location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ() + ";" + optionalKey.get().asString();
                })
                .toList();

        pdc.set(BLOCKS_KEY, PersistentDataType.LIST.strings(), encodedStrings);
        chunkIndex.putIfAbsent(chunkKey, chunk.getWorld().getName());
    }

    public void unloadChunk(Chunk chunk) {
        saveChunk(chunk);
        blockManager.getPlacedBlocks().remove(chunk.getChunkKey());
    }

    public void loadChunk(Chunk chunk) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(MoonBreak.instance(), () -> {
            long chunkKey = chunk.getChunkKey();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            if (!pdc.has(BLOCKS_KEY)) return;
            if (!chunkIndex.containsKey(chunkKey)) {
                pdc.remove(BLOCKS_KEY);
                return;
            }

            List<String> encodedStrings = pdc.get(BLOCKS_KEY, PersistentDataType.LIST.strings());
            if (encodedStrings == null || encodedStrings.isEmpty()) return;

            for (String encodedString : encodedStrings) {
                String[] split = encodedString.split(";");

                Location location = new Location(chunk.getWorld(), Integer.parseInt(split[0]),Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                Optional<CustomBlockType> optionalBlockType = BuiltinRegistries.BLOCK_TYPE.getEntry(Key.key(split[3]));
                if (optionalBlockType.isEmpty()) throw new IllegalStateException(BLOCK_NOT_REGISTERED);
                CustomBlockType customBlockType = optionalBlockType.get();

                blockManager.place(location, customBlockType);
            }
        });
    }

    public void wipeAllBlockFromExistence() {
        deletionInProgress = true;

        blockManager.getPlacedBlocks().clear();

        chunkIndex.forEach((chunkKey, worldName) -> {
            World world = Bukkit.getWorld(worldName);
            if (world == null) return;

            int chunkX = chunkKey.intValue();
            int chunkZ = (int) (chunkKey >> 32);
            if (!world.isChunkLoaded(chunkX, chunkZ)) return;

            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            chunk.getPersistentDataContainer().remove(BLOCKS_KEY);
        });

        chunkIndex.clear();
        deletionInProgress = false;
        saveChunkIndex();
    }

}
