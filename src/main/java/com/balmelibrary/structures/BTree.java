package com.balmelibrary.structures;

/**
 * B-tree of minimum degree t (each non-root node has between t-1 and 2t-1
 * keys). Matches the guide's "B-tree (or a B-tree-page search simulation)"
 * allowance: insertion (with proactive node-splitting) and search are
 * implemented; deletion is a TODO (B-tree deletion has borrow/merge cases
 * comparable in effort to red-black deletion).
 *
 * Use the node-split events this class produces for the "node-split
 * explanation" diagram required in Section 4.2 of the guide -- print
 * before/after key arrays whenever splitChild() runs during your tests.
 *
 * Big-O: search/insert O(log n) with base ~t, and far fewer levels than a
 * binary tree for the same n -- the standard justification for choosing a
 * B-tree when node comparisons are cheap relative to disk/page reads.
 */
public class BTree<K extends Comparable<K>, V> {

    private final int minDegree; // t
    private Node root;

    private class Node {
        boolean leaf;
        int keyCount;
        @SuppressWarnings("unchecked")
        K[] keys = (K[]) new Comparable[2 * minDegree - 1];
        @SuppressWarnings("unchecked")
        V[] values = (V[]) new Object[2 * minDegree - 1];
        // Node is an inner class of a generic outer type, so Node[] would be
        // a generic array creation (disallowed) -- store as Object[] and cast.
        Object[] rawChildren = new Object[2 * minDegree];

        Node(boolean leaf) { this.leaf = leaf; }

        Node child(int i) { return (Node) rawChildren[i]; }
        void setChild(int i, Node n) { rawChildren[i] = n; }
    }

    public BTree(int minDegree) {
        if (minDegree < 2) throw new IllegalArgumentException("minDegree must be >= 2");
        this.minDegree = minDegree;
        this.root = new Node(true);
    }

    public V search(K key) {
        return search(root, key);
    }

    private V search(Node node, K key) {
        int i = 0;
        while (i < node.keyCount && key.compareTo(node.keys[i]) > 0) i++;
        if (i < node.keyCount && key.compareTo(node.keys[i]) == 0) {
            return node.values[i];
        }
        if (node.leaf) return null;
        return search(node.child(i), key);
    }

    public void insert(K key, V value) {
        Node r = root;
        if (r.keyCount == 2 * minDegree - 1) {
            Node newRoot = new Node(false);
            newRoot.setChild(0, r);
            splitChild(newRoot, 0);
            root = newRoot;
            insertNonFull(newRoot, key, value);
        } else {
            insertNonFull(r, key, value);
        }
    }

    private void splitChild(Node parent, int index) {
        Node full = parent.child(index);
        Node sibling = new Node(full.leaf);
        sibling.keyCount = minDegree - 1;

        for (int j = 0; j < minDegree - 1; j++) {
            sibling.keys[j] = full.keys[j + minDegree];
            sibling.values[j] = full.values[j + minDegree];
        }
        if (!full.leaf) {
            for (int j = 0; j < minDegree; j++) {
                sibling.setChild(j, full.child(j + minDegree));
            }
        }

        K midKey = full.keys[minDegree - 1];
        V midValue = full.values[minDegree - 1];
        full.keyCount = minDegree - 1;

        for (int j = parent.keyCount; j >= index + 1; j--) {
            parent.setChild(j + 1, parent.child(j));
        }
        parent.setChild(index + 1, sibling);

        for (int j = parent.keyCount - 1; j >= index; j--) {
            parent.keys[j + 1] = parent.keys[j];
            parent.values[j + 1] = parent.values[j];
        }
        parent.keys[index] = midKey;
        parent.values[index] = midValue;
        parent.keyCount++;

        // TODO(Data Structure Developer): emit a (before, after) key-array
        // snapshot here for the node-split diagram required by the guide.
    }

    private void insertNonFull(Node node, K key, V value) {
        int i = node.keyCount - 1;
        if (node.leaf) {
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                node.keys[i + 1] = node.keys[i];
                node.values[i + 1] = node.values[i];
                i--;
            }
            node.keys[i + 1] = key;
            node.values[i + 1] = value;
            node.keyCount++;
        } else {
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) i--;
            i++;
            if (node.child(i).keyCount == 2 * minDegree - 1) {
                splitChild(node, i);
                if (key.compareTo(node.keys[i]) > 0) i++;
            }
            insertNonFull(node.child(i), key, value);
        }
    }

    // TODO(Data Structure Developer): implement delete(K key) if the team
    // wants full B-tree deletion (borrow-from-sibling / merge cases).
    // Otherwise document the omission in the design note, same as
    // RedBlackTree.
}
