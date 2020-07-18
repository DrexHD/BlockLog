package me.drex.logblock.database;

import me.drex.logblock.LogBlockMod;
import org.apache.commons.lang3.time.StopWatch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

public class DBUtil {

    public static ResultSet getDataWhere(String criteria) throws SQLException {
        StopWatch stopWatch = StopWatch.createStarted();
        Connection connection = LogBlockMod.getConnection();
        Statement statement = connection.createStatement();
        String where = criteria.equals("") ? "" : " WHERE " + criteria;
        String query = "SELECT * FROM history" + where + " ORDER BY time DESC";
        System.out.println(query);
        ResultSet resultSet = statement.executeQuery(query);
        System.out.println("Query took " + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms");
        return resultSet;
    }

}
