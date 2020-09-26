package me.drex.logblock.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;

public class PageCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> page = LiteralArgumentBuilder.literal("page");
        RequiredArgumentBuilder<ServerCommandSource, Integer> i = RequiredArgumentBuilder.argument("i", IntegerArgumentType.integer(0));
        i.executes(PageCommand::execute);

        page.then(i);
        page.requires(source -> BlockLog.hasPermission(source, "blocklog.lookup") || BlockLog.hasPermission(source, "blocklog.inspect"));
        command.then(page);
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        try {
            MessageUtil.sendPage(context.getSource(), 5, IntegerArgumentType.getInteger(context, "i"));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }

}
