package dev.lunaa.moonbreak.block.storage;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.block.*;
import dev.lunaa.moonbreak.persistence.MoonBreakPersistentDataTypes;
import dev.lunaa.moonbreak.registry.BuiltinRegistries;
import net.kyori.adventure.key.Key;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class CustomBlockLoader {

    private static final String BLOCK_TYPE_NOT_REGISTERED = "Block type not registered";
    private static final NamespacedKey BLOCKS_KEY = new NamespacedKey(MoonBreak.instance(), "blocks");

    private final CustomBlockManagerImpl blockManager;
    private final ChunkIndexStorage chunkIndexStorage;
    private boolean wiping = false;

    public CustomBlockLoader(CustomBlockManagerImpl blockManager, ChunkIndexStorage chunkIndexStorage) {
        this.blockManager = blockManager;
        this.chunkIndexStorage = chunkIndexStorage;
    }

    public void setWiping(boolean wiping) {
        this.wiping = wiping;
    }

    public void wipe(@Nullable Player player) {
        new ThrottledWipeTask(this, chunkIndexStorage, player).start();
    }

    public void wipeSavedChunkData(Chunk chunk) {
        blockManager.clearChunkData(chunk);
        chunk.getPersistentDataContainer().remove(BLOCKS_KEY);
        chunkIndexStorage.removeChunk(WorldChunkKey.from(chunk));
    }

    public void saveChunk(Chunk chunk) {
        if (wiping) return;

        Optional<HashMap<ChunkBlockKey, CustomBlockType>> optionalBlocks = blockManager.blocksInChunk(chunk);
        if (optionalBlocks.isEmpty() || optionalBlocks.get().isEmpty()) {
            wipeSavedChunkData(chunk);
            return;
        }
        HashMap<ChunkBlockKey, CustomBlockType> blocks = optionalBlocks.get();

        ArrayList<ChunkBlockEntry> entries = new ArrayList<>(blocks.size());
        for (Map.Entry<ChunkBlockKey, CustomBlockType> entry : blocks.entrySet()) {
            Optional<Key> optionalBlockKey = BuiltinRegistries.BLOCK_TYPE.getKey(entry.getValue());
            if (optionalBlockKey.isEmpty()) {
                MoonBreak.logger().warning("Tried saving chunk data entry. " + BLOCK_TYPE_NOT_REGISTERED + " - skipping");
                continue;
            }

            ChunkBlockKey chunkBlockKey = entry.getKey();
            entries.add(new ChunkBlockEntry(chunkBlockKey.x(), chunkBlockKey.y(), chunkBlockKey.z(), optionalBlockKey.get()));
        }

        if (entries.isEmpty()) {
            wipeSavedChunkData(chunk);
            return;
        }

        chunkIndexStorage.addChunk(WorldChunkKey.from(chunk));
        chunk.getPersistentDataContainer().set(BLOCKS_KEY, MoonBreakPersistentDataTypes.CHUNK_BLOCK_ENTRY_LIST, entries);
    }

    public void unloadChunk(Chunk chunk) {
        if (wiping) return;
        saveChunk(chunk);
        blockManager.clearChunkData(chunk);
    }

    public void loadChunk(Chunk chunk) {
        List<ChunkBlockEntry> entries = chunk.getPersistentDataContainer().get(BLOCKS_KEY, MoonBreakPersistentDataTypes.CHUNK_BLOCK_ENTRY_LIST);
        if (entries == null || entries.isEmpty()) return;

        HashMap<ChunkBlockKey, CustomBlockType> blocks = new HashMap<>();
        for (ChunkBlockEntry entry : entries) {
            Optional<CustomBlockType> optionalBlockType = BuiltinRegistries.BLOCK_TYPE.getEntry(entry.blockTypeKey());
            if (optionalBlockType.isEmpty()) {
                MoonBreak.logger().warning("Tried loading chunk data entry. " + BLOCK_TYPE_NOT_REGISTERED + " - skipping");
                continue;
            }

            ChunkBlockKey chunkBlockKey = new ChunkBlockKey(entry.x(),  entry.y(), entry.z());
            blocks.put(chunkBlockKey, optionalBlockType.get());
        }
        if (blocks.isEmpty()) return;

        chunkIndexStorage.addChunk(WorldChunkKey.from(chunk));
        blockManager.placeChunkBlocks(chunk, blocks);
    }

    public void saveAllBlocks(boolean saveIndexAsync) {
        if (wiping) return;

        blockManager.chunksWithBlocks().forEach(this::saveChunk);
        if (saveIndexAsync) {
            chunkIndexStorage.saveAsync();
        } else {
            chunkIndexStorage.saveSync();
        }
    }

}
