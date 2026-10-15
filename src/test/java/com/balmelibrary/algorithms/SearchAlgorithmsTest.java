package com.balmelibrary.algorithms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Normal-case, boundary-case and invalid-input tests for
 * {@link SearchAlgorithms}, per the project brief's Table 8 evidence
 * requirements. See {@link BinarySearchTests#rejectsUnsortedInput()} for the
 * required binary-search precondition test.
 */
class SearchAlgorithmsTest {

    private static final Comparator<Integer> NATURAL = Comparator.naturalOrder();

    @Nested
    @DisplayName("linearSearch")
    class LinearSearchTests {

        @Test
        @DisplayName("normal case: finds a present element in an unsorted array")
        void findsPresentElement() {
            Integer[] data = {40, 10, 30, 20, 50};
            SearchOutcome outcome = SearchAlgorithms.linearSearch(data, 30, NATURAL);
            assertEquals(2, outcome.index());
            assertTrue(outcome.found());
        }

        @Test
        @DisplayName("normal case: returns -1 for an absent element")
        void absentElement() {
            Integer[] data = {40, 10, 30, 20, 50};
            SearchOutcome outcome = SearchAlgorithms.linearSearch(data, 99, NATURAL);
            assertEquals(-1, outcome.index());
            assertFalse(outcome.found());
        }

        @Test
        @DisplayName("boundary case: empty array returns -1 with zero comparisons")
        void emptyArray() {
            Integer[] data = {};
            SearchOutcome outcome = SearchAlgorithms.linearSearch(data, 1, NATURAL);
            assertEquals(-1, outcome.index());
            assertEquals(0, outcome.comparisons());
        }

        @Test
        @DisplayName("boundary case: returns the first matching index among duplicates")
        void firstOfDuplicates() {
            Integer[] data = {5, 5, 5};
            SearchOutcome outcome = SearchAlgorithms.linearSearch(data, 5, NATURAL);
            assertEquals(0, outcome.index());
        }

        @Test
        @DisplayName("invalid input: null array is rejected")
        void nullArrayRejected() {
            assertThrows(NullPointerException.class,
                    () -> SearchAlgorithms.linearSearch((Integer[]) null, 1, NATURAL));
        }

        @Test
        @DisplayName("invalid input: null target is rejected")
        void nullTargetRejected() {
            Integer[] data = {1, 2, 3};
            assertThrows(NullPointerException.class,
                    () -> SearchAlgorithms.linearSearch(data, null, NATURAL));
        }
    }
//    () -> SearchAlgorithms.linearSearch((Integer[]) null, 1, NATURAL));

    @Nested
    @DisplayName("binarySearch")
    class BinarySearchTests {

        @Test
        @DisplayName("normal case: finds a present element in a sorted array")
        void findsPresentElement() {
            Integer[] data = {10, 20, 30, 40, 50};
            SearchOutcome outcome = SearchAlgorithms.binarySearch(data, 40, NATURAL);
            assertEquals(3, outcome.index());
        }

        @Test
        @DisplayName("normal case: returns -1 for an absent element in a sorted array")
        void absentElement() {
            Integer[] data = {10, 20, 30, 40, 50};
            SearchOutcome outcome = SearchAlgorithms.binarySearch(data, 25, NATURAL);
            assertEquals(-1, outcome.index());
        }

        @Test
        @DisplayName("boundary case: empty array is trivially sorted and returns -1")
        void emptyArray() {
            Integer[] data = {};
            SearchOutcome outcome = SearchAlgorithms.binarySearch(data, 1, NATURAL);
            assertEquals(-1, outcome.index());
        }

        @Test
        @DisplayName("boundary case: single-element array, hit and miss")
        void singleElement() {
            Integer[] data = {7};
            assertEquals(0, SearchAlgorithms.binarySearch(data, 7, NATURAL).index());
            assertEquals(-1, SearchAlgorithms.binarySearch(data, 8, NATURAL).index());
        }

        @Test
        @DisplayName("boundary case: finds a valid index among duplicate keys")
        void duplicateKeys() {
            Integer[] data = {1, 3, 3, 3, 3, 8};
            SearchOutcome outcome = SearchAlgorithms.binarySearch(data, 3, NATURAL);
            assertTrue(outcome.index() >= 1 && outcome.index() <= 4);
        }

        @Test
        @DisplayName("PRECONDITION TEST: throws IllegalArgumentException on unsorted input")
        void rejectsUnsortedInput() {
            Integer[] unsorted = {30, 10, 50, 20, 40};
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> SearchAlgorithms.binarySearch(unsorted, 20, NATURAL));
            assertTrue(ex.getMessage().toLowerCase().contains("sorted"),
                    "exception message should explain the violated precondition");
        }

        @Test
        @DisplayName("isSorted correctly classifies sorted, unsorted, empty and single-element arrays")
        void isSortedHelper() {
            assertTrue(SearchAlgorithms.isSorted(new Integer[]{}, NATURAL));
            assertTrue(SearchAlgorithms.isSorted(new Integer[]{5}, NATURAL));
            assertTrue(SearchAlgorithms.isSorted(new Integer[]{1, 1, 2, 3}, NATURAL));
            assertFalse(SearchAlgorithms.isSorted(new Integer[]{2, 1}, NATURAL));
        }

        @Test
        @DisplayName("binarySearchUnchecked skips the precondition check (undefined but non-throwing on unsorted input)")
        void uncheckedVariantSkipsValidation() {
            Integer[] unsorted = {30, 10, 50, 20, 40};
            assertDoesNotThrow(() -> SearchAlgorithms.binarySearchUnchecked(unsorted, 20, NATURAL));
        }

        @Test
        @DisplayName("invalid input: null array is rejected")
        void nullArrayRejected() {
            assertThrows(NullPointerException.class,
                    () -> SearchAlgorithms.linearSearch((Integer[]) null, 1, NATURAL));
        }
// () -> SearchAlgorithms.linearSearch((Integer[]) null, 1, NATURAL));
        @Test
        @DisplayName("invalid input: null comparator is rejected")
        void nullComparatorRejected() {
            Integer[] data = {1, 2, 3};
            assertThrows(NullPointerException.class,
                    () -> SearchAlgorithms.binarySearch(data, 2, null));
        }
    }
}
