package ru.ifmo.ctddev.panin.concurrent;

import java.util.List;
import java.util.function.Function;

/**
 * Call {@link #reset()} before executing worker second time
 */
class Worker<TSource, TResult> implements Runnable
{
    private List<? extends TSource> arguments;
    private TResult result;
    private Function<List<? extends TSource>, TResult> function;
    private boolean isFinished = false;

    /**
     * Worker applies function to list.
     * Call {@link #getResult()} to get result after the thread has finished.
     * @param arguments list of arguments
     * @param function function to apply to arguments
     */
    public Worker(List<? extends TSource> arguments, Function<List<? extends TSource>, TResult> function)
    {
        this.arguments = arguments;
        this.function = function;
    }


    /***
     * resets Worker to it's unlaunched state
     */
    public void reset()
    {
        result = null;
        isFinished = false;
    }

    /**
     *
     * @throws WorkerInvokationException if worker already was launched and wasn't reset after last time
     */
    @Override
    public synchronized void run()
    {
        if (isFinished)
        {
            throw new WorkerInvokationException("Launching finished worker again! Call reset() to emphasize your intent");
        }

        result = function.apply(arguments);

        isFinished = true;
    }

    /**
     * Returns computation result
     * @throws WorkerInvokationException if result is not calculated yet,
     * @return computation result
     */
    public synchronized TResult getResult()
    {
        if (!isFinished)
        {
            throw new WorkerInvokationException("Trying to get result before it's ready!");
        }
        return result;
    }

    public static class WorkerInvokationException extends RuntimeException
    {
        public WorkerInvokationException(String reason)
        {
            super(reason);
        }
    }
}