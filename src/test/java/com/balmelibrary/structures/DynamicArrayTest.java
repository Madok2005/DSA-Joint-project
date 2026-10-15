package com.balmelibrary.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test class showing the normal-case / boundary-case / invalid-input
 * pattern the guide requires for every structure (Section 4.2). Copy this
 * shape for the other 12 structures -- Testing & QA will review/extend
 * rather than write these from scratch.
 */
class DynamicArrayTest {

    // --- normal case ---
    @Test
    void addAndGetReturnInsertedValues() {
        DynamicArray<String> arr = new DynamicArray<>();
        arr.add("Book Loan");
        arr.add("Book Return");
        assertEquals(2, arr.size());
        assertEquals("Book Loan", arr.get(0));
        assertEquals("Book Return", arr.get(1));
    }

    @Test
    void resizeGrowsCapacityWithoutLosingData() {
        DynamicArray<Integer> arr = new DynamicArray<>(2); // tiny initial capacity to force a resize
        for (int i = 0; i < 10; i++) arr.add(i);
        assertEquals(10, arr.size());
        for (int i = 0; i < 10; i++) assertEquals(i, arr.get(i));
    }

    // --- boundary case ---
    @Test
    void emptyArrayHasZeroSize() {
        DynamicArray<Integer> arr = new DynamicArray<>();
        assertTrue(arr.isEmpty());
        assertEquals(0, arr.size());
    }

    @Test
    void singleElementRemoveLeavesArrayEmpty() {
        DynamicArray<Integer> arr = new DynamicArray<>();
        arr.add(42);
        arr.remove(0);
        assertTrue(arr.isEmpty());
    }

    // --- invalid-input case ---
    @Test
    void getWithNegativeIndexThrows() {
        DynamicArray<Integer> arr = new DynamicArray<>();
        arr.add(1);
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(-1));
    }

    @Test
    void getPastEndThrows() {
        DynamicArray<Integer> arr = new DynamicArray<>();
        arr.add(1);
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(1));
    }
}
