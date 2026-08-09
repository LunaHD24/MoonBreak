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
    private static final NamespacedKey BLOCKS_KEY = new NamespacedKey(MoonBreak.instance(), "blocks");

    private final ConcurrentHashMap<Long, String> chunkIndex = new ConcurrentHashMap<>();
    private int chunkIndexSaveRetries = 1;
    private int chunkIndexLoadRetries = 2;
    private boolean deletionInProgress = false;

    private final CustomBlockManager blockManager;

    public CustomBlockLoader(CustomBlockManager blockManager) {
        this.blockManager = blockManager;
    }

    public void init() {
        loadChunkIndex();
    }

    public void saveChunkIndex() {
        if (deletionInProgress) return;
        if (!Files.exists(CHUNK_INDEX_PATH)) loadChunkIndex();

        try {
            List<String> lines = chunkIndex.entrySet().stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .toList();

            Files.write(CHUNK_INDEX_PATH, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            MoonBreak.logger().log(Level.SEVERE, "Could not save chunk index. Altered chunks since last save will not be recognized when clearing! Retrying in 5 seconds...");
            if (chunkIndexSaveRetries > 0) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(MoonBreak.instance(), this::saveChunkIndex, 5 * 20);
            } else {
                chunkIndexSaveRetries = 2;
                return;
            }
            chunkIndexSaveRetries--;
        }
    }

    private void loadChunkIndex() {
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
                String[] split = line.split(":");
                chunkIndex.put(Long.parseLong(split[0]), split[1]);
            });
        } catch (IOException e) {
            MoonBreak.logger().log(Level.SEVERE, "Could not load chunk index. Clearing chunk data will not work! Retrying in 5 seconds...");
            if (chunkIndexLoadRetries > 0) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(MoonBreak.instance(), this::loadChunkIndex, 5 * 20);
            } else {
                MoonBreak.logger().severe("Failed loading chunk index. Disabling plugin.");
                MoonBreak.disablePlugin();
            }
            chunkIndexLoadRetries--;
        } catch (NumberFormatException e) {
            MoonBreak.logger().severe("Cannot load chunk index due to invalid number at line " + chunkIndex.size() + ". Disabling plugin.");
            MoonBreak.disablePlugin();
        }
    }

    public void saveAllBlocks() {
        if (deletionInProgress) return;

        HashMap<Long, String> copyOfChunkIndex = new HashMap<>(chunkIndex);
        Set<Chunk> chunks = new HashSet<>();

        for (Location location : blockManager.getPlacedBlocks().keySet()) {
            long chunkKey = location.getChunk().getChunkKey();
            chunks.add(location.getWorld().getChunkAt(chunkKey));
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
        List<Map.Entry<Location, CustomBlockType>> blocks = blockManager.getPlacedBlocks().entrySet().stream()
                .filter(entry -> {
                    Chunk locationChunk = entry.getKey().getChunk();
                    return chunk.getX() == locationChunk.getX() && chunk.getZ() == locationChunk.getZ();
                })
                .toList();

        PersistentDataContainer pdc = chunk.getPersistentDataContainer();

        if (blocks.isEmpty()) {
            pdc.remove(BLOCKS_KEY);
            chunkIndex.remove(chunk.getChunkKey());
            return;
        }

        List<String> encodedStrings = blocks.stream()
                .map(entry -> {
                    Location location = entry.getKey().toBlockLocation();
                    Optional<Key> optionalKey = BuiltinRegistries.BLOCK_TYPE.getKey(entry.getValue());
                    if (optionalKey.isEmpty()) throw new IllegalStateException(BLOCK_NOT_REGISTERED);

                    return location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ() + ";" + optionalKey.get().asString();
                })
                .toList();

        pdc.set(BLOCKS_KEY, PersistentDataType.LIST.strings(), encodedStrings);
        if (!chunkIndex.containsKey(chunk.getChunkKey())) chunkIndex.put(chunk.getChunkKey(), chunk.getWorld().getName());
    }
    public void unloadChunk(Chunk chunk) {
        saveChunk(chunk);

        blockManager.getPlacedBlocks().entrySet().removeIf(entry -> {
            Chunk locationChunk = entry.getKey().getChunk();
            return chunk.getX() == locationChunk.getX() && chunk.getZ() == locationChunk.getZ();
        });
    }

    public void loadChunk(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        if (!pdc.has(BLOCKS_KEY)) return;
        if (!chunkIndex.containsKey(chunk.getChunkKey())) {
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

            if (location.getBlock().getType() != customBlockType.material()) {
                blockManager.place(new CustomBlockImpl(customBlockType, location));
            }
            blockManager.getPlacedBlocks().put(location, customBlockType);
        }
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
