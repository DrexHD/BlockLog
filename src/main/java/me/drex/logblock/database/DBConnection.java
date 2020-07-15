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
                + "	entityID text NOT NULL,\n"
                + "	x int NOT NULL,\n"
                + "	y int NOT NULL,\n"
                + "	z int NOT NULL,\n"
                + "	dimension text NOT NULL,\n"
                + "	block text NOT NULL,\n"
                + "	time long NOT NULL,\n"
                + "	placed boolean NOT NULL,\n"
                + "	undone boolean NOT NULL\n"
                + ");";

        String entities = "CREATE TABLE IF NOT EXISTS entities (\n"
                + "	entityID INTEGER PRIMARY KEY,\n"
                + "	ueid text NOT NULL,\n" //Unique Entity ID
                + "	name text NOT NULL\n"
                + ");";

        stmt.execute(history);
        stmt.execute(entities);
    }

    public java.sql.Connection get() {
        return this.connection;
    }

    public void close() throws SQLException {
        connection.close();
    }

}
