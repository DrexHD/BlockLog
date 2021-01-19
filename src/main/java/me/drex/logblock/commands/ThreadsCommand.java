package me.drex.logblock.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import static java.lang.Thread.State;
import static java.lang.Thread.getAllStackTraces;

public class ThreadsCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> info = LiteralArgumentBuilder.literal("info");
        info.executes(ctx -> execute(ctx, true));
        LiteralArgumentBuilder<ServerCommandSource> threads = LiteralArgumentBuilder.literal("threads");
        threads.then(info);
        threads.executes(ctx -> execute(ctx, false));
        command.then(threads);
    }

    public static int execute(CommandContext<ServerCommandSource> ctx, boolean showNotRunning) {
        MutableText text = new LiteralText("Threads (").formatted(Formatting.YELLOW)
                .append(new LiteralText(String.valueOf((int) getAllStackTraces().keySet().stream().filter(thread -> thread.getName().startsWith("BlockLog")).count()))
                        .formatted(Formatting.GOLD))
                .append(new LiteralText(")\n")
                        .formatted(Formatting.YELLOW));
        for (Thread thread : getAllStackTraces().keySet()) {
            if (thread.getName().startsWith("BlockLog") && (thread.getState() == State.RUNNABLE || showNotRunning)) {
                text.append(new LiteralText("\n" + thread.getName()).formatted(Formatting.AQUA))
                        .append(new LiteralText(" " + thread.getState().toString()).formatted(Formatting.GRAY));
                if (thread.getStackTrace().length > 0) {
                    MutableText hover = new LiteralText("");
                    for (StackTraceElement stackTraceElement : thread.getStackTrace()) {
                        hover.append(new LiteralText(stackTraceElement.toString() + "\n").formatted(Formatting.YELLOW));
                    }
                    StackTraceElement stackTraceElement = thread.getStackTrace()[0];
                    text.append(new LiteralText(" " + stackTraceElement.toString()).formatted(Formatting.GREEN))
                            .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
                }
            }
        }
        ctx.getSource().sendFeedback(text, false);
        return 1;
    }

}
