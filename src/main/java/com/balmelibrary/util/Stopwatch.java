package com.balmelibrary.util;

/**
 * Minimal nanosecond stopwatch built on {@link System#nanoTime()}, used by
 * the search and sort algorithms to produce the {@code timeNs} figure the
 * project brief's {@code algorithm_runs} table and performance-experiments
 * section expect (in place of a heavier JMH benchmarking harness, which the
 * brief marks as optional).
 */
public final class Stopwatch {

    private long startNanos = -1;
    private long elapsedNanos = 0;

    /** Starts (or restarts) timing. */
    public Stopwatch start() {
        startNanos = System.nanoTime();
        return this;
    }

    /**
     * Stops timing and returns the elapsed nanoseconds since {@link #start()}.
     *
     * @throws IllegalStateException if {@link #start()} was never called
     */
    public long stop() {
        if (startNanos < 0) {
            throw new IllegalStateException("Stopwatch.stop() called before start().");
        }
        elapsedNanos = System.nanoTime() - startNanos;
        startNanos = -1;
        return elapsedNanos;
    }

    /** Returns the duration recorded by the most recent start/stop pair. */
    public long elapsedNanos() {
        return elapsedNanos;
    }
}
