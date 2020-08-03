package me.drex.logblock.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.MessageUtil;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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

    private static int lookup(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            BlockPos pos = BlockPosArgumentType.getBlockPos(context, "blockpos");
            String criteria = "x = " + pos.getX() + " AND " + "y = " + pos.getY() + " AND " + "z = " + pos.getZ();
            ResultSet resultSet = DBUtil.getDataWhere(criteria, false);
            MessageUtil.send(context.getSource(), resultSet, new LiteralText("(").formatted(Formatting.GRAY).append(new LiteralText( pos.getX() + " " + pos.getY() + " " + pos.getZ() + ")").formatted(Formatting.GRAY)));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 1;
    }

}
