package me.drex.logblock.database;

import me.drex.logblock.LogBlockMod;
import me.drex.logblock.util.WorldUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DBUtil {

    public static void createEntry(String player, UUID uuid, BlockPos pos, DimensionType dimensionType, String block, boolean placed) {
        System.out.println("Creating entry: ");
        System.out.println(player);
        System.out.println(uuid);
        System.out.println(pos.toString());
        System.out.println(dimensionType.toString());
        System.out.println(block);
        System.out.println(System.currentTimeMillis());
        try {
            Connection connection = LogBlockMod.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO history (id, entityID, x, y, z, dimension, block, time, placed, undone)" + " VALUES (NULL, \"" + getOrCreateUser(uuid.toString(), player).getString("entityID") + "\", " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ", \"" + WorldUtil.getDimensionNameWithNameSpace(dimensionType) + "\", \"" + block + "\", " + System.currentTimeMillis() + ", " + placed  + ", " + false + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getOrCreateUser(String ueid, String name) throws SQLException {
        Connection connection = LogBlockMod.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM entities WHERE ueid = \"" + ueid + "\"");
        if (resultSet.next()) {
            return resultSet;
        } else {
            statement.executeUpdate("INSERT INTO entities (entityID, ueid, name)" + " VALUES (NULL, \"" + ueid + "\", \"" + name + "\")");
            return statement.executeQuery("SELECT * FROM entities WHERE ueid = \"" + ueid + "\"");
        }
    }

    public static ResultSet getDataWhere(String criteria) throws SQLException {
        Connection connection = LogBlockMod.getConnection();
        Statement statement = connection.createStatement();
        String query = criteria.equals("") ? "" : " WHERE " + criteria;
        System.out.println("SELECT * FROM history" + query);
        ResultSet resultSet = statement.executeQuery("SELECT * FROM history" + query);
        return resultSet;
    }

    public static void setUndone(int id, boolean undone) throws SQLException {
        Connection connection = LogBlockMod.getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("UPDATE history SET undone=" + undone + " WHERE id=" + id);

    }

    public static Future<ResultSet> getDataWhereAsync(String criteria) throws InterruptedException {
        CompletableFuture<ResultSet> completableFuture
                = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
//            Thread.sleep(500);
            Connection connection = LogBlockMod.getConnection();
            Statement statement = connection.createStatement();
            String query = criteria.equals("") ? "" : " WHERE " + criteria;
            System.out.println("SELECT * FROM history" + query);
            ResultSet resultSet = statement.executeQuery("SELECT * FROM history" + query);
            completableFuture.complete(resultSet);
            return null;
        });

        return completableFuture;
    }

}
