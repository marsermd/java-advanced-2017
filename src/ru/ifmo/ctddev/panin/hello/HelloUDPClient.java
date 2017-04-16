package ru.ifmo.ctddev.panin.hello;


import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient
{
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
            int threadCnt = Integer.parseInt(args[3]);
            int requestCnt = Integer.parseInt(args[4]);
            new HelloUDPClient().start(host, port, prefix, requestCnt, threadCnt);
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
            threadPool.shutdown();
        }
        catch (InterruptedException e)
        {
            System.err.println("Interrupted while executing client tasks");
            return;
        }
    }
}