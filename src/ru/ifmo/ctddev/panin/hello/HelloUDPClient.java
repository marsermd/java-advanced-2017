package ru.ifmo.ctddev.panin.hello;


import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Sends requests to server and prints responses in parallel. <br/>
 * See {@link ClientUDPTask} for more details.
 */
public class HelloUDPClient implements HelloClient
{
    /**
     * Commandline interface for HelloUDPClient <br/>
     * format: HelloUdpClient host port prefix threadCnt requestCnt
     * @param args arguments.
     */
    public static void main(String[] args)
    {
        if (args.length != 5)
        {
            System.err.println("Wrong arguments count");
            printUsage();
            return;
        }

        try
        {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threadsCnt = Integer.parseInt(args[3]);
            int requestCnt = Integer.parseInt(args[4]);
            new HelloUDPClient().start(host, port, prefix, requestCnt, threadsCnt);
        }
        catch (NumberFormatException e)
        {
            System.err.println("Failed parsing arguments");
            printUsage();
            return;
        }
    }

    private static void printUsage()
    {
        System.out.println("Usage:");
        System.out.println("HelloUdpClient host port prefix threadCnt requestCnt");
    }

    /**
     * Start Sending packets. <br/>
     * See {@link ClientUDPTask} for more details
     * @param host server's host name.
     * @param port server's listening port
     * @param prefix prefix for request
     * @param requests amount of requests per thread
     * @param threads amount ofthreads to send the requests
     */
    @Override
    public void start(String host, int port, String prefix, int requests, int threads)
    {
        InetSocketAddress serverAddress = new InetSocketAddress(host, port);
        try
        {
            ExecutorService threadPool = Executors.newFixedThreadPool(threads);
            threadPool.invokeAll(
                IntStream.range(0, threads).mapToObj(
                    threadId -> new ClientUDPTask(threadId, requests, prefix, serverAddress)
                ).collect(Collectors.toList())
            );
            // All tasks are already finished, so it is safe to shutdown pool.
            threadPool.shutdown();
        }
        catch (InterruptedException e)
        {
            System.err.println("Interrupted while executing client tasks");
            return;
        }
    }
}