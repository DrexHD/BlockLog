package me.drex.logblock.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

public class Manager {

    public static CommandDispatcher dispatcher;

    public static void register() {
        LiteralArgumentBuilder<ServerCommandSource> main = LiteralArgumentBuilder.literal("logblock");
        LiteralArgumentBuilder<ServerCommandSource> alias = LiteralArgumentBuilder.literal("lb");
        register(main, dispatcher);
        register(alias, dispatcher);
        dispatcher.register(main);
        dispatcher.register(alias);
    }

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandDispatcher dispatcher) {
        UndoCommand.register(command);
        LookupCommand.register(command);
    }

}
