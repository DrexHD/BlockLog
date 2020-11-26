package me.drex.logblock.database.entry;

import me.drex.logblock.database.entry.util.CacheEntry;

public class BlockTagEntry extends CacheEntry<byte[]> {

    public BlockTagEntry(byte[] value, int id) {
        super(value, id);
    }

}
