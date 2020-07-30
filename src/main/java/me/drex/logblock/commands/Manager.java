package me.drex.logblock.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.logblock.BlockLog;
import net.minecraft.server.command.ServerCommandSource;

public class Manager {

    public static CommandDispatcher dispatcher;

    public static void register() {
        LiteralArgumentBuilder<ServerCommandSource> main = LiteralArgumentBuilder.literal("blocklog");
        LiteralArgumentBuilder<ServerCommandSource> alias = LiteralArgumentBuilder.literal("bl");
        register(main, dispatcher);
        register(alias, dispatcher);
        main.requires(source -> BlockLog.hasPermission(source, "blocklog"));
        alias.requires(source -> BlockLog.hasPermission(source, "blocklog"));
        dispatcher.register(main);
        dispatcher.register(alias);
    }

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandDispatcher dispatcher) {
        RollbackCommand.register(command);
        UndoCommand.register(command);
        LookupCommand.register(command);
        InspectCommand.register(command);
        PageCommand.register(command);
        InfoCommand.register(command);
    }

}
