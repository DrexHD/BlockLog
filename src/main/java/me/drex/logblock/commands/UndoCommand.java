package me.drex.logblock.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.LogBlockMod;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.WorldUtil;
import net.minecraft.block.Block;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UndoCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> undo = LiteralArgumentBuilder.literal("undo");
        RequiredArgumentBuilder<ServerCommandSource, String> criteria = RequiredArgumentBuilder.argument("criteria", StringArgumentType.greedyString());
        criteria.executes(context -> execute(context, StringArgumentType.getString(context, "criteria")));
        undo.executes(context -> execute(context, ""));
        undo.then(criteria);
        command.then(undo);
    }

    public static int execute(CommandContext<ServerCommandSource> context, String criteria) throws CommandSyntaxException {
        StopWatch stopWatch = StopWatch.createStarted();

        criteria = "undone=false " + criteria;
        StopWatch stopWatch2 = StopWatch.createStarted();
        String finalCriteria = criteria;
        CompletableFuture.runAsync(() -> {
            try {
                StopWatch stopWatch1 = StopWatch.createStarted();

                ResultSet resultSet = DBUtil.getDataWhere(finalCriteria);
                context.getSource().sendFeedback(new LiteralText("Query done, starting operation!").formatted(Formatting.GREEN), false);
                stopWatch1.stop();
                System.out.println("Took: " + stopWatch1.getTime(TimeUnit.MILLISECONDS));
                int i = 0;
                while (resultSet.next()) {
                    i++;
/*                    boolean placed = resultSet.getBoolean("placed");
                    boolean undone = resultSet.getBoolean("undone");
                    if (!placed && !undone) {
                        int x = resultSet.getInt("x");
                        int y = resultSet.getInt("y");
                        int z = resultSet.getInt("z");
                        World world = WorldUtil.getWorldType(LogBlockMod.getCache().getDimension(resultSet.getInt("dimensionid")));
                        Block block = Registry.BLOCK.get(new Identifier(LogBlockMod.getCache().getBlock(resultSet.getInt("pblockid"))));
                        setBlock(new BlockPos(x, y, z), block, world, resultSet.getInt("id"), true);
                    } else if (placed && !undone) {*/
                        int x = resultSet.getInt("x");
                        int y = resultSet.getInt("y");
                        int z = resultSet.getInt("z");
                        World world = WorldUtil.getWorldType(LogBlockMod.getCache().getDimension(resultSet.getInt("dimensionid")));
                        Block block = Registry.BLOCK.get(new Identifier(LogBlockMod.getCache().getBlock(resultSet.getInt("pblockid"))));
                        setBlock(new BlockPos(x, y, z), block, world, resultSet.getInt("id"), true);
                    /*}*/
                }
                stopWatch.stop();
                context.getSource().sendFeedback(new LiteralText("Rolled back " + i + " actions (took " + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)").formatted(Formatting.AQUA), false);
            } catch (SQLException e) {
//                throw new SimpleCommandExceptionType(new LiteralText("SQL Exception")).create();
                e.printStackTrace();
            }
        });
        stopWatch2.stop();
        System.out.println("Iteration Took: " + stopWatch2.getTime(TimeUnit.MILLISECONDS));


        return 1;
    }

    public static void setBlock(BlockPos pos, Block block, World world, int id, boolean undone) throws SQLException {
        StopWatch stopWatch = StopWatch.createStarted();
        world.setBlockState(pos, block.getDefaultState());
        LogBlockMod.getCache().addUndo(id, undone);
//        DBUtil.setUndone(id, undone);
        stopWatch.stop();
//        System.out.println("Setting block took: " + stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

}
