package me.drex.logblock.util;

import com.google.common.collect.Lists;
import me.drex.logblock.LogBlockMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;

public class WorldUtil {

    private static final List<RegistryKey<World>> worldRegistryKeys = Lists.newArrayList();
    private static RegistryKey<World> DEFAULT = World.OVERWORLD;
    private static World DEFAULT_DIMENSION = server().getWorld(DEFAULT);

    static {
        worldRegistryKeys.addAll(server().getWorldRegistryKeys());
    }

    private static MinecraftServer server() {
        return LogBlockMod.server;
    }

    public static ServerWorld getServerWorld(DimensionType dimensionType) {
        return server().getWorld(getRegistry(dimensionType));
    }

    public static String getDimensionName(DimensionType dimensionType) {
        return getRegistry(dimensionType).getValue().getPath();
    }

    public static String getDimensionNameWithNameSpace(DimensionType dimensionType) {
        return getRegistry(dimensionType).getValue().getNamespace() + ":" + getDimensionName(dimensionType);
    }

    public static World getWorldType(String world) {
        for (RegistryKey<World> registryKey : getWorldKeys()) {
            if (world.equalsIgnoreCase(registryKey.getValue().getNamespace() + ":" + registryKey.getValue().getPath())) {
                return server().getWorld(registryKey);
            }
        }
        return DEFAULT_DIMENSION;
    }

    public static RegistryKey<World> getRegistry(DimensionType dimensionType) {
        for (RegistryKey<World> registryKey : getWorldKeys()) {
            DimensionType dimension = server().getWorld(registryKey).getDimension();
            if (dimension.equals(dimensionType)) {
                return registryKey;
            }
        }
        return DEFAULT;
    }

    public static List<RegistryKey<World>> getWorldKeys() {
        return worldRegistryKeys;
    }

}

