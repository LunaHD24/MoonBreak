package dev.lunaa.moonbreak.persistence;

import dev.lunaa.moonbreak.block.storage.ChunkBlockEntry;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataType;

public class MoonBreakPersistentDataTypes {

    public static final ChunkBlockEntryDataType CHUNK_BLOCK_ENTRY = new ChunkBlockEntryDataType();
    public static final ListPersistentDataType<byte[], ChunkBlockEntry> CHUNK_BLOCK_ENTRY_LIST = PersistentDataType.LIST.listTypeFrom(CHUNK_BLOCK_ENTRY);

}
