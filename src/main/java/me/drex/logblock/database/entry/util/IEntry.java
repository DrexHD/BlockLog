package me.drex.logblock.database.entry.util;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public interface IEntry {

    public void saveAsync(Consumer<Integer> action) throws ExecutionException, InterruptedException;

}
