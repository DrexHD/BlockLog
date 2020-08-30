package me.drex.logblock.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.ArgumentUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String input = StringArgumentType.getString(context, "user");
        MutableText prefix = new LiteralText("\n-----").formatted(Formatting.WHITE).append(new LiteralText(" BlockLog ( " + input + " ) ").formatted(Formatting.DARK_AQUA)).append(new LiteralText("-----").formatted(Formatting.WHITE));
        context.getSource().sendFeedback(prefix, false);
        CompletableFuture.runAsync(() -> {
            try {
                sendLine(context, "minecraft:diamond_ore");
                sendLine(context, "minecraft:ancient_debris");
                sendLine(context, "minecraft:gold_ore");
//                sendLine(context, "minecraft:iron_ore");
                sendLine(context, "minecraft:redstone_ore");
                sendLine(context, "minecraft:lapis_ore");
                sendLine(context, "minecraft:emerald_ore");
//                sendLine(context, "minecraft:coal_ore");
                sendLine(context, "minecraft:nether_quartz_ore");
            } catch (SQLException | CommandSyntaxException e) {
                e.printStackTrace();
            }
        });
        return 1;
    }

    private static void sendLine(CommandContext<ServerCommandSource> context, String block) throws SQLException, CommandSyntaxException {
        int blockID = BlockLog.getCache().getBlock(block);
        ArrayList<String> remCriterias = new ArrayList<>();
        remCriterias.add(ArgumentUtil.parseUser(context));
        remCriterias.add("pblockid = "  + blockID);
        remCriterias.add("placed = false");

        ArrayList<String> plcCriteria = new ArrayList<>();
        plcCriteria.add(ArgumentUtil.parseUser(context));
        plcCriteria.add("blockid = "  + blockID);
        plcCriteria.add("placed = true");

        ResultSet remResultSet = DBUtil.getDataWhere(ArgumentUtil.parseQuery("", remCriterias), false);
        remResultSet.last();
        int rem = remResultSet.getRow();
        ResultSet plcResultSet = DBUtil.getDataWhere(ArgumentUtil.parseQuery("", plcCriteria), false);
        plcResultSet.last();
        int plc = plcResultSet.getRow();
        String input = StringArgumentType.getString(context, "user");
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
