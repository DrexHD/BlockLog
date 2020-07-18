package me.drex.logblock.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.logblock.LogBlockMod;
import me.drex.logblock.database.DBCache;
import me.drex.logblock.database.DBUtil;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateArgumentType;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collection;


public class LookupCommand {

    private static DecimalFormat df2 = new DecimalFormat("#.##");

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> lookup = LiteralArgumentBuilder.literal("lookup");
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
        RequiredArgumentBuilder<ServerCommandSource, Integer> radius = RequiredArgumentBuilder.argument("radius", IntegerArgumentType.integer(1, 500));
        RequiredArgumentBuilder<ServerCommandSource, BlockStateArgument> block = RequiredArgumentBuilder.argument("block", BlockStateArgumentType.blockState());
        radius.executes(LookupCommand::lookup);
//        radius.then(block);
//        block.then(radius);
        player.then(radius);
//        player.then(block);
        lookup.then(player);
        command.then(lookup);
    }

    private static int lookup(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GameProfile gameProfile = validateGameProfile(GameProfileArgumentType.getProfileArgument(context, "player"));
        int radius = IntegerArgumentType.getInteger(context, "radius");
//        BlockState blockState = BlockStateArgumentType.getBlockState(context, "block").getBlockState();
        Vec3d pos = context.getSource().getPosition();

        String radiusCriteria = "x BETWEEN " + (int) (pos.getX() - radius / 2) + " AND " + (int) (pos.getX() + radius) + " AND " + " y BETWEEN " + 0 + " AND " + 256 + " AND " + " z BETWEEN " + (int) (pos.getZ() - radius) + " AND " + (int) (pos.getZ() + radius / 2);

        DBCache dbCache = LogBlockMod.getCache();
        try {
            ResultSet resultSet = DBUtil.getDataWhere(radiusCriteria);
            while (resultSet.next()) {
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");
                boolean placed = resultSet.getBoolean("placed");
                String block = dbCache.getBlock(resultSet.getInt(placed ? "blockid" : "pblockid"));
                long time = resultSet.getLong("time");
                long dateDiff = System.currentTimeMillis() - time;
                Text text = new LiteralText(convertSecondsToString((dateDiff/1000)) + "h ago").formatted(Formatting.GRAY)
                        .append(new LiteralText(" - ").formatted(Formatting.WHITE))
                        .append(new LiteralText(gameProfile.getName()).formatted(Formatting.AQUA))
                        .append(new LiteralText(placed ? " placed " : " removed ").formatted(Formatting.WHITE))
                        .append(new LiteralText(block.split(":")[1])).formatted(Formatting.AQUA)
                        .append(new LiteralText(" at ").formatted(Formatting.WHITE))
                        .append(new LiteralText(x + " ")).formatted(Formatting.GRAY)
                        .append(new LiteralText(y + " ")).formatted(Formatting.GRAY)
                        .append(new LiteralText(z + " ")).formatted(Formatting.GRAY);
                context.getSource().sendFeedback(text, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SimpleCommandExceptionType(new LiteralText("SQL Exception " + e.getMessage())).create();
        }
        return 1;

    }


    private static GameProfile validateGameProfile(Collection<GameProfile> profiles) throws CommandSyntaxException {
        if (profiles.size() > 1) {
            throw new SimpleCommandExceptionType(new LiteralText("Playerargument has to many players (only 1 allowed)")).create();
        } else if (profiles.size() == 0) {
            throw new SimpleCommandExceptionType(new LiteralText("Playerargument doesnt include a valid player")).create();
        } else {
            for (GameProfile profile : profiles) {
                return profile;
            }
        }
        return null;
    }


    private static String convertSecondsToString(long seconds) {
        double hours = (double) seconds / 3600;
        return df2.format(hours);
    }


}
