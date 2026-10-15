package com.balmelibrary.algorithms;

/**
 * Immutable record of one sort algorithm run, used as the empirical-evidence
 * payload for the project brief's {@code algorithm_runs} table and
 * performance-experiments section.
 *
 * @param comparisons   number of element comparisons performed
 * @param swapsOrWrites number of array writes (insertion/merge sort) or swaps
 *                      (selection sort) performed
 * @param elapsedNanos  wall-clock time of the sort call, from {@link com.balmelibrary.util.Stopwatch}
 */
public record SortOutcome(long comparisons, long swapsOrWrites, long elapsedNanos) {
}
