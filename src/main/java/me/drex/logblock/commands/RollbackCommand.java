package me.drex.logblock.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.database.entry.BlockEntry;
import me.drex.logblock.database.entry.BlockStateEntry;
import me.drex.logblock.database.entry.BlockTagEntry;
import me.drex.logblock.database.entry.util.EntryCache;
import me.drex.logblock.database.request.Requests;
import me.drex.logblock.util.ArgumentUtil;
import me.drex.logblock.util.BlockUtil;
import me.drex.logblock.util.HistoryColumn;
import me.drex.logblock.util.LoadingTimer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    public static int execute(CommandContext<ServerCommandSource> ctx, boolean undo) {
        StopWatch stopWatch = StopWatch.createStarted();
        ctx.getSource().sendFeedback(new LiteralText("Started rollback!").formatted(Formatting.GREEN)
                .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        formatHover(ctx, "Entity", "user")
                        .append(formatHover(ctx, "Radius", "radius"))
                        .append(formatHover(ctx, "Block", "block"))
                        .append(formatHover(ctx, "Time", "time"))
                ))), false);
        StopWatch stopWatch2 = StopWatch.createStarted();
        CompletableFuture.runAsync(() -> {
            Thread.currentThread().setName("BlockLog$Rollback");
            try {
                StopWatch undoBlocks = StopWatch.createStarted();
                Requests<String> r = new Requests<>(5);
                ArgumentUtil.parseUser(ctx, r::complete);
                ArgumentUtil.parseBlock(ctx, r::complete);
                ArgumentUtil.parseRadius(ctx, r::complete);
                ArgumentUtil.parseTime(ctx, r::complete);
                ArgumentUtil.parseDimension(ctx, r::complete);
                if (r.block(5000)) {
                    LoadingTimer lt = new LoadingTimer(ctx.getSource().getPlayer());
                    List<String> criterias = r.getOutput();
                    criterias.add(HistoryColumn.UNDONE + " = " + undo);
                    ResultSet resultSet = DBUtil.getDataWhere(ArgumentUtil.formatQuery("", criterias, "AND"), false);
                    lt.stop();
                    resultSet.last();
                    int size = resultSet.getRow();
                    resultSet.beforeFirst();
                    if (size > 100000) {
                        ctx.getSource().sendError(new LiteralText("You can't manipulate more than 100000 blocks at once!").formatted(Formatting.RED));
                        return;
                    }
                    ctx.getSource().sendFeedback(new LiteralText("Query done, starting rollback ").formatted(Formatting.WHITE).append(new LiteralText("(" + size + " actions)").formatted(Formatting.GRAY)).append(new LiteralText("!").formatted(Formatting.WHITE)), false);
                    int i = 0;
                    while (resultSet.next()) {
                        i++;
                        StopWatch setBlockTime = StopWatch.createStarted();
                        ctx.getSource().getPlayer().networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, new LiteralText(i + " / " + size + " - (Average: " + (undoBlocks.getTime(TimeUnit.MICROSECONDS) / i) + "μs / block)").formatted(Formatting.LIGHT_PURPLE), -1, 200, -1));
                        int x = resultSet.getInt(HistoryColumn.XPOS.toString());
                        int y = resultSet.getInt(HistoryColumn.YPOS.toString());
                        int z = resultSet.getInt(HistoryColumn.ZPOS.toString());
                        World world = ctx.getSource().getWorld();
                        String blockName = (String) EntryCache.get(BlockEntry.class, resultSet.getInt(undo ? HistoryColumn.BLOCKID.toString() : HistoryColumn.PBLOCKID.toString())).getValue();
                        String blockState = (String) EntryCache.get(BlockStateEntry.class, resultSet.getInt(undo ? HistoryColumn.BLOCKSTATEID.toString() : HistoryColumn.PBLOCKSTATEID.toString())).getValue();
                        Block block = Registry.BLOCK.get(new Identifier(blockName));
                        ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) EntryCache.get(BlockTagEntry.class, resultSet.getInt(undo ? HistoryColumn.BLOCKTAGID.toString() : HistoryColumn.PBLOCKTAGID.toString())).getValue());
                        CompoundTag tag = NbtIo.readCompressed(inputStream);
                        setBlock(new BlockPos(x, y, z), BlockUtil.fromString(block, blockState), tag, world, resultSet.getInt(HistoryColumn.ID.toString()), !undo);
                        setBlockTime.stop();
                        if (setBlockTime.getTime() > 1000) {
                            ctx.getSource().sendFeedback(new LiteralText("Warning: " + blockName + "at " + x + " " + y + " " + z + " took " + setBlockTime.getTime() + "ms to be placed!").formatted(Formatting.RED), false);
                        }
                    }
                    stopWatch.stop();
                    ctx.getSource().sendFeedback(new LiteralText("Rolled back " + i + " actions").formatted(Formatting.WHITE).append(new LiteralText(" (took " + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)").formatted(Formatting.GRAY)).append(new LiteralText("!").formatted(Formatting.WHITE)), false);
                }
            } catch (SQLException | CommandSyntaxException | IOException e) {
                e.printStackTrace();
            }
        });
        stopWatch2.stop();


        return 1;
    }

    private static MutableText formatHover(CommandContext<ServerCommandSource> ctx, String title, String arg) {
        return new LiteralText(title + ": ").formatted(Formatting.AQUA).append(new LiteralText(StringArgumentType.getString(ctx, arg)).formatted(Formatting.WHITE)).append(new LiteralText("\n"));
    }

    public static void setBlock(BlockPos pos, BlockState state, CompoundTag tag, World world, int id, boolean undone) throws SQLException {
        BlockState blockState = world.getChunk(pos).setBlockState(pos, state, false);
        world.updateListeners(pos, blockState, state, 0);
        if (tag.getSize() > 0) {
            BlockEntity blockEntity = BlockEntity.createFromTag(pos, blockState, tag);
            world.addBlockEntity(blockEntity);
        }
        DBUtil.setUndone(id, undone);
    }

}
