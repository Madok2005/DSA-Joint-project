package com.balmelibrary.structures;

import java.util.EmptyStackException;

/**
 * LIFO stack built on DynamicArray (no java.util.Stack / ArrayDeque).
 * Suggested use in the project: audit_events undo stack (Database Engineer
 * handoff) and any DFS/backtracking implementation in Algorithm Developer's
 * graph module.
 *
 * Big-O: push/pop/peek amortised O(1).
 */
public class ArrayStack<T> {

    private final DynamicArray<T> data = new DynamicArray<>();

    public void push(T value) {
        data.add(value);
    }

    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        return data.remove(data.size() - 1);
    }

    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return data.get(data.size() - 1);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public int size() {
        return data.size();
    }
}
