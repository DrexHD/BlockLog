package me.drex.logblock.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.database.entry.*;
import me.drex.logblock.database.entry.util.EntryCache;
import me.drex.logblock.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.time.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RollbackCommand {

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
                AtomicReference<String> userCriteria = new AtomicReference<>();
                AtomicReference<String> blockCriteria = new AtomicReference<>();
                AtomicReference<String> radiusCriteria = new AtomicReference<>();
                AtomicReference<String> timeCriteria = new AtomicReference<>();
                AtomicReference<String> dimensionCriteria = new AtomicReference<>();
                AtomicInteger atomicI = new AtomicInteger();
                ArgumentUtil.parseUser(context, s -> {
                    userCriteria.set(s);
                    atomicI.getAndIncrement();
                });
                ArgumentUtil.parseBlock(context, s -> {
                    blockCriteria.set(s);
                    atomicI.getAndIncrement();
                });
                ArgumentUtil.parseRadius(context, s -> {
                    radiusCriteria.set(s);
                    atomicI.getAndIncrement();
                });
                ArgumentUtil.parseTime(context, s -> {
                    timeCriteria.set(s);
                    atomicI.getAndIncrement();
                });
                ArgumentUtil.parseDimension(context, s -> {
                    dimensionCriteria.set(s);
                    atomicI.getAndIncrement();
                });
                while (atomicI.get() < 5) {};
                criterias.add(userCriteria.get());
                criterias.add(blockCriteria.get());
                criterias.add(radiusCriteria.get());
                criterias.add(timeCriteria.get());
                criterias.add(dimensionCriteria.get());

//                criterias.add(ArgumentUtil.parseUser(context));
//                criterias.add(ArgumentUtil.parseBlock(context));
//                criterias.add(ArgumentUtil.parseRadius(context));
//                criterias.add(ArgumentUtil.parseTime(context));

//                criterias.add(HistoryColumn.DIMENSIONID + " = " + DimensionEntry.of(DimensionEntry.class, WorldUtil.getDimensionNameSpace(context.getSource().getWorld().getDimension())).getID());
//                DimensionEntry.of(DimensionEntry.class, WorldUtil.getDimensionNameSpace(context.getSource().getWorld().getDimension()), entry -> {
//                    criterias.add(HistoryColumn.DIMENSIONID + " = " + entry.getID());
//                    criterias.add(HistoryColumn.UNDONE + " = " + (undo ? "true" : "false"));
//                });
                LoadingTimer lt = new LoadingTimer(context.getSource().getPlayer());
                ResultSet resultSet = DBUtil.getDataWhere(ArgumentUtil.formatQuery("", criterias, "AND"), false);
                lt.stop();
                resultSet.last();
                int size = resultSet.getRow();
                resultSet.beforeFirst();
                if (size > 5000) {
                    context.getSource().sendError(new LiteralText("You can't manipulate more than 5000 blocks at once!").formatted(Formatting.RED));
                    return;
                }
                context.getSource().sendFeedback(new LiteralText("Query done, starting rollback ").formatted(Formatting.WHITE).append(new LiteralText("(" + size + " actions)").formatted(Formatting.GRAY)).append(new LiteralText("!").formatted(Formatting.WHITE)), false);
                int i = 0;
                while (resultSet.next()) {
                    i++;
                    StopWatch setBlockTime = StopWatch.createStarted();
                    context.getSource().getPlayer().networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, new LiteralText(i + " / " + size + " - (Average: " + (undoBlocks.getTime() / i) + "ms / block)").formatted(Formatting.LIGHT_PURPLE), -1, 200, -1));
                    int x = resultSet.getInt(HistoryColumn.XPOS.toString());
                    //System.out.println("x " + x);
                    int y = resultSet.getInt(HistoryColumn.YPOS.toString());
                    //System.out.println("y " + y);
                    int z = resultSet.getInt(HistoryColumn.ZPOS.toString());
                    //System.out.println("z " + z);
                    World world = context.getSource().getWorld();
//                    World world = WorldUtil.getWorldType(DimensionEntry.of(DimensionEntry.class, WorldUtil.getDimensionNameSpace()).getValue());
                    //System.out.println("world: " + world);
                    String blockName = (String) EntryCache.get(BlockEntry.class, resultSet.getInt(undo ? HistoryColumn.BLOCKID.toString() : HistoryColumn.PBLOCKID.toString())).getValue();
//                    String blockName = BlockEntry.of(BlockEntry.class, resultSet.getInt(undo ? HistoryColumn.BLOCKID.toString() : HistoryColumn.PBLOCKID.toString()), "").getValue(); /*BlockLog.getCache().getBlock(resultSet.getInt(undo ? HistoryColumn.BLOCKID.toString() : HistoryColumn.PBLOCKID.toString()));*/
                    //System.out.println("Block: " + blockName);

                    String blockState = (String) EntryCache.get(BlockStateEntry.class, resultSet.getInt(undo ? HistoryColumn.BLOCKSTATEID.toString() : HistoryColumn.PBLOCKSTATEID.toString())).getValue();
//                    String blockState = BlockStateEntry.of(BlockStateEntry.class, resultSet.getInt(undo ? HistoryColumn.BLOCKSTATEID.toString() : HistoryColumn.PBLOCKSTATEID.toString()), "").getValue();
                  //System.out.println("blockState: " + blockState);
                    Block block = Registry.BLOCK.get(new Identifier(blockName));
                    ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) EntryCache.get(BlockTagEntry.class, resultSet.getInt(undo ? HistoryColumn.BLOCKTAGID.toString() : HistoryColumn.PBLOCKTAGID.toString())).getValue());
//                    ByteArrayInputStream inputStream = new ByteArrayInputStream(BlockTagEntry.of(BlockTagEntry.class, resultSet.getInt(undo ? HistoryColumn.BLOCKTAGID.toString() : HistoryColumn.PBLOCKTAGID.toString()), new byte[0]).getValue());
                  //System.out.println("inputStream: " + inputStream);
                    CompoundTag tag = NbtIo.readCompressed(inputStream);
                  //System.out.println("tag: " + tag);
                    setBlock(new BlockPos(x, y, z), BlockUtil.fromString(block, blockState), tag, world, resultSet.getInt(HistoryColumn.ID.toString()), !undo);
                    setBlockTime.stop();
                    if (setBlockTime.getTime() > 1000) {
                        context.getSource().sendFeedback(new LiteralText("Warning: " + blockName + " took " + setBlockTime.getTime() + "ms to be placed!").formatted(Formatting.RED), false);
                    }
                }
                stopWatch.stop();
                context.getSource().sendFeedback(new LiteralText("Rolled back " + i + " actions (took " + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)").formatted(Formatting.DARK_AQUA), false);
            } catch (SQLException | CommandSyntaxException | IOException e) {
                e.printStackTrace();
            }
        });
        stopWatch2.stop();


        return 1;
    }

    public static void setBlock(BlockPos pos, BlockState blockState, CompoundTag tag, World world, int id, boolean undone) throws SQLException {
      //System.out.println("Setting block");
        StopWatch stopWatch = StopWatch.createStarted();
        world.setBlockState(pos, blockState);
        if (tag.getSize() > 0) {
          //System.out.println("Placing block entity");
            BlockEntity blockEntity = BlockEntity.createFromTag(pos, blockState, tag);
            world.addBlockEntity(blockEntity);
        }
        DBUtil.setUndone(id, undone);
        stopWatch.stop();
    }

}
