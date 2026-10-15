package com.balmelibrary.structures;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Binary heap / priority queue, array-backed (no java.util.PriorityQueue).
 * This is the structure behind "priority-based dispatch" in the Algorithm
 * Developer's scheduling engine: wrap each service_request with a
 * Comparator on urgency (and the index-number-derived priority weight).
 *
 * Min-heap by default (smallest key = highest priority). Pass
 * Comparator.reverseOrder() (or your own comparator) at construction for a
 * max-heap.
 *
 * Big-O: insert O(log n), extractMin O(log n), peek O(1), buildHeap O(n).
 */
public class BinaryHeap<T> {

    private final DynamicArray<T> data = new DynamicArray<>();
    private final Comparator<? super T> comparator;

    public BinaryHeap(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    public int size() { return data.size(); }
    public boolean isEmpty() { return data.isEmpty(); }

    public void insert(T value) {
        data.add(value);
        siftUp(data.size() - 1);
    }

    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("heap is empty");
        return data.get(0);
    }

    public T extractMin() {
        if (isEmpty()) throw new NoSuchElementException("heap is empty");
        T root = data.get(0);
        T last = data.remove(data.size() - 1);
        if (!data.isEmpty()) {
            data.set(0, last);
            siftDown(0);
        }
        return root;
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (comparator.compare(data.get(i), data.get(parent)) >= 0) break;
            swap(i, parent);
            i = parent;
        }
    }

    private void siftDown(int i) {
        int n = data.size();
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;
            if (left < n && comparator.compare(data.get(left), data.get(smallest)) < 0) smallest = left;
            if (right < n && comparator.compare(data.get(right), data.get(smallest)) < 0) smallest = right;
            if (smallest == i) break;
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int i, int j) {
        T tmp = data.get(i);
        data.set(i, data.get(j));
        data.set(j, tmp);
    }
}
