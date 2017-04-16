package ru.ifmo.ctddev.panin.hello;

import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

class ServerUDPTask implements Callable<Void>
{
    private int bufferSize;
    private DatagramSocket socket;

    ServerUDPTask(int bufferSize, DatagramSocket socket)
    {
        this.bufferSize = bufferSize;
        this.socket = socket;
    }

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
                String greeting = new String(
                    request.getData(),
                    request.getOffset(),
                    request.getLength(), Util.CHARSET
                );
                byte message[] = ("Hello, " + greeting).getBytes(Util.CHARSET);

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
