package me.drex.logblock.util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.entry.BlockEntry;
import me.drex.logblock.database.entry.DimensionEntry;
import me.drex.logblock.database.entry.EntityEntry;
import me.drex.logblock.database.entry.util.CacheEntry;
import me.drex.logblock.database.request.Requests;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MessageUtil {

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static HashMap<UUID, ResultSet> resultCache = new HashMap<>();
    private static HashMap<UUID, Text> titleCache = new HashMap<>();
    private static String uuidRegex = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}";

    public static void send(ServerCommandSource source, ResultSet resultSet, Text title) throws CommandSyntaxException {
        UUID uuid = source.getPlayer().getUuid();
        resultCache.put(uuid, resultSet);
        titleCache.put(uuid, title);
        sendPage(source, 5, 0);
    }

    /**
     * @param source  of the player who's cache should be pulled
     * @param entries which should be displayed on each page
     * @param page    number to display (starts at 0)
     */
    public static void sendPage(ServerCommandSource source, int entries, int page) throws CommandSyntaxException {
        UUID uuid = source.getPlayer().getUuid();
        ResultSet resultSet = resultCache.get(uuid);
        MutableText prefix = new LiteralText("\n-----").formatted(Formatting.WHITE).append(new LiteralText(" BlockLog ").formatted(Formatting.DARK_AQUA)).append(new LiteralText("----- ").formatted(Formatting.WHITE));
        Text title = prefix.append(titleCache.get(uuid));
        if (!title.equals(new LiteralText(""))) source.sendFeedback(title, false);
        if (resultSet == null) {
            source.sendError(new LiteralText("Couldnt find any search history!"));
            return;
        }
        try {
            int results = 0;
            //Calculate resultSize
            resultSet.last();
            int size = resultSet.getRow();
            resultSet.beforeFirst();
            if (size == 0) {
                source.sendError(new LiteralText("No Entries!"));
                return;
            }
            int from = entries * page; //This is the entry number on which the page should startR
            int to = (entries * (page + 1)) - 1;
            int maxPage = (size / entries);
            if (page < 0 || page > maxPage) {
                source.sendError(new LiteralText("Invalid page!"));
                return;
            }
            while (resultSet.next()) {
                if (results >= from && results <= to) {
                    int id = resultSet.getInt(HistoryColumn.ID.toString());
                    int x = resultSet.getInt(HistoryColumn.XPOS.toString());
                    int y = resultSet.getInt(HistoryColumn.YPOS.toString());
                    int z = resultSet.getInt(HistoryColumn.ZPOS.toString());
                    boolean placed = resultSet.getBoolean(HistoryColumn.PLACED.toString());
                    boolean undone = resultSet.getBoolean(HistoryColumn.UNDONE.toString());
                    Requests<CacheEntry<?>> r = new Requests<>(2);
                    EntityEntry.of(EntityEntry.class, resultSet.getInt(HistoryColumn.ENTITYID.toString()), "", entry -> r.complete(entry,0));
                    BlockEntry.of(BlockEntry.class, resultSet.getInt(placed ? HistoryColumn.BLOCKID.toString() : HistoryColumn.PBLOCKID.toString()), "",entry -> r.complete(entry,1));
                    long time = resultSet.getLong(HistoryColumn.TIME.toString());
                    long dateDiff = System.currentTimeMillis() - time;
                    if (r.block(5000)) {
                        String cause;
                        List<CacheEntry<?>> output = r.getOutput();
                        String entity = ((String)output.get(0).getValue());
                        if (entity.matches(uuidRegex)) {
                            GameProfile profile = source.getMinecraftServer().getUserCache().getByUuid(UUID.fromString(entity));
                            cause = profile == null ? entity : profile.getName();
                        } else {
                            cause = entity;
                        }
                        //TODO: Find a way to figure out which result is which (they are currently sorted by completion time)
                        MutableText text = new LiteralText(convertmillisecondstoString((dateDiff)) + " ago").formatted(Formatting.GRAY)
                                .append(new LiteralText(" - ").formatted(Formatting.WHITE))
                                .append(new LiteralText(cause).formatted(Formatting.AQUA))
                                .append(new LiteralText(placed ? " placed " : " removed ").formatted(placed ? Formatting.GREEN : Formatting.RED))
                                .append(new LiteralText(((String)output.get(1).getValue()).split(":")[1])).formatted(Formatting.AQUA)
                                .append(new LiteralText(" at ").formatted(Formatting.WHITE))
                                .append(new LiteralText(x + " ")).formatted(Formatting.GRAY)
                                .append(new LiteralText(y + " ")).formatted(Formatting.GRAY)
                                .append(new LiteralText(z + " ")).formatted(Formatting.GRAY);
                        if (undone) text.formatted(Formatting.ITALIC);
                        text.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bl teleport " + id)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to teleport!").formatted(Formatting.GREEN))));
                        source.sendFeedback(text, false);
                    } else {
                        throw new SimpleCommandExceptionType(new LiteralText("Couldn't retrieve all entries, aborting")).create();
                    }

                }
                results++;
            }
            MutableText text = new LiteralText("");
            if (page > 0) {
                text.append(new LiteralText("||------------").formatted(Formatting.WHITE))
                        .append(new LiteralText("<<-- ").formatted(Formatting.GRAY).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bl page " + (page - 1))).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click!").formatted(Formatting.GREEN)))));
            } else {
                text.append(new LiteralText("||---------------- ").formatted(Formatting.WHITE));
            }
            text.append(new LiteralText((page + 1) + " / " + (maxPage + 1)).formatted(Formatting.GRAY));
            if (page < maxPage) {
                text.append(new LiteralText(" -->>").formatted(Formatting.GRAY).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bl page " + (page + 1))).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click!").formatted(Formatting.GREEN)))))
                        .append(new LiteralText("------------||").formatted(Formatting.WHITE));
            } else {
                text.append(new LiteralText(" ----------------||").formatted(Formatting.WHITE));

            }
            source.sendFeedback(text, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String convertmillisecondstoString(double time) {
        if (time >= 60000) {
            time /= 1000;
            if (time >= 3600) {
                if (time >= 86400) {
                    if (time >= 604800) {
                        if (time >= 2630016) {
                            if (time >= 31556952) {
                                return df2.format(time / 31556952) + "y";
                            } else {
                                return df2.format(time / 2630016) + "mo";
                            }
                        } else {
                            return df2.format(time / 604800) + "w";
                        }
                    } else {
                        return df2.format(time / 86400) + "d";
                    }
                } else {
                    return df2.format(time / 3600) + "h";
                }
            } else {
                return df2.format(time / 60) + "m";
            }
        } else {
            return df2.format(time / 1000) + "s";
        }
    }

}
