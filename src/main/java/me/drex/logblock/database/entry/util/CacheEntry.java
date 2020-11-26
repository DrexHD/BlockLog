package me.drex.logblock.database.entry.util;

import me.drex.logblock.BlockLog;
import me.drex.logblock.database.entry.*;
import me.drex.logblock.util.Constants;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

    private K value;
    private int id;

    public CacheEntry(K value, int id) {
        this.value = value;
        this.id = id;
    }

    static <L> void load(Class<? extends CacheEntry<L>> clazz, int id, L type, Consumer<CacheEntry<L>> consumer) {
      //System.out.println("load(Class<? extends CacheEntry<L>> clazz, int id, L type)");
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = BlockLog.getConnection().prepareStatement("SELECT " + Constants.CacheColumn.VALUE + " FROM " + classToTable.get(clazz) + " WHERE id = ?");
                statement.setInt(1, id);
                ResultSet resultSet = statement.executeQuery();
                //System.out.println("Checking rs");
                if (resultSet.next()) {
                    //System.out.println("hasNext");
                    L value = null;
                    if (type instanceof String) {
                        value = (L) resultSet.getString(Constants.CacheColumn.VALUE.toString());
                    } else if (type instanceof byte[]) {
                        value = (L) resultSet.getBytes(Constants.CacheColumn.VALUE.toString());
                    }
                    //System.out.println("Value: " + value);
                    CacheEntry<L> cacheEntry = (CacheEntry<L>) clazz.getDeclaredConstructors()[0].newInstance(value, id);
                    //System.out.println("CacheEntry: " + cacheEntry);
                    EntryCache.add(clazz, id, cacheEntry);
                    //System.out.println("CacheEntry2: " + cacheEntry);
                    consumer.accept(cacheEntry);
                } else {
                    //System.out.println(" empty for " + id);
                }
            } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException throwables) {
                throwables.printStackTrace();
            }
            throw new RuntimeException("Error loading entry with id " + id);
        });
    }

    static <L> void load(Class<? extends CacheEntry<L>> clazz, L val, Consumer<CacheEntry<L>> consumer) {
        CompletableFuture.runAsync(() -> {
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
                    //System.out.println(clazz.getSimpleName() + " found entry with id " + id);
                    EntryCache.add(clazz, id, cacheEntry);
                    cacheEntry.updateID(id);
                    consumer.accept(cacheEntry);
                } else {
                    cacheEntry.saveAsync(integer -> {
                        //System.out.println(clazz.getSimpleName() + " create entry with id " + integer);
                        cacheEntry.updateID(integer);
                        consumer.accept(cacheEntry);
                    });
                }
            } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public static <L> void of(Class<? extends CacheEntry<L>> clazz, int id, L type, Consumer<CacheEntry<L>> consumer) {
      //System.out.println("of(Class<? extends CacheEntry<L>> clazz, int id, L type)");
        CacheEntry<L> entry = (CacheEntry<L>) EntryCache.get(clazz, id);
      //System.out.println("Entry: " + entry);
        if (entry != null) consumer.accept(entry);
        load(clazz, id, type, consumer);
    }

    public static <L> void of(Class<? extends CacheEntry<L>> clazz, L value, Consumer<CacheEntry<L>> consumer) {
        for (Map.Entry<Integer, CacheEntry<?>> entry : EntryCache.get(clazz).entrySet()) {
            if (entry.getValue().getValue().equals(value)) consumer.accept((CacheEntry<L>) entry.getValue());
        }
        load(clazz, value, consumer);
    }

    public K getValue() {
        return value;
    }

    public String getTable() {
        return classToTable.get(this.getClass());
    }

    public void updateID(int id) {
      //System.out.println("New id of " + this.getClass().getSimpleName() + " is " + id);
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    @Override
    public void saveAsync(Consumer<Integer> action) {
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
                  //System.out.println("Saving " + this.getClass().getSimpleName() + " Entry with id " + rs.getInt(1));
                    action.accept(rs.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


}
