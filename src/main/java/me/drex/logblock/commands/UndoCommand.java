package me.drex.logblock.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

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
        try {
            ResultSet resultSet = DBUtil.getDataWhere(criteria);
            while (resultSet.next()) {
                boolean placed = resultSet.getBoolean("placed");
                boolean undone = resultSet.getBoolean("undone");
                if (!placed && !undone) {
                    int x = resultSet.getInt("x");
                    int y = resultSet.getInt("y");
                    int z = resultSet.getInt("z");
                    World world = WorldUtil.getWorldType(resultSet.getString("dimension"));
                    Block block = Registry.BLOCK.get(new Identifier(resultSet.getString("block")));
                    setBlock(new BlockPos(x, y, z), block, world);
                    DBUtil.setUndone(resultSet.getInt("id"), true);
                } else if (placed && !undone) {
                    int x = resultSet.getInt("x");
                    int y = resultSet.getInt("y");
                    int z = resultSet.getInt("z");
                    World world = WorldUtil.getWorldType(resultSet.getString("dimension"));
                    setBlock(new BlockPos(x, y, z), Blocks.AIR, world);
                    DBUtil.setUndone(resultSet.getInt("id"), true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SimpleCommandExceptionType(new LiteralText("SQL Exception (" + e.getMessage())).create();
        }

        return 1;
    }

    public static void setBlock(BlockPos pos, Block block, World world) {
        world.setBlockState(pos, block.getDefaultState());
    }

}
