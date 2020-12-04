package me.drex.logblock.database.request;

import java.util.ArrayList;
import java.util.List;

public class Requests<K> {

    private int maxSize;
    private int trackedSize = 0;
    private List<K> output = new ArrayList<>();

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

    public boolean isDone() {
        return trackedSize == this.maxSize;
    }

    public List<K> getOutput() {
        return this.output;
    }

}
