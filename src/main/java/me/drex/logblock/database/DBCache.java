package me.drex.logblock.database;

import me.drex.logblock.util.WorldUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DBCache {

    private final Connection connection;
    private final PreparedStatement cachedEntries;
    private final PreparedStatement cachedUndos;
    HashMap<Integer, String> blockCache = new HashMap<>();
    HashMap<Integer, String> entityCache = new HashMap<>();
    HashMap<Integer, String> dimensionCache = new HashMap<>();
    private int statements = 0;
    int wait = 500;

    public DBCache(Connection connection) throws SQLException {
        this.connection = connection;
        this.cachedEntries = connection.prepareStatement("INSERT INTO history (id, entityid, x, y, z, dimensionid, blockid, pblockid, time, placed, undone) VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        this.cachedUndos = connection.prepareStatement("UPDATE history SET undone = ? WHERE id = ?");
        loadBlocks();
        loadEntities();
        loadDimensions();
        startThread();
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

    private void loadDimensions() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM dimensions");
        while (resultSet.next()) {
            dimensionCache.put(resultSet.getInt("id"), resultSet.getString("value"));
        }
    }

    public void startThread() {
        /*TODO: Fix this (thread randomly stops + some stuff isn't logged*/
        CompletableFuture.runAsync(() -> {
            Thread.currentThread().setName("SQL Executor");
            while (!Thread.currentThread().isInterrupted()) {
                try {
/*                    LogBlockMod.server.getPlayerManager().getPlayerList().forEach(playerEntity -> {
                        playerEntity.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, new LiteralText("Working on " + statements + " entries!").formatted(Formatting.AQUA), -1, 50, -1));
                    });*/
                    Thread.sleep(wait);
                    if(statements > 0) {
                        System.out.println(statements);
                        modifyEntries(null, null, null, null, null, false);
                        modifyUndos(0, false);
                    }
                    statements = 0;
                } catch (InterruptedException | SQLException ex) {
                    ex.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public String getDimension(int id) throws SQLException {
        if (dimensionCache.containsKey(id)) {
            return dimensionCache.get(id);
        }
        return getFromDatabase("dimensions", id);
    }

    public int getDimension(String value) throws SQLException {
        if (dimensionCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : dimensionCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getFromDatabase("dimensions", value);
    }

    public String getBlock(int id) throws SQLException {
        if (blockCache.containsKey(id)) {
            return blockCache.get(id);
        }
        return getFromDatabase("blocks", id);
    }

    public int getBlock(String value) throws SQLException {
        if (blockCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : blockCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getFromDatabase("blocks", value);
    }

    public String getEntity(int id) throws SQLException {
        if (entityCache.containsKey(id)) {
            return entityCache.get(id);
        }
        return getFromDatabase("entities", id);
    }

    public int getEntity(String value) throws SQLException {
        if (entityCache.containsValue(value)) {
            for (Map.Entry<Integer, String> entry : entityCache.entrySet()) {
                if (entry.getValue().equals(value)) return entry.getKey();
            }
        }
        return getFromDatabase("entities", value);
    }

    private String getFromDatabase(String table, int id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE id = ?");
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.getString("value");
    }

    private int getFromDatabase(String table, String value) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE value = ?");
        statement.setString(1, value);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("id");
        } else {
            addToDatabase(table, value);
            return getFromDatabase(table, value);
        }
    }

    private void addToDatabase(String table, String value) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table + " (id, value)" + " VALUES (NULL, ?)");
        statement.setString(1, value);
        statement.executeUpdate();
    }

    /*This method ensures that cachedStatements is only accessed and changed by one thread at a time*/
    private synchronized void modifyEntries(String uuid, BlockPos pos, DimensionType dimensionType, String block, String pblock, boolean placed) throws SQLException {
        if (uuid == null) {
            cachedEntries.executeBatch();
        } else {
            cachedEntries.setInt(1, getEntity(uuid));
            cachedEntries.setInt(2, pos.getX());
            cachedEntries.setInt(3, pos.getY());
            cachedEntries.setInt(4, pos.getZ());
            cachedEntries.setInt(5, getDimension(WorldUtil.getDimensionNameWithNameSpace(dimensionType)));
            cachedEntries.setInt(6, getBlock(block));
            cachedEntries.setInt(7, getBlock(pblock));
            cachedEntries.setLong(8, System.currentTimeMillis());
            cachedEntries.setBoolean(9, placed);
            cachedEntries.setBoolean(10, false);
            cachedEntries.addBatch();
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

    public void addEntry(String uuid, BlockPos pos, DimensionType dimensionType, String block, String pblock, boolean placed) {
        statements++;
        CompletableFuture.runAsync(() -> {
            try {
                modifyEntries(uuid, pos, dimensionType, block, pblock, placed);
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
