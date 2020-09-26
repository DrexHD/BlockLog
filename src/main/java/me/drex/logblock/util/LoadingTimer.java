package me.drex.logblock.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoadingTimer {

    private static final List<Character> characters = Arrays.asList('|', '/', '-', '\\', '|', '/', '-', '\\');
    private final StopWatch stopWatch = StopWatch.createStarted();
    private long cachedTime = 0;
    private final ServerPlayerEntity player;
    private Thread t;
    private boolean stop = false;

    public LoadingTimer(ServerPlayerEntity player) {
        this.player = player;
        start();
    }

    public void start() {
        t = new Thread(() -> {
            int i = 0;
            while(!stop) {
                if (cachedTime / 10 < stopWatch.getTime() / 10) {
                    this.player.sendMessage(new LiteralText("Loading data " + characters.get(i / 50 % characters.size())).formatted(Formatting.YELLOW)
                            /*.append(new LiteralText(" (" + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)").formatted(Formatting.DARK_GRAY))*/, true);
                    i++;
                    cachedTime = stopWatch.getTime();
                }
            }
        });
        t.start();
    }

    public void stop() {
        stop = true;
    }

}
