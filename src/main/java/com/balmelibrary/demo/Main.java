package com.balmelibrary.demo;

import com.balmelibrary.algorithms.SearchAlgorithms;
import com.balmelibrary.algorithms.SearchOutcome;
import com.balmelibrary.algorithms.SortAlgorithms;
import com.balmelibrary.algorithms.SortOutcome;
import com.balmelibrary.io.CsvDatasetLoader;
import com.balmelibrary.model.LibraryLocation;
import com.balmelibrary.model.RoadSegment;
import com.balmelibrary.model.ServiceRequestRecord;
import com.balmelibrary.structures.BinaryHeap;
import com.balmelibrary.structures.DynamicArray;
import com.balmelibrary.structures.Graph;
import com.balmelibrary.structures.MyHashMap;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Console demonstration an examiner can run end-to-end (project brief,
 * Section 8) to see the search/sort algorithms and the custom data
 * structures operate together on the real Balme Library dataset, and to
 * export raw timing data for the {@code algorithm_runs} table and
 * performance-experiments section.
 *
 * <p>Three parts:</p>
 * <ol>
 *   <li><b>Sorting and searching on plain arrays</b> -- the original
 *       Algorithm Developer demo, unchanged in behaviour.</li>
 *   <li><b>The same sorting and searching on {@link DynamicArray}</b> -- the
 *       Data Structure Developer's from-scratch resizable array, proving the
 *       two modules' new overloads (see {@code SearchAlgorithms} and
 *       {@code SortAlgorithms}) produce identical results to the array
 *       versions.</li>
 *   <li><b>A data-structures showcase against the same dataset</b> --
 *       {@link MyHashMap} for O(1) location lookup by ID, {@link Graph} built
 *       from {@code roads.csv} for O(degree) neighbour queries, and
 *       {@link BinaryHeap} as an urgency-ordered priority queue over
 *       {@code service_requests.csv}. This only exercises each structure's
 *       own native operations; it is not a routing/scheduling algorithm
 *       implementation, which remains a separate Module M4 deliverable.</li>
 * </ol>
 *
 * <p>Usage: {@code mvn -q exec:java -Dexec.mainClass=com.balmelibrary.demo.Main}
 * or run the compiled class with {@code data/} on the working directory,
 * where {@code data/} contains locations.csv, roads.csv,
 * service_requests.csv and resources.csv (already provided in this
 * repository).</p>
 */
public final class Main {

    public static void main(String[] args) throws IOException {
        Path dataDir = Path.of(args.length > 0 ? args[0] : "data");

        LibraryLocation[] locations = CsvDatasetLoader.loadLocations(dataDir.resolve("locations.csv"));
        RoadSegment[] roads = CsvDatasetLoader.loadRoads(dataDir.resolve("roads.csv"));
        ServiceRequestRecord[] requests = CsvDatasetLoader.loadServiceRequests(dataDir.resolve("service_requests.csv"));

        System.out.println("Loaded " + locations.length + " locations, " + roads.length
                + " roads, " + requests.length + " service requests from " + dataDir.toAbsolutePath());
        System.out.println();

        try (PrintWriter runsLog = new PrintWriter("algorithm_runs.csv")) {
            runsLog.println("algorithm,dataset,n,comparisons,writesOrSwaps,timeNs");

            System.out.println("== Part 1: sorting and searching on plain arrays ==");
            demoSort("insertionSort", "locations.csv (by name)", locations.clone(),
                    Comparator.comparing(LibraryLocation::name), runsLog);
            demoSort("selectionSort", "service_requests.csv (by urgency)", requests.clone(),
                    Comparator.comparingInt(ServiceRequestRecord::urgency), runsLog);
            demoSort("mergeSort", "roads.csv (by distance_km)", roads.clone(),
                    Comparator.comparingDouble(RoadSegment::distanceKm), runsLog);

            System.out.println();
            demoSearch(locations, roads, requests, runsLog);

            System.out.println();
            System.out.println("== Part 2: the same algorithms on the Data Structure Developer's DynamicArray ==");
            demoDynamicArrayAlgorithms(dataDir, runsLog);

            System.out.println();
            System.out.println("== Part 3: data structures showcase on the real dataset ==");
            demoHashMapLookup(locations);
            demoGraphFromRoads(locations, roads);
            demoPriorityQueueByUrgency(requests);
        }

        System.out.println();
        System.out.println("Run log written to algorithm_runs.csv");
    }

    // ------------------------------------------------------------------
    // Part 1: plain-array sort/search (original Algorithm Developer demo)
    // ------------------------------------------------------------------

    private static <T> void demoSort(String label, String dataset, T[] array,
                                      Comparator<T> comparator, PrintWriter runsLog) {
        SortOutcome outcome = switch (label) {
            case "insertionSort" -> SortAlgorithms.insertionSort(array, comparator);
            case "selectionSort" -> SortAlgorithms.selectionSort(array, comparator);
            case "mergeSort" -> SortAlgorithms.mergeSort(array, comparator);
            default -> throw new IllegalArgumentException("Unknown sort: " + label);
        };
        System.out.printf("%-14s %-34s n=%-4d comparisons=%-6d writes/swaps=%-6d time=%,dns%n",
                label, dataset, array.length, outcome.comparisons(), outcome.swapsOrWrites(), outcome.elapsedNanos());
        runsLog.printf("%s,%s,%d,%d,%d,%d%n", label, dataset, array.length,
                outcome.comparisons(), outcome.swapsOrWrites(), outcome.elapsedNanos());
    }

    private static void demoSearch(LibraryLocation[] locations, RoadSegment[] roads,
                                    ServiceRequestRecord[] requests, PrintWriter runsLog) {
        System.out.println("-- Searching --");

        // 1) binarySearch on data sorted by its own key (precondition satisfied).
        LibraryLocation[] byId = locations.clone();
        SortAlgorithms.mergeSort(byId, Comparator.naturalOrder()); // sorts by locationId
        LibraryLocation probe = new LibraryLocation("L034", "", "", "", 0, 0);
        SearchOutcome binaryHit = SearchAlgorithms.binarySearch(byId, probe, Comparator.naturalOrder());
        System.out.println("binarySearch for L034 in sorted locations -> "
                + (binaryHit.found() ? byId[binaryHit.index()].name() : "NOT FOUND")
                + " (comparisons=" + binaryHit.comparisons() + ", time=" + binaryHit.elapsedNanos() + "ns)");
        runsLog.printf("binarySearch,locations.csv (sorted by locationId),%d,%d,0,%d%n",
                byId.length, binaryHit.comparisons(), binaryHit.elapsedNanos());

        // 2) linearSearch needs no sortedness; run it on service_requests as-is.
        ServiceRequestRecord lastRequest = requests[requests.length - 1];
        SearchOutcome linearHit = SearchAlgorithms.linearSearch(requests, lastRequest,
                Comparator.comparing(ServiceRequestRecord::requestId));
        System.out.println("linearSearch for " + lastRequest.requestId() + " in service_requests -> index "
                + linearHit.index() + " (comparisons=" + linearHit.comparisons() + ")");
        runsLog.printf("linearSearch,service_requests.csv,%d,%d,0,%d%n",
                requests.length, linearHit.comparisons(), linearHit.elapsedNanos());

        // 3) PRECONDITION DEMONSTRATION: roads.csv is stored in road_id order,
        //    not distance order, so binary-searching it by distance must be
        //    rejected rather than silently returning a wrong or missed index.
        Comparator<RoadSegment> byDistance = Comparator.comparingDouble(RoadSegment::distanceKm);
        try {
            SearchAlgorithms.binarySearch(roads, roads[0], byDistance);
            System.out.println("UNEXPECTED: binarySearch accepted unsorted input.");
        } catch (IllegalArgumentException expected) {
            System.out.println("binarySearch correctly rejected roads.csv sorted by road_id "
                    + "when searched by distance_km: " + expected.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Part 2: the same algorithms, driven off DynamicArray instead of T[]
    // ------------------------------------------------------------------

    private static void demoDynamicArrayAlgorithms(Path dataDir, PrintWriter runsLog) throws IOException {
        DynamicArray<LibraryLocation> locations =
                CsvDatasetLoader.loadLocationsAsDynamicArray(dataDir.resolve("locations.csv"));

        SortOutcome sortOutcome = SortAlgorithms.mergeSort(locations, Comparator.naturalOrder());
        System.out.printf("mergeSort (DynamicArray<LibraryLocation>, by locationId) n=%-4d "
                        + "comparisons=%-6d writes=%-6d time=%,dns%n",
                locations.size(), sortOutcome.comparisons(), sortOutcome.swapsOrWrites(), sortOutcome.elapsedNanos());
        runsLog.printf("mergeSort,locations.csv (DynamicArray, by locationId),%d,%d,%d,%d%n",
                locations.size(), sortOutcome.comparisons(), sortOutcome.swapsOrWrites(), sortOutcome.elapsedNanos());

        LibraryLocation probe = new LibraryLocation("L034", "", "", "", 0, 0);
        SearchOutcome searchOutcome = SearchAlgorithms.binarySearch(locations, probe, Comparator.naturalOrder());
        System.out.println("binarySearch (DynamicArray) for L034 -> "
                + (searchOutcome.found() ? locations.get(searchOutcome.index()).name() : "NOT FOUND")
                + " (comparisons=" + searchOutcome.comparisons() + ")");
        runsLog.printf("binarySearch,locations.csv (DynamicArray, sorted by locationId),%d,%d,0,%d%n",
                locations.size(), searchOutcome.comparisons(), searchOutcome.elapsedNanos());
    }

    // ------------------------------------------------------------------
    // Part 3: data structures showcase
    // ------------------------------------------------------------------

    /** MyHashMap: O(1) average location lookup by ID, an alternative to binarySearch/linearSearch. */
    private static void demoHashMapLookup(LibraryLocation[] locations) {
        MyHashMap<String, LibraryLocation> byId = new MyHashMap<>();
        for (LibraryLocation location : locations) {
            byId.put(location.locationId(), location);
        }
        LibraryLocation hit = byId.get("L034");
        System.out.println("MyHashMap.get(\"L034\") -> "
                + (hit != null ? hit.name() : "NOT FOUND")
                + " (size=" + byId.size() + ", loadFactor=" + String.format("%.2f", byId.loadFactor())
                + ", collisions=" + byId.collisionCount() + ")");
    }

    /** Graph: builds the location/road network so future BFS/DFS/Dijkstra work has real adjacency data. */
    private static void demoGraphFromRoads(LibraryLocation[] locations, RoadSegment[] roads) {
        Graph<String> network = new Graph<>(false); // roads are corridors, not one-way
        for (LibraryLocation location : locations) {
            network.addVertex(location.locationId());
        }
        for (RoadSegment road : roads) {
            network.addEdge(road.fromLocationId(), road.toLocationId(), road.distanceKm());
        }

        String hub = locations[0].locationId();
        int degree = 0;
        for (Graph.Edge<String> ignored : network.neighbors(hub)) {
            degree++;
        }
        System.out.println("Graph built from roads.csv: " + network.vertexCount() + " vertices, "
                + hub + " has " + degree + " direct neighbours");
    }

    /** BinaryHeap as a priority queue: the 3 most urgent service requests, highest urgency first. */
    private static void demoPriorityQueueByUrgency(ServiceRequestRecord[] requests) {
        Comparator<ServiceRequestRecord> byUrgencyDescending =
                Comparator.comparingInt(ServiceRequestRecord::urgency).reversed();
        BinaryHeap<ServiceRequestRecord> priorityQueue = new BinaryHeap<>(byUrgencyDescending);
        for (ServiceRequestRecord request : requests) {
            priorityQueue.insert(request);
        }

        System.out.println("BinaryHeap top-3 most urgent service requests:");
        for (int i = 0; i < 3 && !priorityQueue.isEmpty(); i++) {
            ServiceRequestRecord top = priorityQueue.extractMin(); // "min" per comparator = most urgent here
            System.out.println("  " + top.requestId() + " urgency=" + top.urgency()
                    + " category=" + top.category() + " status=" + top.status());
        }
    }

    private Main() {
    }
}
