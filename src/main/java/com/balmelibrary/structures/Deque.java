package com.balmelibrary.structures;

import java.util.NoSuchElementException;

/**
 * Double-ended queue built on DoublyLinkedList (no java.util.ArrayDeque).
 * Useful for the scheduling engine when a request needs to be pushed back
 * to the front of the line (e.g. a re-queued failed dispatch).
 *
 * Big-O: addFirst/addLast/removeFirst/removeLast/peekFirst/peekLast O(1).
 */
public class Deque<T> {

    private final DoublyLinkedList<T> data = new DoublyLinkedList<>();

    public void addFirst(T value) { data.addFirst(value); }
    public void addLast(T value) { data.addLast(value); }

    public T removeFirst() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        return data.removeFirst();
    }

    public T removeLast() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        return data.removeLast();
    }

    public T peekFirst() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        return data.peekFirst();
    }

    public T peekLast() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        return data.peekLast();
    }

    public boolean isEmpty() { return data.isEmpty(); }
    public int size() { return data.size(); }
}
