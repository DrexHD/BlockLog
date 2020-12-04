package me.drex.logblock;

import me.drex.logblock.config.LogBlockConfig;
import me.drex.logblock.database.DBCache;
import me.drex.logblock.database.DBConnection;
import me.drex.logblock.util.PermissionUtil;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class BlockLog implements DedicatedServerModInitializer {

    private static final Logger LOGGER = LogManager.getLogger("BlockLog");
    private static BlockLog INSTANCE;
    private static final Path path = new File(System.getProperty("user.dir")).toPath().resolve("config");
    public static MinecraftServer server;
    public static DBConnection dbConnection;
    private static DBCache dbCache;
    private static HashMap<UUID, Boolean> inspecting = new HashMap<>();
    public static PermissionUtil permissionUtil;

    @Override
    public void onInitializeServer() {
        LogBlockConfig.load();
        try {
            dbConnection = new DBConnection();
            dbCache = new DBCache(dbConnection.get());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        permissionUtil = new PermissionUtil();
        INSTANCE = this;
    }

    public static Path getPath() {
        return path;
    }

    public static BlockLog getInstance() {
        return INSTANCE;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Connection getConnection() {
        return dbConnection.get();
    }

    public static DBCache getCache()  { return dbCache; }

    public static boolean isInspecting(UUID uuid) {
        return inspecting.getOrDefault(uuid, false);
    }

    public static void setInspecting(UUID uuid, boolean value) {
        inspecting.put(uuid, value);
    }

    public static boolean hasPermission(ServerCommandSource source, String permission) {
        return permissionUtil.hasPermission(source, permission, 2);
    }

}
