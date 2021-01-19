package me.drex.logblock.database.request;

import me.drex.logblock.BlockLog;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Requests<K> {

    private int maxSize;
    private int trackedSize = 0;
    private long start = 0;
    private List<K> output = new CopyOnWriteArrayList<>();

    public Requests(int size) {
        this.maxSize = size;
    }

    public synchronized void complete(K k) {
        output.add(k);
        trackedSize++;
    }

    public synchronized void complete(K k, int index) {
        if (index > output.size() -1) {
            output.add(null);
            complete(k, index);
        } else {
            output.set(index, k);
            trackedSize++;
        }
    }

    public boolean isNotDone() {
        return trackedSize != this.maxSize;
    }

    public List<K> getOutput() {
        return this.output;
    }

    public boolean block(int timeout) {
        start = new Date().getTime();
        while (isNotDone()) {
            if (start + timeout < new Date().getTime()) {
                BlockLog.getLogger().warn("Request only had " + this.trackedSize + " / " + this.maxSize + " requests done after " + timeout + "ms! " + Arrays.toString(this.getOutput().toArray()));
                return false;
            }
        }
        return true;
    }

}
