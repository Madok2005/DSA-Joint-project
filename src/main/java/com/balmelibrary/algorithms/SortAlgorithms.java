package com.balmelibrary.algorithms;

import com.balmelibrary.structures.DynamicArray;
import com.balmelibrary.util.Stopwatch;

import java.util.Comparator;
import java.util.Objects;

/**
 * Sort algorithms implemented from first principles for Module M4 of the
 * Ghana Smart Service Operations Optimizer project (Balme Library dataset).
 *
 * <p>No {@code java.util.Arrays.sort} or {@code Collections.sort} is used
 * anywhere in this class. All three sorts operate generically via a
 * {@link Comparator}&lt;T&gt;, so the same code sorts {@code LibraryLocation}
 * (e.g. by name for a directory listing), {@code RoadSegment} (e.g. by
 * distance for route planning), {@code ServiceRequestRecord} (e.g. by
 * urgency for dispatch) or {@code LibraryResource} records without any
 * modification -- only the comparator passed in changes.</p>
 *
 * <p><b>Shared precondition:</b> {@code array} and {@code comparator} must
 * not be {@code null}. No precondition is placed on the initial ordering of
 * {@code array} -- establishing a sorted order is exactly what these methods
 * do. {@link SearchAlgorithms#binarySearch}, by contrast, <em>does</em>
 * require its input to already be sorted -- see that class for the stated
 * precondition and its test.</p>
 *
 * <p><b>Connected to the Data Structure Developer's module.</b> Every method
 * has two public overloads: one over a plain {@code T[]} (unchanged from the
 * original Algorithm Developer drop), and one over {@link DynamicArray}&lt;T&gt;,
 * the Data Structure Developer's from-scratch resizable array. Both overloads
 * run the exact same in-place comparison/swap/write logic -- they are thin
 * adapters over a single private {@link Indexed} core, so there is only one
 * implementation of each algorithm regardless of which array-like structure a
 * caller happens to be holding.</p>
 */
public final class SortAlgorithms {

    private SortAlgorithms() {
    }

    // ------------------------------------------------------------------
    // Indexed<T>: the shared seam between "raw T[]" and "DynamicArray<T>"
    // ------------------------------------------------------------------

    /**
     * Minimal read/write/size view over an array-like structure. Unlike
     * {@code SearchAlgorithms.Indexed}, sorting also needs {@code set(i, v)}
     * since every algorithm here reorders elements in place. Not part of the
     * public API: callers always work with {@code T[]} or
     * {@link DynamicArray}&lt;T&gt; directly.
     */
    private interface Indexed<T> {
        int size();

        T get(int index);

        void set(int index, T value);
    }

    private static <T> Indexed<T> indexed(T[] array) {
        return new Indexed<T>() {
            public int size() {
                return array.length;
            }

            public T get(int index) {
                return array[index];
            }

            public void set(int index, T value) {
                array[index] = value;
            }
        };
    }

    private static <T> Indexed<T> indexed(DynamicArray<T> array) {
        return new Indexed<T>() {
            public int size() {
                return array.size();
            }

            public T get(int index) {
                return array.get(index);
            }

            public void set(int index, T value) {
                array.set(index, value);
            }
        };
    }

    // ------------------------------------------------------------------
    // Insertion sort
    // ------------------------------------------------------------------

    /**
     * In-place insertion sort over a plain array. Stable: elements that
     * compare equal keep their original relative order (verified by
     * {@code SortAlgorithmsTest#isStableForEqualKeys}).
     *
     * <p><b>Complexity:</b> O(n^2) worst case and average case, O(n) best
     * case (already non-descending input, since the inner while-loop body
     * never executes). O(1) extra space.</p>
     *
     * @throws NullPointerException if {@code array} or {@code comparator} is {@code null}
     */
    public static <T> SortOutcome insertionSort(T[] array, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return insertionSortCore(indexed(array), comparator);
    }

    /**
     * {@link DynamicArray} counterpart of {@link #insertionSort(Object[], Comparator)}.
     * Identical algorithm and evidence payload; only the backing structure differs.
     *
     * @throws NullPointerException if {@code array} or {@code comparator} is {@code null}
     */
    public static <T> SortOutcome insertionSort(DynamicArray<T> array, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return insertionSortCore(indexed(array), comparator);
    }

    private static <T> SortOutcome insertionSortCore(Indexed<T> array, Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "comparator must not be null");

        Stopwatch stopwatch = new Stopwatch().start();
        long comparisons = 0;
        long writes = 0;

        for (int i = 1; i < array.size(); i++) {
            T key = array.get(i);
            int j = i - 1;
            while (j >= 0) {
                comparisons++;
                if (comparator.compare(array.get(j), key) > 0) {
                    array.set(j + 1, array.get(j));
                    writes++;
                    j--;
                } else {
                    break;
                }
            }
            array.set(j + 1, key);
            writes++;
        }

        long elapsed = stopwatch.stop();
        return new SortOutcome(comparisons, writes, elapsed);
    }

    // ------------------------------------------------------------------
    // Selection sort
    // ------------------------------------------------------------------

    /**
     * In-place selection sort over a plain array. Not stable: this
     * straightforward swap-based version can reorder elements that compare
     * equal.
     *
     * <p><b>Complexity:</b> O(n^2) comparisons in every case (the scan for
     * the minimum runs to completion regardless of input order); at most
     * O(n) swaps, which makes selection sort attractive when writes are far
     * more expensive than comparisons. O(1) extra space.</p>
     *
     * @throws NullPointerException if {@code array} or {@code comparator} is {@code null}
     */
    public static <T> SortOutcome selectionSort(T[] array, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return selectionSortCore(indexed(array), comparator);
    }

    /**
     * {@link DynamicArray} counterpart of {@link #selectionSort(Object[], Comparator)}.
     *
     * @throws NullPointerException if {@code array} or {@code comparator} is {@code null}
     */
    public static <T> SortOutcome selectionSort(DynamicArray<T> array, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return selectionSortCore(indexed(array), comparator);
    }

    private static <T> SortOutcome selectionSortCore(Indexed<T> array, Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "comparator must not be null");

        Stopwatch stopwatch = new Stopwatch().start();
        long comparisons = 0;
        long swaps = 0;

        int n = array.size();
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                comparisons++;
                if (comparator.compare(array.get(j), array.get(minIndex)) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                swap(array, i, minIndex);
                swaps++;
            }
        }

        long elapsed = stopwatch.stop();
        return new SortOutcome(comparisons, swaps, elapsed);
    }

    // ------------------------------------------------------------------
    // Merge sort
    // ------------------------------------------------------------------

    /**
     * Top-down (recursive divide-and-conquer) merge sort over a plain array.
     * Stable: when {@code comparator.compare(a, b) == 0}, the merge step
     * always takes the left-half element first (verified by
     * {@code SortAlgorithmsTest#isStableForEqualKeys}).
     *
     * <p><b>Complexity:</b> O(n log n) in every case (worst, average and
     * best). O(n) extra space for the merge buffer.</p>
     *
     * @throws NullPointerException if {@code array} or {@code comparator} is {@code null}
     */
    public static <T> SortOutcome mergeSort(T[] array, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return mergeSortCore(indexed(array), comparator);
    }

    /**
     * {@link DynamicArray} counterpart of {@link #mergeSort(Object[], Comparator)}.
     * Same O(n log n) / O(n)-extra-space guarantees; {@link DynamicArray#get}
     * and {@code set} are both O(1), so routing the merge through them costs
     * nothing asymptotically.
     *
     * @throws NullPointerException if {@code array} or {@code comparator} is {@code null}
     */
    public static <T> SortOutcome mergeSort(DynamicArray<T> array, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return mergeSortCore(indexed(array), comparator);
    }

    private static <T> SortOutcome mergeSortCore(Indexed<T> array, Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "comparator must not be null");

        Stopwatch stopwatch = new Stopwatch().start();
        long[] comparisons = {0};
        long[] writes = {0};

        if (array.size() > 1) {
            Object[] buffer = new Object[array.size()];
            mergeSortRecursive(array, buffer, 0, array.size() - 1, comparator, comparisons, writes);
        }

        long elapsed = stopwatch.stop();
        return new SortOutcome(comparisons[0], writes[0], elapsed);
    }

    private static <T> void mergeSortRecursive(Indexed<T> array, Object[] buffer, int left, int right,
                                                Comparator<T> comparator, long[] comparisons, long[] writes) {
        if (left >= right) {
            return; // 0 or 1 elements: already sorted
        }
        int mid = left + (right - left) / 2;
        mergeSortRecursive(array, buffer, left, mid, comparator, comparisons, writes);
        mergeSortRecursive(array, buffer, mid + 1, right, comparator, comparisons, writes);
        merge(array, buffer, left, mid, right, comparator, comparisons, writes);
    }

    /** Merges the two already-sorted runs {@code [left, mid]} and {@code [mid+1, right]}. */
    @SuppressWarnings("unchecked")
    private static <T> void merge(Indexed<T> array, Object[] buffer, int left, int mid, int right,
                                   Comparator<T> comparator, long[] comparisons, long[] writes) {
        for (int k = left; k <= right; k++) {
            buffer[k] = array.get(k);
        }

        int i = left;      // cursor into the left run
        int j = mid + 1;   // cursor into the right run
        int k = left;      // write cursor into array

        while (i <= mid && j <= right) {
            comparisons[0]++;
            if (comparator.compare((T) buffer[i], (T) buffer[j]) <= 0) { // <= keeps it stable
                array.set(k++, (T) buffer[i++]);
            } else {
                array.set(k++, (T) buffer[j++]);
            }
            writes[0]++;
        }
        while (i <= mid) {
            array.set(k++, (T) buffer[i++]);
            writes[0]++;
        }
        while (j <= right) {
            array.set(k++, (T) buffer[j++]);
            writes[0]++;
        }
    }

    private static <T> void swap(Indexed<T> array, int a, int b) {
        T tmp = array.get(a);
        array.set(a, array.get(b));
        array.set(b, tmp);
    }
}
