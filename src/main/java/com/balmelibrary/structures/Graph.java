package com.balmelibrary.structures;

import java.util.NoSuchElementException;

/**
 * Weighted graph supporting both an adjacency list and an adjacency matrix
 * representation, built from the custom structures above (no java.util
 * collections). Vertices are the library's locations; edges are roads.
 *
 * Design choice: vertices are identified by a generic label V (e.g. the
 * location ID/name string), mapped internally to a dense int index so the
 * adjacency matrix can be a plain double[][]. The Algorithm Developer's
 * BFS/DFS/Dijkstra/Prim/Kruskal implementations should use whichever
 * representation suits the algorithm (matrix for Prim's O(V^2) form and
 * getWeight lookups, list for BFS/DFS/Dijkstra's neighbour iteration).
 *
 * Big-O:
 *   addVertex           O(1) amortised
 *   addEdge             O(1) (list) -- matrix write is O(1) too
 *   neighbors(v)         O(degree(v)) via the list
 *   getWeight(u, v)      O(1) via the matrix, O(degree(u)) via the list
 */
public class Graph<V> {

    public static class Edge<V> {
        public final V target;
        public final double weight;
        Edge(V target, double weight) { this.target = target; this.weight = weight; }
    }

    private final MyHashMap<V, Integer> indexOf = new MyHashMap<>();
    private final DynamicArray<V> vertices = new DynamicArray<>();
    private final DynamicArray<SinglyLinkedList<Edge<V>>> adjacencyList = new DynamicArray<>();
    private double[][] adjacencyMatrix; // rebuilt lazily; null means "stale, needs rebuild"
    private final boolean directed;

    public Graph(boolean directed) {
        this.directed = directed;
    }

    public void addVertex(V v) {
        if (indexOf.containsKey(v)) return;
        indexOf.put(v, vertices.size());
        vertices.add(v);
        adjacencyList.add(new SinglyLinkedList<>());
        adjacencyMatrix = null; // matrix is now stale
    }

    public void addEdge(V from, V to, double weight) {
        addVertex(from);
        addVertex(to);
        int fromIdx = indexOf.get(from);
        adjacencyList.get(fromIdx).addLast(new Edge<>(to, weight));
        if (!directed) {
            int toIdx = indexOf.get(to);
            adjacencyList.get(toIdx).addLast(new Edge<>(from, weight));
        }
        adjacencyMatrix = null; // matrix is now stale
    }

    public int vertexCount() { return vertices.size(); }

    public Iterable<V> vertices() { return vertices; }

    public int indexOf(V v) {
        Integer idx = indexOf.get(v);
        if (idx == null) throw new NoSuchElementException("no such vertex: " + v);
        return idx;
    }

    public V vertexAt(int index) { return vertices.get(index); }

    /** Neighbours of v as (target, weight) pairs -- use for BFS/DFS/Dijkstra. */
    public SinglyLinkedList<Edge<V>> neighbors(V v) {
        return adjacencyList.get(indexOf(v));
    }

    /** Builds (or returns the cached) adjacency matrix. Double.POSITIVE_INFINITY
     *  means "no edge" -- use that sentinel consistently in Dijkstra/Prim. */
    public double[][] adjacencyMatrix() {
        if (adjacencyMatrix != null) return adjacencyMatrix;
        int n = vertices.size();
        double[][] matrix = new double[n][n];
        for (double[] row : matrix) java.util.Arrays.fill(row, Double.POSITIVE_INFINITY);
        for (int i = 0; i < n; i++) {
            for (Edge<V> edge : adjacencyList.get(i)) {
                int j = indexOf(edge.target);
                matrix[i][j] = edge.weight;
            }
        }
        adjacencyMatrix = matrix;
        return matrix;
    }

    public double getWeight(V from, V to) {
        double[][] matrix = adjacencyMatrix();
        return matrix[indexOf(from)][indexOf(to)];
    }
}
