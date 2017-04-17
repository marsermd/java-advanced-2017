package ru.ifmo.ctddev.panin.hello;

/**
 * This class provides an blocking way to print to stdout.
 */
class ConcurrentLogger
{
    /**
     * @param value print value to stdout
     */
    static void log(String value)
    {
        synchronized (System.out)
        {
            System.out.println(value);
        }
    }

    /**
     * @param value print value to stderr
     */
    static void err(String value)
    {
        synchronized (System.err)
        {
            System.err.println(value);
        }
    }
}
