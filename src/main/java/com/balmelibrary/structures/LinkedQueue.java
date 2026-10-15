package com.balmelibrary.structures;

import java.util.NoSuchElementException;

/**
 * FIFO queue built on SinglyLinkedList. Use this for the plain FIFO
 * dispatch mode in the Algorithm Developer's scheduling engine
 * (Section 4.3: "FIFO, urgency-based, and priority-based dispatch").
 *
 * Big-O: enqueue/dequeue/peek all O(1) (addLast/removeFirst on the list).
 */
public class LinkedQueue<T> {

    private final SinglyLinkedList<T> data = new SinglyLinkedList<>();

    public void enqueue(T value) {
        data.addLast(value);
    }

    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("queue is empty");
        return data.removeFirst();
    }

    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("queue is empty");
        return data.get(0);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public int size() {
        return data.size();
    }
}
