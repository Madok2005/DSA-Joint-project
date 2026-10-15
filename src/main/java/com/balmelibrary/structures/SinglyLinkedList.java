package com.balmelibrary.structures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Singly linked list with head/tail pointers and a forward iterator.
 *
 * Big-O:
 *   addFirst/addLast   O(1)
 *   removeFirst        O(1)
 *   removeLast         O(n)  -- no back-pointer, must walk to find predecessor
 *   get(index)/contains O(n)
 */
public class SinglyLinkedList<T> implements Iterable<T> {

    private static class Node<T> {
        T value;
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
        node.next = head;
        head = node;
        if (tail == null) tail = node;
        size++;
    }

    public void addLast(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public T removeFirst() {
        if (head == null) throw new NoSuchElementException("list is empty");
        T value = head.value;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return value;
    }

    // O(n): walk to the node before tail. Documented cost — mention it in
    // the design note as a reason to prefer DoublyLinkedList when
    // removeLast is on the hot path (e.g. deque-style usage).
    public T removeLast() {
        if (head == null) throw new NoSuchElementException("list is empty");
        if (head == tail) {
            T value = head.value;
            head = tail = null;
            size--;
            return value;
        }
        Node<T> current = head;
        while (current.next != tail) {
            current = current.next;
        }
        T value = tail.value;
        current.next = null;
        tail = current;
        size--;
        return value;
    }

    public T get(int index) {
        checkIndex(index);
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.value;
    }

    public boolean contains(T value) {
        for (T item : this) {
            if (item == null ? value == null : item.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
        }
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
}
