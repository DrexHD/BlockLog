package me.drex.logblock.database;

import me.drex.logblock.BlockLog;
import org.apache.commons.lang3.time.StopWatch;

import java.sql.*;
import java.util.concurrent.TimeUnit;

public class DBUtil {

    public static ResultSet getDataWhere(String criteria) throws SQLException {
        StopWatch stopWatch = StopWatch.createStarted();
        Connection connection = BlockLog.getConnection();
        Statement statement = connection.createStatement();
        String where = criteria.equals("") ? "" : " WHERE " + criteria;
        String query = "SELECT * FROM history" + where + " ORDER BY time DESC";
        System.out.println(query);
        ResultSet resultSet = statement.executeQuery(query);
        System.out.println("getDataWhere Query took " + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms");
        return resultSet;
    }

    public static void setUndone(int id, boolean value) throws SQLException {
        PreparedStatement cachedUndos = BlockLog.getConnection().prepareStatement("UPDATE history SET undone = ? WHERE id = ?");
        cachedUndos.setBoolean(1, value);
        cachedUndos.setInt(2, id);
        cachedUndos.executeUpdate();
    }

}
