package com.balmelibrary.structures;

/**
 * Set built on top of MyHashMap (satisfies the guide's "a set/map built on
 * the hash table or BST" line item). Kept as a thin wrapper so it reuses
 * MyHashMap's tested collision handling rather than duplicating it.
 */
public class MySet<T> {

    private final MyHashMap<T, Boolean> map = new MyHashMap<>();

    public void add(T value) {
        map.put(value, Boolean.TRUE);
    }

    public boolean contains(T value) {
        return map.containsKey(value);
    }

    public void remove(T value) {
        map.remove(value);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void forEach(java.util.function.Consumer<T> visitor) {
        map.forEach((key, ignoredValue) -> visitor.accept(key));
    }
}
