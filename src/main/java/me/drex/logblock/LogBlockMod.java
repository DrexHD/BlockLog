package me.drex.logblock;

import me.drex.logblock.database.DBCache;
import me.drex.logblock.database.DBConnection;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class LogBlockMod implements DedicatedServerModInitializer {

    private static final Logger LOGGER = LogManager.getLogger("AntiXray");
    private static LogBlockMod INSTANCE;
    private static final Path path = new File(System.getProperty("user.dir")).toPath().resolve("config");
    public static MinecraftServer server;
    public static DBConnection dbConnection;
    private static DBCache dbCache;

    @Override
    public void onInitializeServer() {
        try {
            dbConnection = new DBConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        try {
            dbCache = new DBCache(dbConnection.get());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        INSTANCE = this;
//        DBUtil.start();
    }

    public static Path getPath() {
        return path;
    }

    public static LogBlockMod getInstance() {
        return INSTANCE;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Connection getConnection() {
        return dbConnection.get();
    }

    public static DBCache getCache()  { return dbCache; }

}
