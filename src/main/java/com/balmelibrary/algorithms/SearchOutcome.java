package com.balmelibrary.algorithms;

/**
 * Immutable record of one search algorithm run. Doubles as the returned
 * result (index) and as the empirical-evidence payload for the project
 * brief's {@code algorithm_runs} table and performance-experiments section.
 *
 * @param index        index of the target in the array, or -1 if not found
 * @param comparisons  number of element comparisons performed
 * @param elapsedNanos wall-clock time of the search call, from {@link com.balmelibrary.util.Stopwatch}
 */
public record SearchOutcome(int index, long comparisons, long elapsedNanos) {

    /** True if the target was found (i.e. {@link #index()} &gt;= 0). */
    public boolean found() {
        return index >= 0;
    }
}
