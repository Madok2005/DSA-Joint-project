package com.balmelibrary.algorithms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Normal-case, boundary-case and invalid-input tests for
 * {@link SortAlgorithms}, per the project brief's Table 8 evidence
 * requirements, plus a cross-validation test that all three sorts agree.
 */
class SortAlgorithmsTest {

    private static final Comparator<Integer> NATURAL = Comparator.naturalOrder();

    @Nested
    @DisplayName("insertionSort")
    class InsertionSortTests {

        @Test
        @DisplayName("normal case: sorts a random array")
        void sortsRandomArray() {
            Integer[] a = {40, 10, 30, 20, 50};
            SortAlgorithms.insertionSort(a, NATURAL);
            assertArrayEquals(new Integer[]{10, 20, 30, 40, 50}, a);
        }

        @Test
        @DisplayName("best case: already-sorted input does exactly n-1 comparisons")
        void handlesAlreadySortedInput() {
            Integer[] a = {1, 2, 3, 4};
            SortOutcome outcome = SortAlgorithms.insertionSort(a, NATURAL);
            assertArrayEquals(new Integer[]{1, 2, 3, 4}, a);
            assertEquals(3, outcome.comparisons());
        }

        @Test
        @DisplayName("worst case: reverse-sorted input is fully sorted")
        void handlesReverseSortedInput() {
            Integer[] a = {4, 3, 2, 1};
            SortAlgorithms.insertionSort(a, NATURAL);
            assertArrayEquals(new Integer[]{1, 2, 3, 4}, a);
        }

        @Test
        @DisplayName("boundary case: empty and single-element arrays are handled without error")
        void boundaryEmptyAndSingleElement() {
            Integer[] empty = {};
            Integer[] single = {1};
            assertDoesNotThrow(() -> SortAlgorithms.insertionSort(empty, NATURAL));
            assertDoesNotThrow(() -> SortAlgorithms.insertionSort(single, NATURAL));
            assertArrayEquals(new Integer[]{1}, single);
        }

        @Test
        @DisplayName("stability: elements with equal keys keep their original relative order")
        void isStableForEqualKeys() {
            String[] a = {"b1", "a1", "b2", "a2"};
            Comparator<String> byFirstChar = Comparator.comparing(s -> s.charAt(0));
            SortAlgorithms.insertionSort(a, byFirstChar);
            assertArrayEquals(new String[]{"a1", "a2", "b1", "b2"}, a);
        }

        @Test
        @DisplayName("invalid input: null array is rejected")
        void nullArrayRejected() {
            assertThrows(NullPointerException.class, () -> SortAlgorithms.insertionSort((Integer[]) null, NATURAL));
        }
    }

    @Nested
    @DisplayName("selectionSort")
    class SelectionSortTests {

        @Test
        @DisplayName("normal case: sorts a random array")
        void sortsRandomArray() {
            Integer[] a = {40, 10, 30, 20, 50};
            SortAlgorithms.selectionSort(a, NATURAL);
            assertArrayEquals(new Integer[]{10, 20, 30, 40, 50}, a);
        }

        @Test
        @DisplayName("normal case: handles duplicate keys correctly")
        void handlesDuplicates() {
            Integer[] a = {3, 1, 3, 2, 1};
            SortAlgorithms.selectionSort(a, NATURAL);
            assertArrayEquals(new Integer[]{1, 1, 2, 3, 3}, a);
        }

        @Test
        @DisplayName("boundary case: empty and single-element arrays are handled without error")
        void boundaryEmptyAndSingleElement() {
            Integer[] empty = {};
            Integer[] single = {1};
            assertDoesNotThrow(() -> SortAlgorithms.selectionSort(empty, NATURAL));
            assertDoesNotThrow(() -> SortAlgorithms.selectionSort(single, NATURAL));
        }

        @Test
        @DisplayName("comparison count is independent of initial input order (always ~n^2/2)")
        void comparisonCountIsInputOrderIndependent() {
            Integer[] sorted = {1, 2, 3, 4, 5};
            Integer[] reverse = {5, 4, 3, 2, 1};
            SortOutcome sortedOutcome = SortAlgorithms.selectionSort(sorted, NATURAL);
            SortOutcome reverseOutcome = SortAlgorithms.selectionSort(reverse, NATURAL);
            assertEquals(sortedOutcome.comparisons(), reverseOutcome.comparisons());
        }

        @Test
        @DisplayName("invalid input: null comparator is rejected")
        void nullComparatorRejected() {
            assertThrows(NullPointerException.class,
                    () -> SortAlgorithms.selectionSort(new Integer[]{1}, null));
        }
    }

    @Nested
    @DisplayName("mergeSort")
    class MergeSortTests {

        @Test
        @DisplayName("normal case: sorts a random array")
        void sortsRandomArray() {
            Integer[] a = {40, 10, 30, 20, 50};
            SortAlgorithms.mergeSort(a, NATURAL);
            assertArrayEquals(new Integer[]{10, 20, 30, 40, 50}, a);
        }

        @Test
        @DisplayName("boundary case: empty and single-element arrays are handled without error")
        void boundaryEmptyAndSingleElement() {
            Integer[] empty = {};
            Integer[] single = {1};
            assertDoesNotThrow(() -> SortAlgorithms.mergeSort(empty, NATURAL));
            assertDoesNotThrow(() -> SortAlgorithms.mergeSort(single, NATURAL));
        }

        @Test
        @DisplayName("stability: elements with equal keys keep their original relative order")
        void isStableForEqualKeys() {
            String[] a = {"b1", "a1", "b2", "a2"};
            Comparator<String> byFirstChar = Comparator.comparing(s -> s.charAt(0));
            SortAlgorithms.mergeSort(a, byFirstChar);
            assertArrayEquals(new String[]{"a1", "a2", "b1", "b2"}, a);
        }

        @Test
        @DisplayName("comparisons stay within the theoretical O(n log n) bound")
        void achievesOnLogNComparisonBound() {
            Integer[] a = new Integer[64];
            for (int i = 0; i < a.length; i++) {
                a[i] = a.length - i; // reverse-sorted
            }
            SortOutcome outcome = SortAlgorithms.mergeSort(a, NATURAL);
            long theoreticalUpperBound = (long) (a.length * (Math.log(a.length) / Math.log(2))) + a.length;
            assertTrue(outcome.comparisons() <= theoreticalUpperBound,
                    "comparisons=" + outcome.comparisons() + " exceeded the O(n log n) bound " + theoreticalUpperBound);
        }

        @Test
        @DisplayName("invalid input: null array is rejected")
        void nullArrayRejected() {
            assertThrows(NullPointerException.class, () -> SortAlgorithms.mergeSort((Integer[]) null, NATURAL));
        }
    }

    @Test
    @DisplayName("cross-validation: insertionSort, selectionSort and mergeSort agree on the same input")
    void allThreeSortsAgree() {
        Integer[] base = {17, 3, 9, 42, 8, 8, -1, 0, 15, 4};
        Integer[] a = base.clone();
        Integer[] b = base.clone();
        Integer[] c = base.clone();

        SortAlgorithms.insertionSort(a, NATURAL);
        SortAlgorithms.selectionSort(b, NATURAL);
        SortAlgorithms.mergeSort(c, NATURAL);

        assertArrayEquals(a, b);
        assertArrayEquals(b, c);
    }
}
