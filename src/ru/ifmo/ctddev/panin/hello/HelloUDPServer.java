package ru.ifmo.ctddev.panin.hello;


import info.kgeorgiy.java.advanced.hello.HelloServer;
import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Listens to requests from client sends back responses in parallel. <br/>
 * See {@link ServerUDPTask} for more details.
 */
public class HelloUDPServer implements HelloServer
{
    private boolean isRunning = false;
    private ExecutorService threadPool;
    private DatagramSocket socket;

    /**
     * Commandline interface for HelloUDPServer <br/>
     * format: HelloUDPServer port threadsCnt
     * @param args arguments.
     */
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Not enough arguments");
            printUsage();
        }

        try
        {
            int port = Integer.parseInt(args[0]);
            int threadsCnt = Integer.parseInt(args[1]);

            new HelloUDPServer().start(port, threadsCnt);
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
        System.out.println("HelloUDPServer port threadCnt");
    }

    /**
     * Begin listening to requests. <br/>
     * For more details see {@link ServerUDPTask}
     * @param port port to listen on
     * @param threads threads amount to parallelize listining task
     */
    @Override
    public synchronized void start(int port, int threads)
    {
        if (isRunning)
        {
            throw new IllegalStateException("already running!");
        }
        isRunning = true;

        try
        {
            socket = new DatagramSocket(port);
            int bufferSize = socket.getReceiveBufferSize();

            threadPool = Executors.newFixedThreadPool(threads);

            for (int i = 0; i < threads; i++)
            {
                threadPool.submit(new ServerUDPTask(bufferSize, socket));
            }
        }
        catch (SocketException e)
        {
            System.err.println(e.getMessage());
            return;
        }
    }

    /**
     * Finish listening to requests
     */
    @Override
    public synchronized void close()
    {
        if (!isRunning)
        {
            throw new IllegalStateException("already stopped!");
        }
        threadPool.shutdownNow();
        socket.close();
        isRunning = false;
    }

}