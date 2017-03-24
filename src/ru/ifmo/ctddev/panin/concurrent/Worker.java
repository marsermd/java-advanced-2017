package ru.ifmo.ctddev.panin.concurrent;

import java.util.List;
import java.util.function.Function;

public class Worker<TSource, TResult> implements Runnable
{
    private List<? extends TSource> list;
    private TResult result;
    private Function<List<? extends TSource>, TResult> function;
    private boolean isFinished = false;

    public Worker(List<? extends TSource> list, Function<List<? extends TSource>, TResult> function)
    {
        this.list = list;
        this.function = function;
    }

    @Override
    public synchronized void run()
    {
        result = function.apply(list);
    }

    /**
     * Returns computation result
     * Don't call before process has finished!
     */
    public synchronized TResult getResult()
    {
        return result;
    }
}