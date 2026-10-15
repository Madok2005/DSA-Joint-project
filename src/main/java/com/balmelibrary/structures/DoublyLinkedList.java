package com.balmelibrary.structures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Doubly linked list with head/tail pointers, forward and reverse iteration.
 * Used internally by Deque (Section 4.2 of the guide) so implement this
 * before starting the Deque.
 *
 * Big-O: addFirst/addLast/removeFirst/removeLast all O(1).
 *        get(index) O(n) (walks from the closer end).
 */
public class DoublyLinkedList<T> implements Iterable<T> {

    private static class Node<T> {
        T value;
        Node<T> prev;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void addFirst(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
    }

    public void addLast(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            node.prev = tail;
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public T removeFirst() {
        if (head == null) throw new NoSuchElementException("list is empty");
        T value = head.value;
        head = head.next;
        if (head != null) head.prev = null; else tail = null;
        size--;
        return value;
    }

    public T removeLast() {
        if (tail == null) throw new NoSuchElementException("list is empty");
        T value = tail.value;
        tail = tail.prev;
        if (tail != null) tail.next = null; else head = null;
        size--;
        return value;
    }

    public T peekFirst() {
        if (head == null) throw new NoSuchElementException("list is empty");
        return head.value;
    }

    public T peekLast() {
        if (tail == null) throw new NoSuchElementException("list is empty");
        return tail.value;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
        }
        Node<T> current;
        if (index < size / 2) {
            current = head;
            for (int i = 0; i < index; i++) current = current.next;
        } else {
            current = tail;
            for (int i = size - 1; i > index; i--) current = current.prev;
        }
        return current.value;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> cursor = head;

            @Override
            public boolean hasNext() { return cursor != null; }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T value = cursor.value;
                cursor = cursor.next;
                return value;
            }
        };
    }

    public Iterator<T> descendingIterator() {
        return new Iterator<T>() {
            private Node<T> cursor = tail;

            @Override
            public boolean hasNext() { return cursor != null; }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T value = cursor.value;
                cursor = cursor.prev;
                return value;
            }
        };
    }
}
