package me.drex.logblock.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {

    public static final String HEADER = "XRAY! Configuration File\n" +
            "Licensed Under the MIT License, Copyright (c) 2020\n" +
            "Votifier is using HOCON for its configuration files\n learn more about it here: " +
            "https://docs.spongepowered.org/stable/en/server/getting-started/configuration/hocon.html";

    @Setting(value = "enabled")
    public boolean enabled = true;

    @Setting(value = "databaseUser", comment = "Make sure to change this!")
    public String databaseUser = "admin";

    @Setting(value = "databasePassword", comment = "Make sure to change this!")
    public String databasePassword = "admin";

    @Setting(value = "database")
    public String database = "jdbc:mysql://localhost:3306/antixray";


}
