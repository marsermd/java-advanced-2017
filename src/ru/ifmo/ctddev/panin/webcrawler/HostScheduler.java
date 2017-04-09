package ru.ifmo.ctddev.panin.webcrawler;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * Blocking scheduler that keeps limits maximum of simultaniously running threads on the same host. <br>
 * Call {@link #submitOrScheduleDownload} to add task <br>
 * Call {@link #onFinishedDownloadTask} when task is finished
 */
class HostScheduler
{
    /**
     * Map from host to count of download workers on it.
     */
    private final HashMap<String, Integer> downloadTasksCnt = new HashMap<String, Integer>()
    {
        @Override
        public Integer get(Object key)
        {
            if (!containsKey(key))
            {
                put((String)key, 0);
            }
            return super.get(key);
        }
    };

    /**
     * Map from host to queue of download tasks that still have to be executed on it, but can't be executed right now
     */
    private final HashMap<String, Queue<Runnable>> delayedDownloadTasks = new HashMap<String, Queue<Runnable>>()
    {
        @Override
        public Queue<Runnable> get(Object key)
        {
            if (!containsKey(key))
            {
                put((String)key, new ArrayDeque<>());
            }
            return super.get(key);
        }
    };

    private final int hostDownloadLimit;

    HostScheduler(int hostDownloadLimit)
    {
        this.hostDownloadLimit = hostDownloadLimit;
    }

    /**
     * Submit download task to the execution servers if limit is not exceeded, schedule for later download otherwise.
     * Always call {@see onFinishedDownloadTask} when task is finished to submit next download tasks
     * @param hostName hostname to download from
     * @param service executor service
     * @param task download task
     */
    synchronized void submitOrScheduleDownload(String hostName, ExecutorService service, Runnable task)
    {
        if (downloadTasksCnt.get(hostName) >= hostDownloadLimit)
        {
            delayedDownloadTasks.get(hostName).add(task);
        }
        else
        {
            downloadTasksCnt.compute(hostName, (key, value) -> {return value + 1;});
            service.submit(task);
        }
    }

    /**
     * Call this from the download task when it is finished.
     * @param hostName hostname associated with finished task
     * @param service executor service
     */
    synchronized void onFinishedDownloadTask(String hostName, ExecutorService service)
    {
        Queue<Runnable> queue = delayedDownloadTasks.get(hostName);

        if (queue != null)
        {
            boolean taskSubmited = false;

            if (!queue.isEmpty())
            {
                service.submit(queue.poll());
                taskSubmited = true;
            }

            if (!taskSubmited)
            {
                downloadTasksCnt.compute(hostName, (key, value) -> {return value - 1;});
            }
        }
    }
}
