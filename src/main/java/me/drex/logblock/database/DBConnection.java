package me.drex.logblock.database;

import me.drex.logblock.config.Config;
import me.drex.logblock.config.LogBlockConfig;
import me.drex.logblock.util.Constants;
import me.drex.logblock.util.HistoryColumn;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private final java.sql.Connection connection;

    public DBConnection() throws SQLException {
        Config config = LogBlockConfig.getConfig();
        this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + config.database + "?autoReconnect=true", config.databaseUser, config.databasePassword);
        setupTables();
    }

    public void setupTables() throws SQLException {
        Statement stmt = connection.createStatement();
        String history = "CREATE TABLE IF NOT EXISTS " + Constants.Table.HISTORY + " (\n "
                + HistoryColumn.ID + " INTEGER NOT NULL AUTO_INCREMENT,\n "
                + HistoryColumn.ENTITYID + " INTEGER NOT NULL,\n "
                + HistoryColumn.XPOS + " INTEGER NOT NULL,\n "
                + HistoryColumn.YPOS + " INTEGER NOT NULL,\n "
                + HistoryColumn.ZPOS + " INTEGER NOT NULL,\n "
                + HistoryColumn.DIMENSIONID + " INTEGER NOT NULL,\n "
                + HistoryColumn.BLOCKID + " INTEGER NOT NULL,\n "
                + HistoryColumn.PBLOCKID + " INTEGER NOT NULL,\n "
                + HistoryColumn.BLOCKSTATEID + " INTEGER NOT NULL,\n "
                + HistoryColumn.PBLOCKSTATEID + " INTEGER NOT NULL,\n "
                + HistoryColumn.BLOCKTAGID + " INTEGER NOT NULL,\n "
                + HistoryColumn.PBLOCKTAGID + " INTEGER NOT NULL,\n "
                + HistoryColumn.TIME + " LONG NOT NULL,\n "
                + HistoryColumn.PLACED + " BOOLEAN NOT NULL,\n "
                + HistoryColumn.UNDONE + " BOOLEAN NOT NULL,\n"
                + "	PRIMARY KEY (id)\n"
                + ");";

        String entities = "CREATE TABLE IF NOT EXISTS " + Constants.Table.ENTITIES + " (\n"
                + Constants.CacheColumn.ID + " INTEGER NOT NULL AUTO_INCREMENT,\n"
                + Constants.CacheColumn.VALUE + " text NOT NULL,\n"
                + "	PRIMARY KEY (id)\n"
                + ");";

        String blocks = "CREATE TABLE IF NOT EXISTS " + Constants.Table.BLOCKS + " (\n"
                + Constants.CacheColumn.ID + " INTEGER NOT NULL AUTO_INCREMENT,\n"
                + Constants.CacheColumn.VALUE + " text NOT NULL,\n"
                + "PRIMARY KEY (id)\n"
                + ");";

        String blockstates = "CREATE TABLE IF NOT EXISTS " + Constants.Table.BLOCKSTATES + " (\n"
                + Constants.CacheColumn.ID + " INTEGER NOT NULL AUTO_INCREMENT,\n"
                + Constants.CacheColumn.VALUE + " text NOT NULL,\n"
                + "PRIMARY KEY (id)\n"
                + ");";

        String blocktags = "CREATE TABLE IF NOT EXISTS " + Constants.Table.BLOCKTAGS + " (\n"
                + Constants.CacheColumn.ID + " INTEGER NOT NULL AUTO_INCREMENT,\n"
                + Constants.CacheColumn.VALUE + " text NOT NULL,\n"
                + "PRIMARY KEY (id)\n"
                + ");";

        String dimensions = "CREATE TABLE IF NOT EXISTS " + Constants.Table.DIMENSIONS + " (\n"
                + Constants.CacheColumn.ID + " INTEGER NOT NULL AUTO_INCREMENT,\n"
                + Constants.CacheColumn.VALUE + " text NOT NULL,\n"
                + "PRIMARY KEY (id)"
                + ");";

        stmt.execute(history);
        stmt.execute(entities);
        stmt.execute(blocks);
        stmt.execute(blockstates);
        stmt.execute(blocktags);
        stmt.execute(dimensions);
    }

    public java.sql.Connection get() {
        return this.connection;
    }

    public void close() throws SQLException {
        connection.close();
    }

}
