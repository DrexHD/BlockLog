package me.drex.logblock.config;

import com.google.common.reflect.TypeToken;
import me.drex.logblock.BlockLog;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.File;
import java.io.IOException;


public class LogBlockConfig {
    private static Config config;
    private static ConfigurationNode mainNode;

    public static Config getConfig() {
        return config;
    }

    public static void load() {
        try {
            File CONFIG_FILE = BlockLog.getPath().resolve("logblock.hocon").toFile();
            ConfigurationLoader<CommentedConfigurationNode> mainLoader = HoconConfigurationLoader.builder()
                    .setFile(CONFIG_FILE).build();

            CONFIG_FILE.createNewFile();

            mainNode = mainLoader.load(configurationOptions());

            config = mainNode.getValue(TypeToken.of(Config.class), new Config());

            mainLoader.save(mainNode);
        } catch (IOException | ObjectMappingException e) {
            BlockLog.getLogger().error("Exception handling a configuration file! " + LogBlockConfig.class.getName());
            e.printStackTrace();
        }
    }

    public static ConfigurationOptions configurationOptions() {
        return ConfigurationOptions.defaults()
                .setHeader(Config.HEADER)
                .setObjectMapperFactory(DefaultObjectMapperFactory.getInstance())
                .setShouldCopyDefaults(true);
    }

}
