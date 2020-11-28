package me.drex.logblock.database.request;

import java.util.ArrayList;
import java.util.List;

public class Requests<K> {

    private int maxSize;
    private int trackedSize = 0;
    private List<K> output = new ArrayList<>();

    public Requests(int size) {
        this.maxSize = size;
        for (int i = 0; i < size; i++) {
            output.add(null);
        }
    }

    public synchronized void complete(K k) {
        output.add(k);
        trackedSize++;
    }

    public synchronized void complete(K k, int index) {
        output.set(index, k);
        trackedSize++;
    }

    public boolean isDone() {
        return trackedSize == this.maxSize;
    }

    public List<K> getOutput() {
        return this.output;
    }

}
