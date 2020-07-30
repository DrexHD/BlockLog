package me.drex.logblock.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.drex.logblock.BlockLog;
import me.drex.logblock.util.ArgumentUtil;
import net.minecraft.server.command.ServerCommandSource;

public class UndoCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> undo = LiteralArgumentBuilder.literal("undo");
        RequiredArgumentBuilder<ServerCommandSource, String> player = ArgumentUtil.getUser();
        RequiredArgumentBuilder<ServerCommandSource, String> radius = ArgumentUtil.getRadius();
        RequiredArgumentBuilder<ServerCommandSource, String> block = ArgumentUtil.getBlock();
        RequiredArgumentBuilder<ServerCommandSource, String> time = ArgumentUtil.getTime();
        time.executes(context -> RollbackCommand.execute(context, true));
        block.then(time);
        radius.then(block);
        player.then(radius);
        undo.then(player);
        undo.requires(source -> BlockLog.hasPermission(source, "blocklog.undo"));
        command.then(undo);
    }

}
