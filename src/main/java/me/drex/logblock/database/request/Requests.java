package me.drex.logblock.database.request;

import java.util.ArrayList;
import java.util.List;

public class Requests<K> {

    private int size;
    private List<K> output = new ArrayList<>();

    public Requests(int size) {
        this.size = size;
    }

    public synchronized void complete(K k) {
        output.add(k);
    }

    public boolean isDone() {
        return output.size() == this.size;
    }

    public List<K> getOutput() {
        return this.output;
    }

}
