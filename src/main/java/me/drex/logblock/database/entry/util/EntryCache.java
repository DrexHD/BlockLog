package me.drex.logblock.database.entry.util;


import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EntryCache {

    public static final HashMap<Class<? extends CacheEntry<?>>, HashMap<Integer, CacheEntry<?>>> cache = new HashMap<>();

    //TODO: Load all cache entries from databse on server start

    @Nullable
    public static CacheEntry<?> get(Class<? extends CacheEntry<?>> clazz, int id) {
        HashMap<Integer, ? extends CacheEntry<?>> entries = cache.get(clazz);
        entries = entries == null ? new HashMap<>() : entries;
        return entries.get(id);
    }

    @Nullable
    public static <K> CacheEntry<?> get(Class<? extends CacheEntry<K>> clazz, K value) {
        HashMap<Integer, ? extends CacheEntry<?>> entries = cache.get(clazz);
        for (Map.Entry<Integer, ? extends CacheEntry<?>> entry : entries.entrySet()) {
            if (entry.getValue().getValue().equals(value)) return entry.getValue();
        }
        return null;
    }

    public static void add(Class<? extends CacheEntry<?>> clazz, int id, CacheEntry<?> entry) {
        HashMap<Integer, CacheEntry<?>> map = cache.get(clazz);
        map = map == null ? new HashMap<>() : map;
        map.put(id, entry);
        cache.put(clazz, map);
    }

    public static HashMap<Integer, CacheEntry<?>> get(Class<? extends CacheEntry<?>> clazz) {
        HashMap<Integer, CacheEntry<?>> map = cache.get(clazz);
        map = map == null ? new HashMap<>() : map;
        return map;
    }

    public static String asString() {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<Class<? extends CacheEntry<?>>, HashMap<Integer, CacheEntry<?>>> entry : cache.entrySet()) {
            s.append("\n").append(entry.getKey().getSimpleName()).append(": ").append(entry.getValue().toString());
        }
        return s.toString();
    }

}
