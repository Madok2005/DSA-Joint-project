package com.balmelibrary.algorithms;

import com.balmelibrary.structures.DynamicArray;
import com.balmelibrary.util.Stopwatch;

import java.util.Comparator;
import java.util.Objects;

/**
 * Search algorithms implemented from first principles for Module M4 of the
 * Ghana Smart Service Operations Optimizer project (Balme Library dataset).
 *
 * <p>No {@code java.util.Arrays.binarySearch}, {@code Collections.binarySearch}
 * or any other library search routine is used anywhere in this class; every
 * comparison and index calculation is written out explicitly so it can be
 * traced for the report's Table 8 evidence.</p>
 *
 * <p>Both algorithms are generic over any element type {@code T} via a
 * {@link Comparator}&lt;T&gt;, so the exact same code searches
 * {@code LibraryLocation}, {@code RoadSegment}, {@code ServiceRequestRecord}
 * or {@code LibraryResource} records without modification -- only the
 * comparator changes (e.g. by {@code locationId}, by {@code distanceKm}, by
 * {@code urgency}).</p>
 *
 * <p><b>Connected to the Data Structure Developer's module.</b> Every method
 * has two public overloads: one over a plain {@code T[]} (unchanged from the
 * original Algorithm Developer drop, so existing call sites and tests keep
 * compiling), and one over {@link DynamicArray}&lt;T&gt;, the Data Structure
 * Developer's from-scratch resizable array. Both overloads run the exact same
 * comparison/index logic -- they are thin adapters over a single private
 * {@link Indexed} core (see {@link #linearSearchCore}, {@link #binarySearchCore},
 * {@link #isSortedCore}) so there is only one implementation of each algorithm
 * to reason about, trace and test, regardless of which array-like structure a
 * caller happens to be holding.</p>
 */
public final class SearchAlgorithms {

    private SearchAlgorithms() {
    }

    // ------------------------------------------------------------------
    // Indexed<T>: the shared seam between "raw T[]" and "DynamicArray<T>"
    // ------------------------------------------------------------------

    /**
     * Minimal read/size view over an array-like structure, used internally so
     * every algorithm below is written exactly once against {@code get(i)}/
     * {@code size()} and then reused by both the {@code T[]} overloads and the
     * {@link DynamicArray} overloads. Not part of the public API: callers
     * always work with {@code T[]} or {@link DynamicArray}&lt;T&gt; directly.
     */
    private interface Indexed<T> {
        int size();

        T get(int index);
    }

    private static <T> Indexed<T> indexed(T[] array) {
        return new Indexed<T>() {
            public int size() {
                return array.length;
            }

            public T get(int index) {
                return array[index];
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
        };
    }

    // ------------------------------------------------------------------
    // Linear search
    // ------------------------------------------------------------------

    /**
     * Linear (sequential) search over a plain array.
     *
     * <p><b>Precondition:</b> none. {@code array} may be empty, unsorted, and
     * may contain duplicate keys.</p>
     *
     * <p><b>Postcondition:</b> returns the smallest index {@code i} such that
     * {@code comparator.compare(array[i], target) == 0}, or -1 if no such
     * index exists.</p>
     *
     * <p><b>Complexity:</b> O(n) worst case and average case, O(1) best case.
     * O(1) extra space.</p>
     *
     * @throws NullPointerException if {@code array}, {@code target} or
     *                               {@code comparator} is {@code null}
     */
    public static <T> SearchOutcome linearSearch(T[] array, T target, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return linearSearchCore(indexed(array), target, comparator);
    }

    /**
     * Linear (sequential) search over the Data Structure Developer's
     * {@link DynamicArray}. Identical algorithm and evidence payload to
     * {@link #linearSearch(Object[], Object, Comparator)}; only the backing
     * structure differs.
     *
     * @throws NullPointerException if {@code array}, {@code target} or
     *                               {@code comparator} is {@code null}
     */
    public static <T> SearchOutcome linearSearch(DynamicArray<T> array, T target, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return linearSearchCore(indexed(array), target, comparator);
    }

    private static <T> SearchOutcome linearSearchCore(Indexed<T> array, T target, Comparator<T> comparator) {
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(comparator, "comparator must not be null");

        Stopwatch stopwatch = new Stopwatch().start();
        long comparisons = 0;
        int result = -1;

        for (int i = 0; i < array.size(); i++) {
            comparisons++;
            if (comparator.compare(array.get(i), target) == 0) {
                result = i;
                break;
            }
        }

        long elapsed = stopwatch.stop();
        return new SearchOutcome(result, comparisons, elapsed);
    }

    // ------------------------------------------------------------------
    // Binary search
    // ------------------------------------------------------------------

    /**
     * Iterative binary search over a plain array.
     *
     * <p><b>STATED PRECONDITION:</b> {@code array} MUST already be sorted in
     * non-descending order according to {@code comparator}. Binary search
     * relies on this invariant to safely discard half of the remaining search
     * space at every step; calling it on an unsorted array gives an
     * <em>undefined</em> result (it may silently return -1 for a value that
     * is present, or the wrong index for a duplicate key) without necessarily
     * throwing anything.</p>
     *
     * <p>Because a silent wrong answer is worse than a loud failure, this
     * method checks the precondition explicitly (an O(n) scan via
     * {@link #isSorted(Object[], Comparator)}) and throws
     * {@link IllegalArgumentException} rather than returning a
     * plausible-looking but incorrect index. See
     * {@code SearchAlgorithmsTest#rejectsUnsortedInput} for the required
     * precondition test. {@link #binarySearchUnchecked(Object[], Object, Comparator)}
     * is provided for callers who have already established the precondition
     * elsewhere (e.g. immediately after calling {@link SortAlgorithms#mergeSort})
     * and want to skip the O(n) verification cost.</p>
     *
     * <p><b>Postcondition:</b> if the target is present, returns <em>an</em>
     * index {@code i} with {@code comparator.compare(array[i], target) == 0}
     * (not necessarily the first, for arrays with duplicate keys); otherwise
     * returns -1.</p>
     *
     * <p><b>Complexity:</b> O(log n) for the search itself; this method adds
     * an O(n) precondition check on top (use {@link #binarySearchUnchecked}
     * for pure O(log n) once the precondition is already guaranteed). O(1)
     * extra space either way.</p>
     *
     * @throws NullPointerException     if {@code array}, {@code target} or
     *                                   {@code comparator} is {@code null}
     * @throws IllegalArgumentException if {@code array} is not sorted in
     *                                   non-descending order per {@code comparator}
     */
    public static <T> SearchOutcome binarySearch(T[] array, T target, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return binarySearchCore(indexed(array), target, comparator);
    }

    /**
     * Iterative binary search over the Data Structure Developer's
     * {@link DynamicArray}. Same stated precondition, same evidence payload,
     * as {@link #binarySearch(Object[], Object, Comparator)} -- only the
     * backing structure differs. {@link DynamicArray#get} is O(1), so the
     * O(log n) complexity guarantee is unaffected by the choice of structure.
     *
     * @throws NullPointerException     if {@code array}, {@code target} or
     *                                   {@code comparator} is {@code null}
     * @throws IllegalArgumentException if {@code array} is not sorted in
     *                                   non-descending order per {@code comparator}
     */
    public static <T> SearchOutcome binarySearch(DynamicArray<T> array, T target, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return binarySearchCore(indexed(array), target, comparator);
    }

    private static <T> SearchOutcome binarySearchCore(Indexed<T> array, T target, Comparator<T> comparator) {
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(comparator, "comparator must not be null");

        if (!isSortedCore(array, comparator)) {
            throw new IllegalArgumentException(
                    "Precondition violated: binarySearch requires the input array to be "
                            + "sorted in non-descending order according to the supplied comparator. "
                            + "Sort it first (see SortAlgorithms.insertionSort/selectionSort/mergeSort) "
                            + "or use linearSearch on unsorted data.");
        }
        return binarySearchUncheckedCore(array, target, comparator);
    }

    /**
     * Binary search without the O(n) precondition check performed by
     * {@link #binarySearch(Object[], Object, Comparator)}. Callers take on
     * the responsibility of guaranteeing {@code array} is already sorted per
     * {@code comparator}; violating this precondition yields undefined (not
     * exception-throwing) behaviour, matching classic textbook binary search.
     * Prefer {@link #binarySearch} unless the O(n) check is a proven
     * bottleneck.
     */
    public static <T> SearchOutcome binarySearchUnchecked(T[] array, T target, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return binarySearchUncheckedCore(indexed(array), target, comparator);
    }

    /**
     * {@link DynamicArray} counterpart of {@link #binarySearchUnchecked(Object[], Object, Comparator)}.
     */
    public static <T> SearchOutcome binarySearchUnchecked(DynamicArray<T> array, T target, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return binarySearchUncheckedCore(indexed(array), target, comparator);
    }

    private static <T> SearchOutcome binarySearchUncheckedCore(Indexed<T> array, T target, Comparator<T> comparator) {
        Stopwatch stopwatch = new Stopwatch().start();
        long comparisons = 0;
        int low = 0;
        int high = array.size() - 1;
        int result = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2; // avoids (low + high) overflow
            comparisons++;
            int cmp = comparator.compare(array.get(mid), target);
            if (cmp == 0) {
                result = mid;
                break;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        long elapsed = stopwatch.stop();
        return new SearchOutcome(result, comparisons, elapsed);
    }

    /**
     * Precondition helper: verifies {@code array} is sorted in non-descending
     * order per {@code comparator}. Exposed as {@code public} so both the
     * production code ({@link #binarySearch}) and the unit tests reuse the
     * exact same check. An empty array or a single-element array is
     * trivially sorted.
     *
     * @throws NullPointerException if {@code array} or {@code comparator} is {@code null}
     */
    public static <T> boolean isSorted(T[] array, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return isSortedCore(indexed(array), comparator);
    }

    /**
     * {@link DynamicArray} counterpart of {@link #isSorted(Object[], Comparator)}.
     */
    public static <T> boolean isSorted(DynamicArray<T> array, Comparator<T> comparator) {
        Objects.requireNonNull(array, "array must not be null");
        return isSortedCore(indexed(array), comparator);
    }

    private static <T> boolean isSortedCore(Indexed<T> array, Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "comparator must not be null");
        for (int i = 1; i < array.size(); i++) {
            if (comparator.compare(array.get(i - 1), array.get(i)) > 0) {
                return false;
            }
        }
        return true;
    }
}
