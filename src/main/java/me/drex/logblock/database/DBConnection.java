package me.drex.logblock.database;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private final java.sql.Connection connection;

    public DBConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        this.connection = DriverManager.getConnection("jdbc:sqlite:test.db");
        setupTables();
    }

    public void setupTables() throws SQLException {
        Statement stmt = connection.createStatement();
        String history = "CREATE TABLE IF NOT EXISTS history (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	entityid INTEGER NOT NULL,\n"
                + "	x INTEGER NOT NULL,\n"
                + "	y INTEGER NOT NULL,\n"
                + "	z INTEGER NOT NULL,\n"
                + "	dimensionid INTEGER NOT NULL,\n"
                + "	blockid INTEGER NOT NULL,\n"
                + "	pblockid INTEGER NOT NULL,\n"
                + "	time LONG NOT NULL,\n"
                + "	placed BOOLEAN NOT NULL,\n"
                + "	undone BOOLEAN NOT NULL\n"
                + ");";

        String entities = "CREATE TABLE IF NOT EXISTS entities (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	value text NOT NULL\n"
                + ");";

        String blocks = "CREATE TABLE IF NOT EXISTS blocks (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	value text NOT NULL\n"
                + ");";

        String dimensions = "CREATE TABLE IF NOT EXISTS dimensions (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	value text NOT NULL\n"
                + ");";

        stmt.execute(history);
        stmt.execute(entities);
        stmt.execute(blocks);
        stmt.execute(dimensions);
    }

    public java.sql.Connection get() {
        return this.connection;
    }

    public void close() throws SQLException {
        connection.close();
    }

}
