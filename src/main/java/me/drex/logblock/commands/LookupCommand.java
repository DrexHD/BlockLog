package me.drex.logblock.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.database.request.Requests;
import me.drex.logblock.util.ArgumentUtil;
import me.drex.logblock.util.LoadingTimer;
import me.drex.logblock.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


public class LookupCommand {

    private static DecimalFormat df2 = new DecimalFormat("#.##");

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> lookup = LiteralArgumentBuilder.literal("lookup");
        RequiredArgumentBuilder<ServerCommandSource, String> player = ArgumentUtil.getUser();
        RequiredArgumentBuilder<ServerCommandSource, String> radius = ArgumentUtil.getRadius();
        RequiredArgumentBuilder<ServerCommandSource, String> block = ArgumentUtil.getBlock();
        RequiredArgumentBuilder<ServerCommandSource, String> time = ArgumentUtil.getTime();
        time.executes(LookupCommand::lookup);
        block.then(time);
        radius.then(block);
        player.then(radius);
        lookup.then(player);
        lookup.requires(source -> BlockLog.hasPermission(source, "blocklog.lookup"));
        command.then(lookup);
    }

    private static int lookup(CommandContext<ServerCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            LoadingTimer lt = null;
            try {
                Requests<String> r = new Requests<>(5);
                ArgumentUtil.parseUser(context, r::complete);
                ArgumentUtil.parseBlock(context, r::complete);
                ArgumentUtil.parseRadius(context, r::complete);
                ArgumentUtil.parseTime(context, r::complete);
                ArgumentUtil.parseDimension(context, r::complete);
                while (!r.isDone()) {};
                BlockPos pos = context.getSource().getPlayer().getBlockPos();
                lt = new LoadingTimer(context.getSource().getPlayer());;
                ResultSet resultSet = DBUtil.getDataWhere(ArgumentUtil.formatQuery("", r.getOutput(), "AND"), false, 250);
                lt.stop();
                MessageUtil.send(context.getSource(), resultSet, new LiteralText("(").formatted(Formatting.GRAY).append(new LiteralText(pos.getX() + " " + pos.getZ() + " " + pos.getZ() + ")").formatted(Formatting.GRAY)));
            } catch (SQLException | CommandSyntaxException e) {
                context.getSource().sendError(new LiteralText(e.getMessage()));
                if (lt != null) lt.stop();
                e.printStackTrace();
            }
        });
        return 1;

    }


    private static String convertSecondsToString(long seconds) {
        double hours = (double) seconds / 3600;
        return df2.format(hours);
    }

}
