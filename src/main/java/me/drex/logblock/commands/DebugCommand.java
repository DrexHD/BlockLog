package me.drex.logblock.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBCache;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class DebugCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> debug = LiteralArgumentBuilder.literal("debug");
        LiteralArgumentBuilder<ServerCommandSource> setwait = LiteralArgumentBuilder.literal("setwait");
        RequiredArgumentBuilder<ServerCommandSource, Integer> time = RequiredArgumentBuilder.argument("time", IntegerArgumentType.integer(1));
        time.executes(DebugCommand::set);

        setwait.then(time);
        debug.then(setwait);
        debug.requires(source -> BlockLog.hasPermission(source, "blocklog.debug"));
        debug.executes(DebugCommand::info);


        command.then(debug);
    }

    private static int info(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText("Running: ").formatted(Formatting.GRAY).append(new LiteralText(DBCache.running ? "true" : "false").formatted(DBCache.running ? Formatting.RED : Formatting.GREEN)
        ).append(new LiteralText("\nTime: ").formatted(Formatting.GRAY))
                .append(new LiteralText(DBCache.timeRunning.getTime() + "ms").formatted(Formatting.YELLOW))
                .append(new LiteralText("\nStatements left: ").formatted(Formatting.GRAY))
                .append(new LiteralText(String.valueOf(DBCache.statements)).formatted(Formatting.LIGHT_PURPLE))
                .append(new LiteralText("\nTime not running: ").formatted(Formatting.GRAY))
                .append(new LiteralText(DBCache.timeNotRunning.getTime() + "ms").formatted(Formatting.YELLOW))
                .append(new LiteralText("\nAverage time running: ").formatted(Formatting.GRAY))
                .append(new LiteralText(DBCache.timeSpentRunning.getTime() / DBCache.timesRun + "ms").formatted(Formatting.YELLOW))
                .append(new LiteralText("\nAverage time not running: ").formatted(Formatting.GRAY))
                .append(new LiteralText(DBCache.timeSpentNotRunning.getTime() / DBCache.timesRun + "ms").formatted(Formatting.YELLOW))
                .append(new LiteralText("\nWait time: ").formatted(Formatting.GRAY))
                .append(new LiteralText(DBCache.wait + "ms").formatted(Formatting.YELLOW)), false);
        return 1;
    }


    private static int set(CommandContext<ServerCommandSource> context) {
        int wait = IntegerArgumentType.getInteger(context, "time");
        DBCache.wait = wait;
        context.getSource().sendFeedback(new LiteralText("Set wait time to: ").formatted(Formatting.GRAY)
                .append(new LiteralText(String.valueOf(wait)).formatted(Formatting.GREEN)), false);
        return wait;
    }
}
