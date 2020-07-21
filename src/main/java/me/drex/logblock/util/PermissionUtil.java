package me.drex.logblock.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.config.LogBlockConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.util.Locale;

public class PermissionUtil {
    private Manager manager;
    private boolean present;

    public PermissionUtil() {
        Logger logger = (Logger) LogManager.getLogger("ItsMine");
        logger.info("Setting up Permissions...");
        this.manager = Manager.fromString(LogBlockConfig.getConfig().permissionManager);

        if (manager == Manager.VANILLA) {
            this.present = false;
            logger.info("Using Vanilla Permissions for Claims");
            return;
        }

        logger.info("Checking " + manager.getName() + " for Availability");

        this.present = this.checkPresent();

        if (!this.present) {
            logger.warn("**** " + manager.getName() + " is not present! Switching to vanilla operator system");
            logger.warn("     You need to install LuckPerms for Fabric to manage the permissions");
            this.manager = Manager.NONE;
            return;
        }

        logger.info("Using " + manager.getName() + " as the Permission Manager");

    }

    public static void reload(){
        new PermissionUtil();
    }


    public boolean hasPermission(ServerCommandSource src, String permission, int opLevel) {
        if (present) {
            if (manager == Manager.LUCKPERMS) {
                return fromLuckPerms(src, permission, opLevel);
            }
        }

        return src.hasPermissionLevel(opLevel);
    }

    private boolean fromLuckPerms(ServerCommandSource src, String perm, int op) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        try {
            ServerPlayerEntity player = src.getPlayer();
            User user = luckPerms.getUserManager().getUser(player.getUuid());

            if (user != null) {
                QueryOptions options = luckPerms.getContextManager().getQueryOptions(player);
                return user.getCachedData().getPermissionData(options).checkPermission(perm).asBoolean();
            }

        } catch (CommandSyntaxException ignored) {
        }

        return src.hasPermissionLevel(op);
    }

    private boolean checkPresent() {
        if (manager == Manager.NONE) {
            return false;
        }

        try {
            if (manager == Manager.LUCKPERMS) {
                try {
                    LuckPermsProvider.get();
                    return true;
                } catch (Throwable ignored) {
                }
            }


            return FabricLoader.getInstance().getModContainer(manager.getName().toLowerCase(Locale.ROOT)).isPresent();
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean managerPresent() {
        return this.present;
    }

    public Manager getManager() {
        return this.manager;
    }

    public enum Manager {
        VANILLA("Vanilla", ""),
        NONE("none", ""),
        LUCKPERMS("LuckPerms", "net.luckperms.api.LuckPerms");
        private final String name;
        private final String classPath;

        Manager(final String name, final String classPath) {
            this.name = name;
            this.classPath = classPath;
        }

        public String getName() {
            return this.name;
        }

        public static Manager fromString(String str) {
            for (Manager value : Manager.values()) {
                if (value.name.equalsIgnoreCase(str)) {
                    return value;
                }
            }

            return Manager.NONE;
        }
    }


}
