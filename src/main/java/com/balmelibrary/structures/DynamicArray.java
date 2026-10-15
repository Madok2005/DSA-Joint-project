package com.balmelibrary.structures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Resizable array, built from scratch (no java.util.ArrayList).
 *
 * Big-O:
 *   get/set          O(1)
 *   add (amortised)  O(1) amortised, O(n) worst case on resize
 *   insert/remove(i) O(n)  -- shifts elements
 *
 * Resize trace (for the report): capacity doubles whenever size == capacity.
 * Log each resize event (oldCapacity -> newCapacity) during testing to build
 * the "resize trace" diagram required in Section 4.2 of the guide.
 */
public class DynamicArray<T> implements Iterable<T> {

    private static final int DEFAULT_CAPACITY = 8;

    private Object[] data;
    private int size;

    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be > 0");
        }
        data = new Object[initialCapacity];
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int capacity() {
        return data.length;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) data[index];
    }

    public void set(int index, T value) {
        checkIndex(index);
        data[index] = value;
    }

    public void add(T value) {
        ensureCapacity(size + 1);
        data[size++] = value;
    }

    public void insert(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
        }
        ensureCapacity(size + 1);
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }
        data[index] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkIndex(index);
        T removed = (T) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[--size] = null;
        return removed;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
        }
    }

    // TODO(Data Structure Developer): call this from add()/insert() and log
    // (oldCapacity, newCapacity, size) somewhere your test harness can read,
    // so QA/Algorithm Developer can plot the resize trace required by the guide.
    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) {
            return;
        }
        int newCapacity = data.length * 2;
        while (newCapacity < minCapacity) {
            newCapacity *= 2;
        }
        Object[] bigger = new Object[newCapacity];
        System.arraycopy(data, 0, bigger, 0, size);
        data = bigger;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return (T) data[cursor++];
            }
        };
    }
}
