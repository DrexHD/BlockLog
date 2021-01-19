package me.drex.logblock.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.HistoryColumn;
import me.drex.logblock.util.LoadingTimer;
import me.drex.logblock.util.MessageUtil;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InspectCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> inspect = LiteralArgumentBuilder.literal("inspect");
        RequiredArgumentBuilder<ServerCommandSource, PosArgument> blockPos = RequiredArgumentBuilder.argument("blockpos", BlockPosArgumentType.blockPos());
        blockPos.executes(InspectCommand::lookup);
        inspect.executes(InspectCommand::toggle);

        inspect.requires(source -> BlockLog.hasPermission(source, "blocklog.inspect"));
        inspect.then(blockPos);
        command.then(inspect);
    }

    private static int toggle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        UUID uuid = context.getSource().getPlayer().getUuid();
        Boolean b = BlockLog.isInspecting(uuid);
        BlockLog.setInspecting(uuid, !b);
        context.getSource().sendFeedback(new LiteralText("Block inspector " + (b ? "disabled" : "enabled")).formatted(b ? Formatting.RED : Formatting.GREEN), false);
        return 1;
    }

    private static int lookup(CommandContext<ServerCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            Thread.currentThread().setName("BlockLog$Lookup");
            try {
                BlockPos pos = BlockPosArgumentType.getBlockPos(context, "blockpos");
                String criteria = HistoryColumn.XPOS + " = " + pos.getX() + " AND " + HistoryColumn.YPOS + " = " + pos.getY() + " AND " + HistoryColumn.ZPOS + " = " + pos.getZ();
                LoadingTimer lt = new LoadingTimer(context.getSource().getPlayer());
                ResultSet resultSet = DBUtil.getDataWhere(criteria, false);
                lt.stop();
                MessageUtil.send(context.getSource(), resultSet, new LiteralText("(").formatted(Formatting.GRAY).append(new LiteralText( pos.getX() + " " + pos.getY() + " " + pos.getZ() + ")").formatted(Formatting.GRAY)));
            } catch (SQLException | CommandSyntaxException e) {
                e.printStackTrace();
            }
        });

        return 1;
    }

}
