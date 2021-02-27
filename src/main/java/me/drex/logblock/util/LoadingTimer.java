package me.drex.logblock.util;

import me.drex.logblock.BlockLog;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LoadingTimer {

    private static final List<Character> characters = Arrays.asList('|', '/', '-', '\\', '|', '/', '-', '\\');
    private final ServerPlayerEntity player;
    private final int wait = 50;
    private boolean stop = false;

    public LoadingTimer(ServerPlayerEntity player) {
        this.player = player;
        start();
    }

    public void start() {
        CompletableFuture.runAsync(() -> {
            int i = 0;
            while (!stop) {
                if (!BlockLog.server.getPlayerManager().getPlayerList().contains(this.player)) break;
                this.player.sendMessage(new LiteralText("Loading data " + characters.get(i % characters.size())).formatted(Formatting.GRAY), true);
                i++;
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    public void stop() {
        stop = true;
        this.player.sendMessage(new LiteralText("Loading data " + characters.get(i % characters.size())).formatted(Formatting.GRAY), true);
    }

}
