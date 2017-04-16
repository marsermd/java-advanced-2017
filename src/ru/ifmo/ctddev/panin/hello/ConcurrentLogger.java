package ru.ifmo.ctddev.panin.hello;

public class ConcurrentLogger
{
    public static synchronized void log(String value)
    {
        System.out.println(value);
    }

    public static synchronized void err(String value)
    {
        System.err.println(value);
    }
}
