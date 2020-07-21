package me.drex.logblock.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.ArgumentUtil;
import me.drex.logblock.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.time.StopWatch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Rollback {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> rollback = LiteralArgumentBuilder.literal("rollback");
        RequiredArgumentBuilder<ServerCommandSource, String> player = ArgumentUtil.getUser();
        RequiredArgumentBuilder<ServerCommandSource, String> radius = ArgumentUtil.getRadius();
        RequiredArgumentBuilder<ServerCommandSource, String> block = ArgumentUtil.getBlock();
        RequiredArgumentBuilder<ServerCommandSource, String> time = ArgumentUtil.getTime();
        time.executes(context -> execute(context, false));
        block.then(time);
        radius.then(block);
        player.then(radius);
        rollback.then(player);
        rollback.requires(source -> BlockLog.hasPermission(source, "blocklog.rollback"));
        command.then(rollback);
    }

    public static int execute(CommandContext<ServerCommandSource> context, boolean undo) {
        StopWatch stopWatch = StopWatch.createStarted();
        context.getSource().sendFeedback(new LiteralText("Rollback started!").formatted(Formatting.DARK_AQUA), false);
        StopWatch stopWatch2 = StopWatch.createStarted();
        CompletableFuture.runAsync(() -> {
            try {
                StopWatch undoBlocks = StopWatch.createStarted();
                ArrayList<String> criterias = new ArrayList<>();
                criterias.add(ArgumentUtil.parseUser(context));
                criterias.add(ArgumentUtil.parseBlock(context));
                criterias.add(ArgumentUtil.parseRadius(context));
                criterias.add(ArgumentUtil.parseTime(context));
                criterias.add("dimensionid = " + BlockLog.getCache().getDimension(WorldUtil.getDimensionNameWithNameSpace(context.getSource().getWorld().getDimension())));
                criterias.add("undone=" + (undo ? "true" : "false"));
                ResultSet resultSet = DBUtil.getDataWhere(LookupCommand.parseQuery("", criterias));
                resultSet.last();
                int size = resultSet.getRow();
                resultSet.beforeFirst();
                context.getSource().sendFeedback(new LiteralText("Query done, starting rollback ").formatted(Formatting.WHITE).append(new LiteralText("(" + size + " actions)").formatted(Formatting.GRAY)).append(new LiteralText("!").formatted(Formatting.WHITE)), false);
                int i = 0;
                while (resultSet.next()) {
                    i++;
                    StopWatch setBlockTime = StopWatch.createStarted();
                    context.getSource().getPlayer().networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, new LiteralText(i + " / " + size + " - (Average: " + (undoBlocks.getTime() / i) + "ms / block)").formatted(Formatting.LIGHT_PURPLE), -1, 200, -1));
                    int x = resultSet.getInt("x");
                    int y = resultSet.getInt("y");
                    int z = resultSet.getInt("z");
                    World world = WorldUtil.getWorldType(BlockLog.getCache().getDimension(resultSet.getInt("dimensionid")));
                    String blockName = BlockLog.getCache().getBlock(resultSet.getInt(undo ? "blockid" : "pblockid"));
                    Block block = Registry.BLOCK.get(new Identifier(blockName));
                    setBlock(new BlockPos(x, y, z), block, world, resultSet.getInt("id"), !undo);
                    setBlockTime.stop();
                    if (setBlockTime.getTime() > 1000) {
                        context.getSource().sendFeedback(new LiteralText("Warning: " + blockName + " took " + setBlockTime.getTime() + "ms to be placed!").formatted(Formatting.RED), false);
                    }
                }
                stopWatch.stop();
                context.getSource().sendFeedback(new LiteralText("Rolled back " + i + " actions (took " + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)").formatted(Formatting.DARK_AQUA), false);
            } catch (SQLException | CommandSyntaxException e) {
                e.printStackTrace();
            }
        });
        stopWatch2.stop();


        return 1;
    }

    public static void setBlock(BlockPos pos, Block block, World world, int id, boolean undone) throws SQLException {
        StopWatch stopWatch = StopWatch.createStarted();
        world.setBlockState(pos, block.getDefaultState());
        DBUtil.setUndone(id, undone);
        stopWatch.stop();
    }

}
