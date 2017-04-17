package ru.ifmo.ctddev.panin.hello;

import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

/**
 * Task for HelloUDPClient
 * Sends udp requests to the given host.
 */
class ClientUDPTask implements Callable<Void>
{
    private int threadId;
    private int requestsCnt;
    private String prefix;
    private InetSocketAddress serverAddress;

    /**
     * Request format is prefix + threadId + "_" + requestId <br/>
     * Where requestId is incremented
     * @param threadId threadId to use in request format.
     * @param requestsCnt count of requests to send to server
     * @param prefix prefix to use in request format
     * @param serverAddress address to send to
     */
    ClientUDPTask(int threadId, int requestsCnt, String prefix, InetSocketAddress serverAddress)
    {
        this.threadId = threadId;
        this.requestsCnt = requestsCnt;
        this.prefix = prefix;
        this.serverAddress = serverAddress;
    }

    /**
     * Execute task:<br/>
     * Send requestsCnt requests to server address, print responses to stdout
     * @return
     */
    public Void call()
    {
        try (DatagramSocket sock = new DatagramSocket())
        {
            sock.setSoTimeout(100);
            byte[] buf = new byte[sock.getReceiveBufferSize()];
            DatagramPacket response = new DatagramPacket(buf, 0, buf.length);

            for (int requestId = 0; requestId < requestsCnt; requestId++)
            {
                String requestStr = String.format("%s%d_%d", prefix, threadId, requestId);
                String responseStr = "Hello, " + requestStr;
                byte message[] = requestStr.getBytes(Util.CHARSET);
                DatagramPacket request = new DatagramPacket(message, message.length, serverAddress);

                while (true)
                {
                    try
                    {
                        sock.send(request);
                        sock.receive(response);
                        String s = new String(response.getData(), 0, response.getLength(), Util.CHARSET);
                        if (s.equals(responseStr))
                        {
                            ConcurrentLogger.log(s);
                            break;
                        }
                    }
                    catch (IOException e)
                    {
                        ConcurrentLogger.err("An IO Error occured at socket\n" + e.getMessage());
                    }
                }
            }
        }
        catch (IOException e)
        {
            ConcurrentLogger.err("Failed opening socket\n" + e.getMessage());
        }
        return null;
    }
}
