package me.drex.logblock.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
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
            int id = IntegerArgumentType.getInteger(context, "id");
            ResultSet resultSet = DBUtil.getDataWhere("id = " + id, false);
            if(resultSet.next()) {
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");
                ServerWorld world = (ServerWorld) WorldUtil.getWorldType(BlockLog.getCache().getDimension(resultSet.getInt("dimensionid")));
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
