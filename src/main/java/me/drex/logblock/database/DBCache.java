package me.drex.logblock.database;

import me.drex.logblock.util.BlockUtil;
import me.drex.logblock.util.HistoryColumn;
import me.drex.logblock.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class DBCache {

    public static int wait = 500;
    public static int statements = 0;
    public static boolean running = false;
    public static StopWatch timeRunning = new StopWatch();
    public static StopWatch timeNotRunning = new StopWatch();
    public static StopWatch timeSpentRunning = StopWatch.createStarted();
    public static StopWatch timeSpentNotRunning = StopWatch.createStarted();
    public static int timesRun = 1;
    private final Connection connection;
    private final PreparedStatement insert;
    private final PreparedStatement cachedUndos;
    HashMap<Integer, String> blockCache = new HashMap<>();
    HashMap<Integer, String> entityCache = new HashMap<>();
    HashMap<Integer, String> dimensionCache = new HashMap<>();
    HashMap<Integer, String> blockstateCache = new HashMap<>();
    private Logger logger = Logger.getLogger("SBL");


    public DBCache(Connection connection) throws SQLException {
        this.connection = connection;
        this.insert = connection.prepareStatement("INSERT INTO history (entityid, x, y, z, dimensionid, blockid, pblockid, time, placed, undone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        this.cachedUndos = connection.prepareStatement("UPDATE history SET undone = ? WHERE id = ?");
//        loadEntities();
//        loadBlocks();
//        loadBlockStates();
//        loadDimensions();
        timeSpentRunning.suspend();
//        startThread();
    }

    private void loadEntities() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM entities");
        while (resultSet.next()) {
            entityCache.put(resultSet.getInt("id"), resultSet.getString("value"));
        }
    }

    private void loadBlocks() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM blocks");
        while (resultSet.next()) {
            blockCache.put(resultSet.getInt("id"), resultSet.getString("value"));
        }
    }

    private void loadBlockStates() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM blockstates");
        while (resultSet.next()) {
            blockstateCache.put(resultSet.getInt("id"), resultSet.getString("value"));
        }
    }

    private void loadDimensions() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM dimensions");
        while (resultSet.next()) {
            dimensionCache.put(resultSet.getInt("id"), resultSet.getString("value"));
        }
    }

/*    public void startThread() {
        CompletableFuture.runAsync(() -> {
            Thread.currentThread().setName("SQL Executor");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(wait);
                    if (statements > 0) {
                        timeNotRunning.reset();
                        timeSpentNotRunning.suspend();
                        timeSpentRunning.resume();
                        timeRunning.start();
                        running = true;
                        modifyEntries(null, null, null, null, null,0, false);
                        running = false;
                        timeNotRunning.start();
                        timeRunning.reset();
                        statements = 0;
                        timesRun++;
                        timeSpentNotRunning.resume();
                        timeSpentRunning.suspend();
                    }
                } catch (InterruptedException | SQLException ex) {
                    ex.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
          //System.out.println("Thread stopped!");
        });
    }*/

    @Nullable
    public String getDimension(int id) throws SQLException {
        if (dimensionCache.containsKey(id)) {
            return dimensionCache.get(id);
        }
        return getFromDatabase("dimensions", id);
    }

    public int getOrCreateDimension(String value) throws SQLException {
        if (dimensionCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : dimensionCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getOrCreateFromDatabase("dimensions", value);
    }

    public int getDimension(String value) throws SQLException {
        if (dimensionCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : dimensionCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getFromDatabase("dimensions", value);
    }

    @Nullable
    public String getBlock(int id) throws SQLException {
        if (blockCache.containsKey(id)) {
            return blockCache.get(id);
        }
        return getFromDatabase("blocks", id);
    }

    public int getOrCreateBlock(String value) throws SQLException {
        if (blockCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : blockCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getOrCreateFromDatabase("blocks", value);
    }

    public int getBlock(String value) throws SQLException {
        if (blockCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : blockCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getFromDatabase("blocks", value);
    }

    @Nullable
    public String getBlockState(int id) throws SQLException {
        if (blockstateCache.containsKey(id)) {
            return blockstateCache.get(id);
        }
        return getFromDatabase("blockstates", id);
    }

    public int getOrCreateBlockState(String value) throws SQLException {
        if (blockstateCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : blockstateCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getOrCreateFromDatabase("blockstates", value);
    }

    public int getBlockState(String value) throws SQLException {
        if (blockstateCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : blockstateCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getFromDatabase("blockstates", value);
    }

    @Nullable
    public String getEntity(int id) throws SQLException {
        if (entityCache.containsKey(id)) {
            return entityCache.get(id);
        }
        return getFromDatabase("entities", id);
    }

    public int getOrCreateEntity(String value) throws SQLException {
        if (entityCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : entityCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getOrCreateFromDatabase("entities", value);
    }

    public int getEntity(String value) throws SQLException {
        if (entityCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : entityCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getFromDatabase("entities", value);
    }

    @Nullable
    private String getFromDatabase(String table, int id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT value FROM " + table + " WHERE id = ?");
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next())
            return resultSet.getString("value");
        return null;
    }

    /**
     * @param table of the value
     * @param value to search
     * @return id of entry, 0 if the entry doesnt exist
     */
    private int getFromDatabase(String table, String value) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT id FROM " + table + " WHERE value = ?");
        statement.setString(1, value);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("id");
        } else {
            return 0;
        }
    }

    private int getOrCreateFromDatabase(String table, String value) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT id FROM " + table + " WHERE value = ?");
        statement.setString(1, value);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("id");
        } else {
            addToDatabase(table, value);
            return getOrCreateFromDatabase(table, value);
        }
    }

    private void addToDatabase(String table, String value) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table + " (value)" + " VALUES (?)");
        statement.setString(1, value);
        statement.executeUpdate();
    }

    /*This method ensures that cachedStatements is only accessed and changed by one thread at a time*/
    public synchronized void modifyEntries(String uuid, BlockPos pos, DimensionType dimensionType, String block, String pblock, long time, boolean placed) throws SQLException {
        if (uuid == null) {
            insert.executeBatch();
        } else {
            insert.setInt(1, getOrCreateEntity(uuid));
            insert.setInt(2, pos.getX());
            insert.setInt(3, pos.getY());
            insert.setInt(4, pos.getZ());
            insert.setInt(5, getOrCreateDimension(WorldUtil.getDimensionNameSpace(dimensionType)));
            insert.setInt(6, getOrCreateBlock(block));
            insert.setInt(7, getOrCreateBlock(pblock));
            insert.setLong(8, time);
            insert.setBoolean(9, placed);
            insert.setBoolean(10, false);
            insert.addBatch();
        }
    }

    /*This method ensures that cachedStatements is only accessed and changed by one thread at a time*/
    private synchronized void modifyUndos(int id, boolean undone) throws SQLException {
        if (id == 0) {
            cachedUndos.executeBatch();
        } else {
            cachedUndos.setBoolean(1, undone);
            cachedUndos.setInt(2, id);
            cachedUndos.addBatch();
        }
    }

    public void addEntry(String uuid, BlockPos pos, DimensionType dimensionType, String block, String pblock, long time, boolean placed) {
        statements++;
//        CompletableFuture.runAsync(() -> {
        try {
            modifyEntries(uuid, pos, dimensionType, block, pblock, time, placed);
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        });
    }

    public void addEntryAsync(String uuid, BlockPos pos, DimensionType dimensionType, BlockState block, BlockState pblock, long time, boolean placed) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement insert = connection.prepareStatement("INSERT INTO history (" + HistoryColumn.ENTITYID + ", " + HistoryColumn.XPOS + ", " + HistoryColumn.YPOS + ", " + HistoryColumn.ZPOS + ", " + HistoryColumn.DIMENSIONID + ", " + HistoryColumn.BLOCKID + ", " + HistoryColumn.PBLOCKID + ", " + HistoryColumn.BLOCKSTATEID + ", " + HistoryColumn.PBLOCKSTATEID + ", " + HistoryColumn.BLOCKTAGID + ", " + HistoryColumn.PBLOCKTAGID + ", " + HistoryColumn.TIME + ", " + HistoryColumn.PLACED + ", " + HistoryColumn.UNDONE + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                insert.setInt(1, getOrCreateEntity(uuid));
                insert.setInt(2, pos.getX());
                insert.setInt(3, pos.getY());
                insert.setInt(4, pos.getZ());
                insert.setInt(5, getOrCreateDimension(WorldUtil.getDimensionNameSpace(dimensionType)));
                insert.setInt(6, getOrCreateBlock(BlockUtil.toNameSpace(block.getBlock())));
                insert.setInt(7, getOrCreateBlockState(BlockUtil.toJsonString(block)));
                insert.setInt(8, getOrCreateBlock(BlockUtil.toNameSpace(pblock.getBlock())));
                insert.setInt(9, getOrCreateBlockState(BlockUtil.toJsonString(pblock)));
                insert.setLong(10, time);
                insert.setBoolean(11, placed);
                insert.setBoolean(12, false);
                insert.addBatch();
                insert.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addUndo(int id, boolean value) {
        statements++;
        CompletableFuture.runAsync(() -> {
            try {
                modifyUndos(id, value);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

}
