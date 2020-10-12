package me.drex.logblock.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.ArgumentUtil;
import me.drex.logblock.util.LoadingTimer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class InfoCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> player = ArgumentUtil.getUser();
        player.executes(InfoCommand::execute);
        LiteralArgumentBuilder<ServerCommandSource> info = LiteralArgumentBuilder.literal("info");
        info.then(player);
        info.requires(source -> BlockLog.hasPermission(source, "blocklog.info"));
        command.then(info);
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            try {
                sendInfo(context);
            } catch (SQLException | CommandSyntaxException e) {
                context.getSource().sendError(new LiteralText("Error: " + e.getMessage()));
                e.printStackTrace();
            }
        });
        return 1;
    }


    private static void sendInfo(CommandContext<ServerCommandSource> context) throws SQLException, CommandSyntaxException {
        List<String> blocks = Arrays.asList("minecraft:diamond_ore", "minecraft:ancient_debris", "minecraft:gold_ore", "minecraft:redstone_ore", "minecraft:lapis_ore", "minecraft:emerald_ore", "minecraft:nether_quartz_ore");
        Map<Integer, Integer> blocksPlaced = new HashMap<>();
        Map<Integer, Integer> blocksDestroyed = new HashMap<>();
        ArrayList<String> criteria = new ArrayList<>();
        criteria.add(ArgumentUtil.parseUser(context)+ " (");
        ArrayList<String> blockCriteria = new ArrayList<>();
        for (String block : blocks) {
            int blockID = BlockLog.getCache().getBlock(block);
            blocksPlaced.put(blockID, 0);
            blocksDestroyed.put(blockID, 0);
            blockCriteria.add("pblockid = " + blockID + " OR blockid = " + blockID);
        }
        criteria.add(ArgumentUtil.formatQuery("", blockCriteria, "OR"));
        LoadingTimer lt = new LoadingTimer(context.getSource().getPlayer());
        ResultSet rs = DBUtil.getDataWhere(ArgumentUtil.formatQuery("", criteria, "AND") + ")", false);
        lt.stop();
        while (rs.next()) {
            {
                int blockID = rs.getInt("blockid");
                if (blocksPlaced.containsKey(blockID)) {
                    int i = blocksPlaced.get(blockID);
                    i++;
                    blocksPlaced.put(blockID, i);
                }
            }
            {
                int blockID = rs.getInt("pblockid");
                if (blocksDestroyed.containsKey(blockID)) {
                    int i = blocksDestroyed.get(blockID);
                    i++;
                    blocksDestroyed.put(blockID, i);
                }
            }

        }
        String input = StringArgumentType.getString(context, "user");
        MutableText prefix = new LiteralText("\n-----").formatted(Formatting.WHITE).append(new LiteralText(" BlockLog ( " + input + " ) ").formatted(Formatting.DARK_AQUA)).append(new LiteralText("-----").formatted(Formatting.WHITE));
        context.getSource().sendFeedback(prefix, false);
        for (String block : blocks) {
            int blockID = BlockLog.getCache().getBlock(block);
            int plc = blocksPlaced.get(blockID);
            int rem = blocksDestroyed.get(blockID);
            MutableText text2 = new LiteralText("")
                    .append(new LiteralText("* " + block.split(":")[1].replace("_ore", "") + ": ").formatted(Formatting.GRAY))
                    .append(String.valueOf((rem-plc))).formatted(Formatting.YELLOW)
                    .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("* Placed: ").formatted(Formatting.GRAY)
                            .append(new LiteralText(String.valueOf(plc)).formatted(Formatting.GREEN))
                            .append(new LiteralText("\n* Mined: ").formatted(Formatting.GRAY))
                            .append(new LiteralText(String.valueOf(rem)).formatted(Formatting.RED))))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bl lookup " + input + " -global " + block.split(":")[1] + " -always")));
            context.getSource().sendFeedback(text2, false);
        }
    }


}
