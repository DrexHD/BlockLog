package me.drex.logblock.database;

import me.drex.logblock.config.Config;
import me.drex.logblock.config.LogBlockConfig;

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
        String history = "CREATE TABLE IF NOT EXISTS history (\n"
                + "	id INTEGER NOT NULL AUTO_INCREMENT,\n"
                + "	entityid INTEGER NOT NULL,\n"
                + "	x INTEGER NOT NULL,\n"
                + "	y INTEGER NOT NULL,\n"
                + "	z INTEGER NOT NULL,\n"
                + "	dimensionid INTEGER NOT NULL,\n"
                + "	blockid INTEGER NOT NULL,\n"
                + "	blockstateid INTEGER NOT NULL,\n"
                + "	pblockid INTEGER NOT NULL,\n"
                + "	pblockstateid INTEGER NOT NULL,\n"
                + "	time LONG NOT NULL,\n"
                + "	placed BOOLEAN NOT NULL,\n"
                + "	undone BOOLEAN NOT NULL,\n"
                + "	PRIMARY KEY (id)\n"
                + ");";

        String entities = "CREATE TABLE IF NOT EXISTS entities (\n"
                + "	id INTEGER NOT NULL AUTO_INCREMENT,\n"
                + "	value text NOT NULL,\n"
                + "	PRIMARY KEY (id)\n"
                + ");";

        String blocks = "CREATE TABLE IF NOT EXISTS blocks (\n"
                + "	id INTEGER NOT NULL AUTO_INCREMENT,\n"
                + "	value text NOT NULL,\n"
                + "PRIMARY KEY (id)\n"
                + ");";

        String blockstates = "CREATE TABLE IF NOT EXISTS blockstates (\n"
                + "	id INTEGER NOT NULL AUTO_INCREMENT,\n"
                + "	value text NOT NULL,\n"
                + "PRIMARY KEY (id)\n"
                + ");";

        String dimensions = "CREATE TABLE IF NOT EXISTS dimensions (\n"
                + "	id INTEGER NOT NULL AUTO_INCREMENT,\n"
                + "	value text NOT NULL,\n"
                + "PRIMARY KEY (id)"
                + ");";

        stmt.execute(history);
        stmt.execute(entities);
        stmt.execute(blocks);
        stmt.execute(blockstates);
        stmt.execute(dimensions);
    }

    public java.sql.Connection get() {
        return this.connection;
    }

    public void close() throws SQLException {
        connection.close();
    }

}
