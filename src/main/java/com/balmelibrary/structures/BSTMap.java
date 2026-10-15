package com.balmelibrary.structures;

/**
 * Unbalanced binary search tree, key-value map (no java.util.TreeMap).
 * Keep this even after RedBlackTree is done -- the "BST vs balanced tree"
 * performance experiment (Testing & QA, six required experiments) needs a
 * plain, unbalanced BST to compare against.
 *
 * Big-O: average O(log n) per op, worst case O(n) on adversarial
 * (sorted-order) insertion -- that worst case IS the point of the
 * comparison experiment, so don't "fix" it here.
 */
public class BSTMap<K extends Comparable<K>, V> {

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> left, right;
        Node(K key, V value) { this.key = key; this.value = value; }
    }

    private Node<K, V> root;
    private int size;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private Node<K, V> put(Node<K, V> node, K key, V value) {
        if (node == null) {
            size++;
            return new Node<>(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) node.left = put(node.left, key, value);
        else if (cmp > 0) node.right = put(node.right, key, value);
        else node.value = value;
        return node;
    }

    public V get(K key) {
        Node<K, V> node = findNode(root, key);
        return node == null ? null : node.value;
    }

    public boolean contains(K key) {
        return findNode(root, key) != null;
    }

    private Node<K, V> findNode(Node<K, V> node, K key) {
        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp < 0) node = node.left;
            else if (cmp > 0) node = node.right;
            else return node;
        }
        return null;
    }

    public void remove(K key) {
        root = remove(root, key);
    }

    private Node<K, V> remove(Node<K, V> node, K key) {
        if (node == null) return null;
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            size--;
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            // two children: replace with inorder successor (min of right subtree)
            Node<K, V> successor = min(node.right);
            node.key = successor.key;
            node.value = successor.value;
            size++; // undo the decrement above; the recursive call below removes it once
            node.right = remove(node.right, successor.key);
        }
        return node;
    }

    private Node<K, V> min(Node<K, V> node) {
        while (node.left != null) node = node.left;
        return node;
    }

    /** In-order traversal (sorted key order) -- use this for the inorder-traversal test. */
    public void inorder(java.util.function.BiConsumer<K, V> visitor) {
        inorder(root, visitor);
    }

    private void inorder(Node<K, V> node, java.util.function.BiConsumer<K, V> visitor) {
        if (node == null) return;
        inorder(node.left, visitor);
        visitor.accept(node.key, node.value);
        inorder(node.right, visitor);
    }

    /** Height of the tree -- log this per test case to demonstrate the
     *  BST-vs-balanced-tree degradation on sorted input. */
    public int height() {
        return height(root);
    }

    private int height(Node<K, V> node) {
        if (node == null) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
    }
}
