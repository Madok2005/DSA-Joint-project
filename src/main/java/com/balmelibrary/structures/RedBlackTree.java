package com.balmelibrary.structures;

/**
 * Red-black tree, key-value map (no java.util.TreeMap).
 *
 * STARTER SCOPE (matches the guide's "or a clearly simplified balanced
 * tree" allowance): insertion with full recolour/rotation fixup, search,
 * and in-order traversal are implemented below. Deletion is intentionally
 * left as a TODO -- full red-black deletion has ~6 fixup cases and is a
 * substantial extra piece of work. If your team wants full marks on this
 * structure, implement deleteFixup() following CLRS chapter 13; otherwise
 * document in your design note that deletion is unsupported by design and
 * cite the guide's allowance for a simplified balanced tree.
 *
 * Big-O: put/get O(log n) guaranteed (height <= 2*log2(n+1)).
 * Use this for the same put/get workload as BSTMap so QA can plot both on
 * the same "BST vs balanced tree" performance chart.
 */
public class RedBlackTree<K extends Comparable<K>, V> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class Node {
        K key;
        V value;
        Node left, right;
        boolean color;
        Node(K key, V value, boolean color) {
            this.key = key;
            this.value = value;
            this.color = color;
        }
    }

    private Node root;
    private int size;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    private boolean isRed(Node node) {
        return node != null && node.color == RED;
    }

    public void put(K key, V value) {
        root = put(root, key, value);
        root.color = BLACK;
    }

    private Node put(Node node, K key, V value) {
        if (node == null) {
            size++;
            return new Node(key, value, RED);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) node.left = put(node.left, key, value);
        else if (cmp > 0) node.right = put(node.right, key, value);
        else node.value = value;

        // Standard CLRS-style fixup, expressed as three local rebalancing rules.
        if (isRed(node.right) && !isRed(node.left)) node = rotateLeft(node);
        if (isRed(node.left) && isRed(node.left.left)) node = rotateRight(node);
        if (isRed(node.left) && isRed(node.right)) flipColors(node);

        return node;
    }

    private Node rotateLeft(Node node) {
        Node right = node.right;
        node.right = right.left;
        right.left = node;
        right.color = node.color;
        node.color = RED;
        return right;
    }

    private Node rotateRight(Node node) {
        Node left = node.left;
        node.left = left.right;
        left.right = node;
        left.color = node.color;
        node.color = RED;
        return left;
    }

    private void flipColors(Node node) {
        node.color = RED;
        node.left.color = BLACK;
        node.right.color = BLACK;
    }

    public V get(K key) {
        Node node = root;
        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp < 0) node = node.left;
            else if (cmp > 0) node = node.right;
            else return node.value;
        }
        return null;
    }

    public boolean contains(K key) {
        return get(key) != null;
    }

    // TODO(Data Structure Developer): implement delete(K key) + deleteFixup
    // if the team wants full red-black deletion. Document the decision
    // either way in the design note.

    public void inorder(java.util.function.BiConsumer<K, V> visitor) {
        inorder(root, visitor);
    }

    private void inorder(Node node, java.util.function.BiConsumer<K, V> visitor) {
        if (node == null) return;
        inorder(node.left, visitor);
        visitor.accept(node.key, node.value);
        inorder(node.right, visitor);
    }

    public int height() {
        return height(root);
    }

    private int height(Node node) {
        if (node == null) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
    }
}
