package com.balmelibrary.structures;

import java.util.NoSuchElementException;

/**
 * Fixed-capacity circular buffer queue (array-based, wraps around using
 * modulo arithmetic). Distinct from LinkedQueue: this is the structure to
 * point to in the oral defense when asked "why not just use a plain
 * queue?" -- O(1) operations with no per-node allocation, at the cost of
 * a fixed capacity.
 *
 * Big-O: enqueue/dequeue/peek O(1).
 */
public class CircularQueue<T> {

    private final Object[] data;
    private int front;   // index of the current front element
    private int size;    // number of elements currently stored
    private final int capacity;

    public CircularQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.front = 0;
        this.size = 0;
    }

    public void enqueue(T value) {
        if (isFull()) throw new IllegalStateException("queue is full (capacity=" + capacity + ")");
        int rear = (front + size) % capacity;
        data[rear] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("queue is empty");
        T value = (T) data[front];
        data[front] = null;
        front = (front + 1) % capacity;
        size--;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("queue is empty");
        return (T) data[front];
    }

    public boolean isEmpty() { return size == 0; }
    public boolean isFull() { return size == capacity; }
    public int size() { return size; }
    public int capacity() { return capacity; }
}
