package ru.ifmo.ctddev.panin.concurrent;

import info.kgeorgiy.java.advanced.concurrent.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP
{
    /**
     * Executes in parallel given function on argument list
     * @param threadsCount count of treads
     * @param arguments list of arguments
     * @param function apply to sublist of arguments
     * @param reduce convert function results to one single result
     * @param <TSource> argument type
     * @param <TResult> result type
     * @return single result as if function was applied to whole list at once
     * @throws InterruptedException if interrupted {@link InterruptedException}
     */
    private <TSource, TResult> TResult parallel(int threadsCount,
                                                List<? extends TSource> arguments,
                                                Function<List<? extends TSource>, TResult> function,
                                                Function<Stream<TResult>, TResult> reduce) throws InterruptedException
    {
        Worker<TSource, TResult>[] workers = createWorkers(threadsCount, arguments, function);
        return applyWorkers(workers, reduce);
    }

    private <TSource, TResult> TResult applyWorkers(Worker<TSource, TResult>[] workers,
                                                    Function<Stream<TResult>, TResult> reduce) throws InterruptedException
    {
        Thread[] threads = new Thread[workers.length];

        for (int i = 0; i < workers.length; i++)
        {
            threads[i] = new Thread(workers[i]);
        }

        for (Thread thread : threads)
        {
            thread.start();
        }

        for (Thread thread : threads)
        {
            thread.join();
        }

        return reduce.apply(Arrays.stream(workers).map(tWorker -> tWorker.getResult()));
    }

    private <TSource, TResult> Worker<TSource, TResult>[] createWorkers(int threadsCount,
                                                                        List<? extends TSource> arguments,
                                                                        Function<List<? extends TSource>, TResult> function)
    {
        threadsCount = Math.min(threadsCount, arguments.size());
        Worker<TSource, TResult>[] workers = new Worker[threadsCount];

        int avereageBulkSize = arguments.size() / threadsCount;
        int bigBulksCount = arguments.size() % threadsCount;
        int currentPos = 0;

        for (int i = 0; i < threadsCount; i++)
        {
            int nextPos = currentPos + avereageBulkSize;
            if (i < bigBulksCount)
            {
                //we have to create bigBulksCount of bulk operations with additional element
                nextPos++;
            }
            workers[i] = new Worker<TSource, TResult>(arguments.subList(currentPos, nextPos), function);
            currentPos = nextPos;
        }

        return workers;
    }


    @Override
    public String join(int i, List<?> list) throws InterruptedException
    {
        return parallel(i, list,
            source -> source.stream().map(Object::toString).collect(Collectors.joining()),
            results -> results.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException
    {
        return parallel(i, list,
            source -> source.stream().filter(predicate).collect(Collectors.toList()),
            results -> results.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException
    {
        return parallel(i, list,
            source -> source.stream().map(function).collect(Collectors.toList()),
            results -> results.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException
    {
        return parallel(i, list,
            source -> source.stream().max(comparator).get(),
            results -> results.max(comparator).get());
    }

    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException
    {
        return maximum(i, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException
    {
        return parallel(i, list,
            source -> source.stream().allMatch(predicate),
            results -> results.allMatch(bool -> bool));
    }

    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException
    {
        return parallel(i, list,
            source -> source.stream().anyMatch(predicate),
            results -> results.anyMatch(bool -> bool));
    }
}
