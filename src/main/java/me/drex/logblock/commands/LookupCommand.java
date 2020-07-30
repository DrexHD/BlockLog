package me.drex.logblock.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.ArgumentUtil;
import me.drex.logblock.util.MessageUtil;
import me.drex.logblock.util.WorldUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;


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

    private static int lookup(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            ArrayList<String> criterias = new ArrayList<>();
            criterias.add(ArgumentUtil.parseUser(context));
            criterias.add(ArgumentUtil.parseBlock(context));
            criterias.add(ArgumentUtil.parseRadius(context));
            criterias.add(ArgumentUtil.parseTime(context));
            criterias.add("dimensionid = " + BlockLog.getCache().getDimension(WorldUtil.getDimensionNameWithNameSpace(context.getSource().getWorld().getDimension())));

            BlockPos pos = context.getSource().getPlayer().getBlockPos();
            ResultSet resultSet = DBUtil.getDataWhere(parseQuery("", criterias), false);
            MessageUtil.send(context.getSource(), resultSet, new LiteralText("(").formatted(Formatting.GRAY).append(new LiteralText(pos.getX() + " " + pos.getZ() + " " + pos.getZ() + ")").formatted(Formatting.GRAY)));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SimpleCommandExceptionType(new LiteralText("SQL Exception " + e.getMessage())).create();
        }
        return 1;

    }


    private static String convertSecondsToString(long seconds) {
        double hours = (double) seconds / 3600;
        return df2.format(hours);
    }


    public static String parseQuery(String query, ArrayList<String> list) {
        if (list.isEmpty()) {
            return query;
        } else {
            String s = list.remove(0);
            query += query.isEmpty() || s.isEmpty() ? s : " AND " + s;
            return parseQuery(query, list);
        }
    }

}
