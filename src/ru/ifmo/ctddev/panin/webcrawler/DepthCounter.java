package ru.ifmo.ctddev.panin.webcrawler;

class DepthCounter
{
    private final int remainingDepth;

    private DepthCounter(int depth)
    {
        this.remainingDepth = depth;
    }

    public static DepthCounter newCounter(int depth)
    {
        return new DepthCounter(depth);
    }

    public DepthCounter next()
    {
        if (isFinished())
        {
            throw new IllegalStateException("can't get deeper!");
        }
        return new DepthCounter(remainingDepth - 1);
    }

    public boolean isFinished()
    {
        return remainingDepth == 1;
    }
}
