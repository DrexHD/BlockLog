package me.drex.logblock.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.database.entry.BlockEntry;
import me.drex.logblock.database.entry.DimensionEntry;
import me.drex.logblock.database.entry.util.CacheEntry;
import me.drex.logblock.database.request.Requests;
import me.drex.logblock.util.HistoryColumn;
import me.drex.logblock.util.WorldUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TeleportCommand {


    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> teleport = LiteralArgumentBuilder.literal("teleport");
        teleport.requires(source -> BlockLog.hasPermission(source, "blocklog.teleport"));
        RequiredArgumentBuilder<ServerCommandSource, Integer> id = RequiredArgumentBuilder.argument("id", IntegerArgumentType.integer(1));
        id.executes(TeleportCommand::execute);
        teleport.then(id);
        command.then(teleport);
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            int id = IntegerArgumentType.getInteger(context, HistoryColumn.ID.toString());
            ResultSet resultSet = DBUtil.getDataWhere(HistoryColumn.ID + "= " + id, false);
            if(resultSet.next()) {
                int x = resultSet.getInt(HistoryColumn.XPOS.toString());
                int y = resultSet.getInt(HistoryColumn.YPOS.toString());
                int z = resultSet.getInt(HistoryColumn.ZPOS.toString());
                Requests<CacheEntry<?>> r = new Requests<>(1);
                DimensionEntry.of(DimensionEntry.class, resultSet.getInt(HistoryColumn.DIMENSIONID.toString()), "", r::complete);
                while (!r.isDone()) {}
                ServerWorld world = (ServerWorld) WorldUtil.getWorldType((String) r.getOutput().get(0).getValue());
                player.teleport(world, x, y, z, player.yaw, player.pitch);
            } else {
                throw new SimpleCommandExceptionType(new LiteralText("Couldn't find an entry with that id")).create();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }


}
