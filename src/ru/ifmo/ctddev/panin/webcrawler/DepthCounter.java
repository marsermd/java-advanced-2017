package ru.ifmo.ctddev.panin.webcrawler;

/**
 * Simple reqursion depth limiter. <br>
 * Call {@link #next()} every time you get deeper in recursion. <br>
 * Call {@link #isFinished()} to check if you should stop recursion. <br>
 */
class DepthCounter
{
    private final int remainingDepth;

    private DepthCounter(int depth)
    {
        this.remainingDepth = depth;
    }

    /**
     * Create new {@link DepthCounter} with given length
     * @param depth maximum depth
     * @return new DepthCounter
     */
    static DepthCounter newCounter(int depth)
    {
        return new DepthCounter(depth);
    }

    /**
     * Call every time you get deeper in recursion.
     * @return new DepthCounter with decremented depth.
     * @throws IllegalStateException if reached bottom of recursion
     */
    public DepthCounter next()
    {
        if (isFinished())
        {
            throw new IllegalStateException("can't get deeper!");
        }
        return new DepthCounter(remainingDepth - 1);
    }

    /**
     * @return true if got to the recursion bottom(depth == 1) and you should stop getting deeper.
     */
    boolean isFinished()
    {
        return remainingDepth == 1;
    }
}
