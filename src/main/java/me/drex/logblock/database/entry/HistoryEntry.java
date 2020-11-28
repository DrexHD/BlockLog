package me.drex.logblock.database.entry;

import com.google.common.base.Stopwatch;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.entry.util.IEntry;
import me.drex.logblock.util.BlockUtil;
import me.drex.logblock.util.Constants;
import me.drex.logblock.util.HistoryColumn;
import me.drex.logblock.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class HistoryEntry implements IEntry {

    private final String entity;
    private final DimensionType dimension;
    private final BlockPos blockPos;
    private final BlockState blockStatePrevious;
    private final BlockState blockStateNow;
    private final long time;
    private final boolean placed;
    private final byte[] bytesPrevious;
    private final byte[] bytesNow;


    public HistoryEntry(String entity, DimensionType dimension, BlockPos blockPos, BlockState blockStatePrevious, BlockState blockStateNow, CompoundTag blockEntityPrevious, CompoundTag blockEntityNow, long time, boolean placed) {
        this.entity = entity;
        this.dimension = dimension;
        this.blockPos = blockPos;
        this.blockStatePrevious = blockStatePrevious;
        this.blockStateNow = blockStateNow;
        this.time = time;
        this.placed = placed;
        ByteArrayOutputStream streamPrevious = new ByteArrayOutputStream();
        ByteArrayOutputStream streamNow = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(blockEntityPrevious, streamPrevious);
            NbtIo.writeCompressed(blockEntityNow, streamNow);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.bytesPrevious = streamPrevious.toByteArray();
        this.bytesNow = streamNow.toByteArray();
    }

    public void saveAsync(Consumer<Integer> action) {
        CompletableFuture.runAsync(() -> {
            try {
                String x = "INSERT INTO " + Constants.Table.HISTORY + " (" + HistoryColumn.ENTITYID + ", " + HistoryColumn.XPOS + ", " + HistoryColumn.YPOS + ", " + HistoryColumn.ZPOS + ", " + HistoryColumn.DIMENSIONID + ", " + HistoryColumn.BLOCKID + ", " + HistoryColumn.PBLOCKID + ", " + HistoryColumn.BLOCKSTATEID + ", " + HistoryColumn.PBLOCKSTATEID + ", " + HistoryColumn.BLOCKTAGID + ", " + HistoryColumn.PBLOCKTAGID + ", " + HistoryColumn.TIME + ", " + HistoryColumn.PLACED + ", " + HistoryColumn.UNDONE + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insert = BlockLog.getConnection().prepareStatement(x, Statement.RETURN_GENERATED_KEYS);
                AtomicInteger i = new AtomicInteger();
                EntityEntry.of(EntityEntry.class, entity, entry -> {
                    try { insert.setInt(1, entry.getID()); } catch (SQLException ignored) { }
                    i.getAndIncrement();
                });
                insert.setInt(2, blockPos.getX());
                insert.setInt(3, blockPos.getY());
                insert.setInt(4, blockPos.getZ());
                DimensionEntry.of(DimensionEntry.class, WorldUtil.getDimensionNameSpace(dimension), entry -> {
                    try { insert.setInt(5, entry.getID()); } catch (SQLException ignored) { }
                    i.getAndIncrement();
                });
                BlockEntry.of(BlockEntry.class, BlockUtil.toNameSpace(blockStateNow.getBlock()), entry -> {
                    try { insert.setInt(6, entry.getID()); } catch (SQLException ignored) { }
                    i.getAndIncrement();
                });
                BlockEntry.of(BlockEntry.class, BlockUtil.toNameSpace(blockStatePrevious.getBlock()), entry -> {
                    try { insert.setInt(7, entry.getID()); } catch (SQLException ignored) { }
                    i.getAndIncrement();
                });
                BlockStateEntry.of(BlockStateEntry.class, BlockUtil.toJsonString(blockStateNow), entry -> {
                    try { insert.setInt(8, entry.getID()); } catch (SQLException ignored) { }
                    i.getAndIncrement();
                });
                BlockStateEntry.of(BlockStateEntry.class, BlockUtil.toJsonString(blockStatePrevious), entry -> {
                    try { insert.setInt(9, entry.getID()); } catch (SQLException ignored) { }
                    i.getAndIncrement();
                });
                BlockTagEntry.of(BlockTagEntry.class, bytesNow, entry -> {
                    try { insert.setInt(10, entry.getID()); } catch (SQLException ignored) { }
                    i.getAndIncrement();
                });
                BlockTagEntry.of(BlockTagEntry.class, bytesPrevious, entry -> {
                    try { insert.setInt(11, entry.getID()); } catch (SQLException ignored) { }
                    i.getAndIncrement();
                });
                insert.setLong(12, time);
                insert.setBoolean(13, placed);
                insert.setBoolean(14, false);
                Stopwatch sw = Stopwatch.createStarted();
                do {
                    if (sw.elapsed(TimeUnit.SECONDS) > 10) {
                        System.out.println("Unable to save entry only " + i.get() + " / 8 values were set after ~10s");
                        return;
                    }
                } while (i.get() < 8);
                insert.addBatch();
                insert.executeUpdate();
                ResultSet rs = insert.getGeneratedKeys();
                if (rs.next()) {
                    action.accept(rs.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveAsync() {
        saveAsync(integer -> {});
    }


}
