package com.balmelibrary.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Second example, chosen because collision handling is the "evidence
 * required" item the guide calls out specifically for the hash table.
 */
class MyHashMapTest {

    // --- normal case ---
    @Test
    void putThenGetReturnsValue() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("IAC", 1);
        map.put("Knowledge Commons", 2);
        assertEquals(1, map.get("IAC"));
        assertEquals(2, map.get("Knowledge Commons"));
    }

    @Test
    void updatingExistingKeyDoesNotIncreaseSizeOrCollisions() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("Reading Room", 1);
        long before = map.collisionCount();
        map.put("Reading Room", 2); // update, not insert
        assertEquals(1, map.size());
        assertEquals(before, map.collisionCount());
        assertEquals(2, map.get("Reading Room"));
    }

    // --- boundary case ---
    @Test
    void missingKeyReturnsNull() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        assertNull(map.get("does-not-exist"));
    }

    @Test
    void forcedCollisionInSmallTableIsCounted() {
        // capacity 1 forces every key into bucket 0 -- deliberately
        // adversarial, for the collision-statistics evidence item.
        MyHashMap<String, Integer> map = new MyHashMap<>(1);
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        assertTrue(map.collisionCount() >= 2);
    }

    // --- invalid-input case ---
    @Test
    void removeOnEmptyMapReturnsNullNotException() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        assertNull(map.remove("nothing-here"));
    }

    @Test
    void nullKeyIsHandledConsistently() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put(null, 99);
        assertEquals(99, map.get(null));
        assertTrue(map.containsKey(null));
    }
}
