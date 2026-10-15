package com.balmelibrary.structures;

/**
 * Disjoint-set / union-find over integer indices 0..n-1, with path
 * compression (find) and union by rank. Used by Algorithm Developer's
 * Kruskal implementation to detect cycles while building the minimum
 * connecting network across library zones, and by the "check zone
 * connectivity" part of the core problem statement.
 *
 * If your locations are IDs (e.g. Strings) rather than 0..n-1 integers,
 * build an index map (String -> int) in the Database Engineer's loader
 * and pass indices into this structure.
 *
 * Big-O: find/union effectively O(alpha(n)) (inverse Ackermann,
 * near-constant) with both path compression and union by rank applied.
 */
public class DisjointSet {

    private final int[] parent;
    private final int[] rank;
    private int componentCount;

    public DisjointSet(int n) {
        parent = new int[n];
        rank = new int[n];
        componentCount = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    /** Path-compressed find: every node visited is re-pointed straight at the root. */
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    /** Returns true if this call actually merged two different components
     *  (useful for Kruskal: only accept the edge when union() returns true). */
    public boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) return false;

        if (rank[rootA] < rank[rootB]) {
            parent[rootA] = rootB;
        } else if (rank[rootA] > rank[rootB]) {
            parent[rootB] = rootA;
        } else {
            parent[rootB] = rootA;
            rank[rootA]++;
        }
        componentCount--;
        return true;
    }

    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    /** Number of distinct components remaining -- 1 means "fully connected". */
    public int componentCount() {
        return componentCount;
    }
}
