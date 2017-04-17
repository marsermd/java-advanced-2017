package ru.ifmo.ctddev.panin.hello;

import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

/**
 * Task for HelloUDPServer
 * Listens for requests and sends responses back
 */
class ServerUDPTask implements Callable<Void>
{
    private int bufferSize;
    private DatagramSocket socket;

    /**
     * Listens to socket for requests
     * And sends responses of fromat "Hello, " + request
     * @param bufferSize size of socket's buffer
     * @param socket socket to operate on
     */
    ServerUDPTask(int bufferSize, DatagramSocket socket)
    {
        this.bufferSize = bufferSize;
        this.socket = socket;
    }

    /**
     * Execute task:<br/>
     * Listen to requests and send responses.
     * @return
     * @throws Exception
     */
    @Override
    public Void call() throws Exception
    {
        try
        {
            byte[] buf = new byte[bufferSize];
            DatagramPacket request = new DatagramPacket(buf, buf.length);

            while (!Thread.interrupted())
            {
                socket.receive(request);
                String reuestAsString = new String(
                    request.getData(),
                    request.getOffset(),
                    request.getLength(),
                    Util.CHARSET
                );
                String greeting = "Hello, " + reuestAsString;
                byte message[] = greeting.getBytes(Util.CHARSET);

                DatagramPacket response = new DatagramPacket(message, message.length, request.getSocketAddress());
                socket.send(response);
            }
        }
        catch (IOException e)
        {
            ConcurrentLogger.err(e.getMessage());
        }
        return null;
    }
}
