package me.drex.logblock.database.entry;

import me.drex.logblock.database.entry.util.CacheEntry;

public class EntityEntry extends CacheEntry<String> {

    public EntityEntry(String value, int id) {
        super(value, id);
    }

}
