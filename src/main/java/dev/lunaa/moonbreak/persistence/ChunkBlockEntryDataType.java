package dev.lunaa.moonbreak.persistence;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.block.storage.ChunkBlockEntry;
import net.kyori.adventure.key.Key;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import java.nio.ByteBuffer;

public final class ChunkBlockEntryDataType implements PersistentDataType<byte[], ChunkBlockEntry> {

    protected ChunkBlockEntryDataType() {}

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<ChunkBlockEntry> getComplexType() {
        return ChunkBlockEntry.class;
    }

    @Override
    public byte[] toPrimitive(ChunkBlockEntry complex, PersistentDataAdapterContext context) {
        byte[] keyStringBytes = complex.blockTypeKey().asString().getBytes(MoonBreak.DEFAULT_CHARSET);

        ByteBuffer buffer = ByteBuffer.allocate(4 + keyStringBytes.length);
        buffer.put(complex.x())
                .putShort(complex.y())
                .put(complex.z())
                .put(keyStringBytes);

        return buffer.array();
    }

    @Override
    public ChunkBlockEntry fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
        ByteBuffer buffer = ByteBuffer.wrap(primitive);

        byte x = buffer.get();
        short y = buffer.getShort();
        byte z = buffer.get();
        byte[] keyStringBytes = new byte[buffer.remaining()];

        buffer.get(keyStringBytes, 0, keyStringBytes.length);
        String keyString = new String(keyStringBytes, MoonBreak.DEFAULT_CHARSET);

        return new ChunkBlockEntry(x, y, z, Key.key(keyString));
    }
}
