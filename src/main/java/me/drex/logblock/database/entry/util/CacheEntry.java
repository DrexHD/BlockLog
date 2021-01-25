package me.drex.logblock.database.entry.util;

import me.drex.logblock.BlockLog;
import me.drex.logblock.database.entry.*;
import me.drex.logblock.util.Constants;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public abstract class CacheEntry<K> implements IEntry {

/*    public static Map<Class<? extends CacheEntry<?>>, String> classToTable = Map.ofEntries(
            Map.entry(BlockStateEntry.class, "blockstate"),
            Map.entry(BlockTagEntry.class, "blocktag"),
            Map.entry(DimensionEntry.class, "dimension"),
            Map.entry(EntityEntry.class, "entity")
    );*/

    static Map<Class<? extends CacheEntry<?>>, String> classToTable = new HashMap<Class<? extends CacheEntry<?>>, String>() {{
        put(BlockEntry.class, Constants.Table.BLOCKS.toString());
        put(BlockStateEntry.class, Constants.Table.BLOCKSTATES.toString());
        put(BlockTagEntry.class, Constants.Table.BLOCKTAGS.toString());
        put(DimensionEntry.class, Constants.Table.DIMENSIONS.toString());
        put(EntityEntry.class, Constants.Table.ENTITIES.toString());
    }};

    static ConcurrentLinkedDeque<Object> currentlyCaching = new ConcurrentLinkedDeque<>();

    private K value;
    private int id;

    public CacheEntry(K value, int id) {
        this.value = value;
        this.id = id;
    }

    static synchronized <L> void load(Class<? extends CacheEntry<L>> clazz, int id, L type, Consumer<CacheEntry<L>> consumer) {
        CompletableFuture.runAsync(() -> {
            Thread.currentThread().setName("BlockLog$LoadByID(" + classToTable.get(clazz) + " / " + id + ")");
            try {
                PreparedStatement statement = BlockLog.getConnection().prepareStatement("SELECT " + Constants.CacheColumn.VALUE + " FROM " + classToTable.get(clazz) + " WHERE id = ?");
                statement.setInt(1, id);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    L value = null;
                    if (type instanceof String) {
                        value = (L) resultSet.getString(Constants.CacheColumn.VALUE.toString());
                    } else if (type instanceof byte[]) {
                        value = (L) resultSet.getBytes(Constants.CacheColumn.VALUE.toString());
                    }
                    CacheEntry<L> cacheEntry = (CacheEntry<L>) clazz.getDeclaredConstructors()[0].newInstance(value, id);
                    EntryCache.add(clazz, id, cacheEntry);
                    consumer.accept(cacheEntry);
                } else {
                    throw new RuntimeException("Error loading entry with id " + id);
                }
            } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException throwables) {
                throwables.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
    }

    static synchronized <L> void load(Class<? extends CacheEntry<L>> clazz, L val, Consumer<CacheEntry<L>> consumer) {
        CompletableFuture.runAsync(() -> {
            Thread.currentThread().setName("BlockLog$LoadByValue(" + classToTable.get(clazz) + " / " + (val instanceof byte[] ? Arrays.toString((byte[]) val) : val) + ")");
            try {
                PreparedStatement statement = BlockLog.getConnection().prepareStatement("SELECT " + Constants.CacheColumn.ID + " FROM " + classToTable.get(clazz) + " WHERE " + Constants.CacheColumn.VALUE + " = ?");
                if (val instanceof String) {
                    statement.setString(1, (String) val);
                } else if (val instanceof byte[]) {
                    statement.setBytes(1, (byte[]) val);
                }
                ResultSet resultSet = statement.executeQuery();
                CacheEntry<L> cacheEntry = (CacheEntry<L>) clazz.getConstructors()[0].newInstance(val, 0);
                if (resultSet.next()) {
                    int id = resultSet.getInt(Constants.CacheColumn.ID.toString());
                    EntryCache.add(clazz, id, cacheEntry);
                    currentlyCaching.remove(val);
                    cacheEntry.updateID(id);
                    consumer.accept(cacheEntry);
                } else {
                    cacheEntry.saveAsync(integer -> {
                        cacheEntry.updateID(integer);
                        EntryCache.add(clazz, integer, cacheEntry);
                        currentlyCaching.remove(val);
                        consumer.accept(cacheEntry);
                    });
                }
            } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public static synchronized <L> void of(Class<? extends CacheEntry<L>> clazz, int id, L type, Consumer<CacheEntry<L>> consumer) {
        CacheEntry<L> entry = (CacheEntry<L>) EntryCache.get(clazz, id);
        if (entry != null) {
            consumer.accept(entry);
            return;
        }
        load(clazz, id, type, consumer);
    }

    public static synchronized <L> void of(Class<? extends CacheEntry<L>> clazz, L value, Consumer<CacheEntry<L>> consumer) {
        for (Map.Entry<Integer, CacheEntry<?>> entry : EntryCache.get(clazz).entrySet()) {
            if (entry.getValue().getValue().equals(value) || (entry.getValue().getValue() instanceof byte[] && value instanceof byte[] && Arrays.equals((byte[]) entry.getValue().getValue(), (byte[]) value))) {
                consumer.accept((CacheEntry<L>) entry.getValue());
                return;
            }
        }
        if (currentlyCaching.contains(value)) {
            try {
                Thread.currentThread().wait(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            of(clazz, value, consumer);
        } else {
            currentlyCaching.add(value);
            load(clazz, value, consumer);
        }
    }

    public K getValue() {
        return value;
    }

    public String getTable() {
        return classToTable.get(this.getClass());
    }

    public void updateID(int id) {
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    @Override
    public synchronized void saveAsync(Consumer<Integer> action) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement insert = BlockLog.getConnection().prepareStatement("INSERT INTO " + getTable() + " (" + Constants.CacheColumn.VALUE + ") VALUES (?);", Statement.RETURN_GENERATED_KEYS);
                if (this.getValue() instanceof String) {
                    insert.setString(1, (String) this.getValue());
                } else if (this.getValue() instanceof byte[]) {
                    insert.setBytes(1, (byte[]) this.getValue());
                }
                insert.addBatch();
                insert.executeUpdate();
                ResultSet rs = insert.getGeneratedKeys();
                if (rs.next()) {
                    action.accept(rs.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


}
