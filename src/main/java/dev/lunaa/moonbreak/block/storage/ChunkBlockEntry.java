package dev.lunaa.moonbreak.block.storage;

import net.kyori.adventure.key.Key;

public record ChunkBlockEntry(byte x, short y, byte z, Key blockTypeKey) {}