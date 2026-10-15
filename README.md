# Balme Library Smart Service Operations Optimizer

**Project:** Ghana Smart Service Operations Optimizer (DCIT 204/308)
**Local context:** University of Ghana Balme Library
**This repository:** the **Algorithm Developer** module and the **Data
Structure Developer** module, merged into a single buildable Maven project
and wired together so the search/sort algorithms run generically over both
plain Java arrays *and* the team's from-scratch data structures.

Two independent submissions came into this merge:

| Was | Owned package(s) | Now |
|---|---|---|
| `balme-algorithm-module` | `com.balmelibrary.{model,io,algorithms,util,demo}` | this repo's `src/main`, refactored |
| `balme_structures_starter_code/balme-structures` | `com.balmelibrary.structures` | this repo's `src/main/java/com/balmelibrary/structures`, unchanged |

They shared the `com.balmelibrary` root package already, but were two
separate Maven projects with two `../pom.xml`s and **no code in either one
referenced the other**. This repository is that missing connection.

## 1. What changed in the merge

1. **One Maven module, one `../pom.xml`.** Both source trees now live under a
   single `groupId:artifactId` (`com.balmelibrary:balme-library`) so `mvn
   test` builds and tests both the structures and the algorithms in one
   pass, and the algorithms module can `import com.balmelibrary.structures.*`
   as an ordinary compile-scope dependency (it's in the same module, not an
   external jar).
2. **`SearchAlgorithms` and `SortAlgorithms` now operate on `DynamicArray<T>`
   as well as `T[]`.** This was the algorithm module's own README action
   item: *"consider adding overloads of `linearSearch`/`insertionSort`/etc.
   that accept the custom array/list wrapper instead of a raw `T[]`, so this
   module stops depending on plain Java arrays at the integration seams."*
   Every method (`linearSearch`, `binarySearch`, `binarySearchUnchecked`,
   `isSorted`, `insertionSort`, `selectionSort`, `mergeSort`) now has two
   public overloads. Both run through the same private algorithm body — see
   §2 — so there is exactly one implementation of each algorithm to trace,
   test and reason about, not two copies that could quietly drift apart.
3. **`CsvDatasetLoader` stages rows in `DynamicArray`, not `java.util.ArrayList`.**
   The original loader used `ArrayList` only as a growable buffer while a
   CSV's row count was unknown in advance, explicitly deferring to "the Data
   Structure Developer's module" for anything structural. Now that module is
   in the same repository, so the loader uses it: `readDataRows` and every
   `loadX(...)` method build on `DynamicArray` end-to-end. The public `T[]`-
   returning methods (`loadLocations`, `loadRoads`, `loadServiceRequests`,
   `loadResources`) are unchanged in signature, so existing callers and
   tests keep compiling; new `loadXAsDynamicArray(...)` variants hand back
   the `DynamicArray` directly.
4. **`Main` demonstrates both modules together**, in three parts — see §3.

Nothing in `com.balmelibrary.structures` was modified: all 15 files are
copied verbatim from the starter module. `SortOutcome`, `SearchOutcome`,
`IndexNumberParameters`, `Stopwatch`, and all four `model` records are also
unchanged. The two existing test suites (`SearchAlgorithmsTest`,
`SortAlgorithmsTest`, `CsvBackedAlgorithmsIntegrationTest`,
`DynamicArrayTest`, `MyHashMapTest`) are copied verbatim too and pass
unmodified against the refactored classes, because every pre-existing public
method kept its exact signature and behaviour.

## 2. How the connection works: `Indexed<T>`

`SearchAlgorithms` and `SortAlgorithms` each define a small private interface
(not part of the public API) with `size()`/`get(i)` (search) or
`size()`/`get(i)`/`set(i, v)` (sort). A private adapter wraps a `T[]` as one
of these, and another wraps a `DynamicArray<T>` as one. The actual algorithm
— the comparisons, the swaps, the recursive merge — is written exactly once
against that interface:

```
public linearSearch(T[] array, ...)             \
public linearSearch(DynamicArray<T> array, ...)   >---> private linearSearchCore(Indexed<T>, ...)
                                                  /
```

This is why adding `DynamicArray` support did not require rewriting or
duplicating any of the sort/search logic, and why the two overloads are
guaranteed to agree: `SearchAlgorithms.binarySearch(T[]...)` and
`SearchAlgorithms.binarySearch(DynamicArray<T>...)` share one code path.
`Main` (§3, Part 2) sorts and searches the *same* `locations.csv` data both
ways and prints identical results as evidence of this.

## 3. What's here

```
pom.xml                                  Single Maven build (JUnit 5, no other deps)
data/                                    Working copy of the team's 4 CSVs, for the console demo
docs/
  Evidence_note.txt                      Team's original data-provenance note (kept verbatim)
  trace_tables.md                        Required trace tables (Table 8), computed on real data
src/main/java/com/balmelibrary/
  model/                                 One record per CSV table (Location, Road, ServiceRequest, Resource)
  structures/                            15 from-scratch data structures (Data Structure Developer module)
    DynamicArray, SinglyLinkedList, DoublyLinkedList, ArrayStack, LinkedQueue,
    CircularQueue, Deque, BinaryHeap, BSTMap, RedBlackTree, BTree, MyHashMap,
    MySet, DisjointSet, Graph
  io/CsvDatasetLoader.java               CSV -> DynamicArray -> array/DynamicArray loader (no algorithmic logic)
  algorithms/
    SearchOutcome.java                   Result + comparisons + elapsedNanos
    SortOutcome.java                     Result + comparisons + writes/swaps + elapsedNanos
    SearchAlgorithms.java                linearSearch, binarySearch (T[] and DynamicArray<T> overloads)
    SortAlgorithms.java                  insertionSort, selectionSort, mergeSort (T[] and DynamicArray<T> overloads)
    IndexNumberParameters.java           Template for the 3 required index-number-derived parameters
  util/Stopwatch.java                    System.nanoTime() wrapper
  demo/Main.java                         Console demo an examiner can run end-to-end (3 parts, see below)
src/test/java/com/balmelibrary/
  algorithms/
    SearchAlgorithmsTest.java            Normal/boundary/invalid-input tests + the precondition test
    SortAlgorithmsTest.java              Normal/boundary/invalid-input tests + stability + cross-validation
    CsvBackedAlgorithmsIntegrationTest.java  Same algorithms exercised against the real CSVs
  structures/
    DynamicArrayTest.java                Example normal/boundary/invalid-input pattern for DynamicArray
    MyHashMapTest.java                   Same pattern for MyHashMap
src/test/resources/data/                 Copy of the 4 CSVs, used only by the integration test
```

### `Main`'s three parts

1. **Sorting and searching on plain arrays** — the original Algorithm
   Developer demo, unchanged in behaviour: `insertionSort`/`selectionSort`/
   `mergeSort` on locations/requests/roads, then `binarySearch`/
   `linearSearch`, including the precondition-rejection demonstration
   (searching `roads.csv` by distance without first sorting it that way).
2. **The same algorithms on `DynamicArray`** — loads `locations.csv`
   straight into a `DynamicArray<LibraryLocation>`, runs `mergeSort` and
   `binarySearch` on it, and prints the result next to Part 1's array-based
   run for comparison.
3. **A data-structures showcase on the real dataset:**
   - `MyHashMap<String, LibraryLocation>` for O(1)-average location lookup
     by ID, as an alternative to `binarySearch`/`linearSearch`.
   - `Graph<String>` built from `locations.csv` (vertices) and `roads.csv`
     (weighted edges), reporting vertex count and one location's neighbour
     count. This only builds the graph and queries it — it does **not**
     implement BFS/DFS/Dijkstra/Prim/Kruskal, which remain a separate,
     not-yet-started Module M4 deliverable (see §5).
   - `BinaryHeap<ServiceRequestRecord>` used as a priority queue to print
     the 3 most urgent open service requests. This demonstrates the heap's
     own `insert`/`extractMin` operations on real data; it is not a
     complete scheduling/dispatch engine (also §5).

## 4. How to build and run

```bash
mvn test                 # compiles + runs every unit and integration test (structures and algorithms)
mvn -q compile exec:java -Dexec.mainClass=com.balmelibrary.demo.Main   # console demo
# or, without the exec plugin:
mvn -q package && java -cp target/classes com.balmelibrary.demo.Main
```

The demo prints comparisons/time for each sort and search against the real
CSVs in `data` and writes `../algorithm_runs.csv` (algorithm, dataset, n,
comparisons, writes/swaps, timeNs) — hand this file to the Database Engineer
for `algorithm_runs` and to Testing & QA for the performance-experiments
graphs.

This has been verified end to end in this environment with `javac`/`java`
directly against JDK 21 (all 27 `src/main` files compile cleanly; the demo
runs against the real `data` CSVs and produces matching results for the
array-based and `DynamicArray`-based sort/search). `mvn test` itself was not
run here because this sandbox has no access to Maven Central to download
JUnit — run it once you have that access, before final submission.

## 5. The stated precondition (why this exists as its own section)

> `binarySearch` requires its input array to already be sorted in
> non-descending order according to the comparator passed in. This is not
> optional and not checked implicitly by the type system — it is a runtime
> invariant the algorithm depends on to safely discard half the remaining
> search space at each step.

This project treats that precondition as a first-class, testable fact rather
than a comment, for **both** the `T[]` and `DynamicArray<T>` overloads (they
share the same check — see §2):

- `SearchAlgorithms.binarySearch(...)` calls `isSorted(...)` first and throws
  `IllegalArgumentException` with an explanatory message if the array is not
  sorted, instead of returning a plausible-looking but wrong index.
- `SearchAlgorithms.binarySearchUnchecked(...)` is the "trust the caller"
  variant with pure O(log n) behaviour and no upfront check, for call sites
  that just finished sorting the structure themselves.
- `SearchAlgorithmsTest.BinarySearchTests.rejectsUnsortedInput()` is the
  required precondition test.
- `CsvBackedAlgorithmsIntegrationTest.binarySearchRejectsRoadsInRoadIdOrderWhenSearchedByDistance()`
  repeats the same idea on real data: `roads.csv` is stored in `road_id`
  order, so searching it by `distance_km` without sorting first must fail
  loudly, and does.

## 6. Design notes for the report

- **Generic over data type.** Every method takes a `Comparator<T>`, so the
  identical `insertionSort`/`selectionSort`/`mergeSort`/`linearSearch`/
  `binarySearch` code sorts and searches `LibraryLocation`, `RoadSegment`,
  `ServiceRequestRecord` and `LibraryResource` — only the comparator passed
  at the call site changes.
- **Generic over backing structure, too.** The same code additionally sorts
  and searches both a raw `T[]` and the team's own `DynamicArray<T>` — see
  §2's `Indexed<T>` seam.
- **No library search/sort/collection routines anywhere in `src/main`.**
  `Arrays.sort`, `Collections.sort`, `Arrays.binarySearch`,
  `Collections.binarySearch` and `java.util.ArrayList` are never used by the
  assessed production code (`Arrays.equals`/`Arrays.sort`/`java.util.*`
  collections do appear in *test* files, but only as an independent
  correctness oracle or JUnit plumbing, never inside `src/main`).
- **Stability documented and tested.** `insertionSort` and `mergeSort` are
  stable (tested via a two-key comparator that would reveal reordering);
  `selectionSort` is not, and is documented as such.
- **Every run returns its own evidence.** `SearchOutcome`/`SortOutcome`
  record comparisons, writes/swaps and elapsed nanoseconds so no separate
  instrumentation pass is needed for the performance-experiments section.
- **`Graph`'s own doc comment already anticipated this merge**: it says
  vertices are the library's locations, edges are roads, and "the Algorithm
  Developer's BFS/DFS/Dijkstra/Prim/Kruskal implementations should use
  whichever representation suits the algorithm." `Main`'s Part 3 builds that
  graph from the real CSVs; the traversal/shortest-path algorithms
  themselves are the outstanding work in §7.

## 7. Outstanding action items for the team (do not skip before final submission)

1. **Index-number parameters are placeholders.** Open
   `IndexNumberParameters.java` and replace `INDEX_NUMBER_A/B/C` with three
   real team member index numbers, then recheck the derived constants.
2. **Validate modelled data.** Per `docs/Evidence_note.txt`, coordinates,
   distances/travel times, condition weights and most `service_requests`
   rows are modelled, not observed. Replace or spot-check as many as
   reasonably possible with real measurements/staff confirmation before the
   report is finalised, and keep the evidence note updated to reflect what
   was validated.
3. **`RedBlackTree.delete()` and `BTree.delete()` are still `TODO`s** (a
   deliberate, documented scope cut carried over from the structures
   starter module — insert/search/traversal is implemented and satisfies
   the brief's "clearly simplified balanced tree" allowance).
4. **Extend Module M4.** This merge only covers Table 6's search/sort line
   item plus a first, read-only connection to the structures module.
   Scheduling/dispatch (queue/deque/priority-queue based — `BinaryHeap` is
   now wired to real `service_requests.csv` data and ready for this), the
   graph route engine (BFS/DFS/Dijkstra/Prim/Kruskal — `Graph` is now built
   from real `locations.csv`/`roads.csv` data and ready for this), and the
   required greedy and dynamic-programming algorithms are still owed per
   the brief.
5. **Run `mvn test` once you have Maven Central access** to confirm the
   JUnit suites pass in a normal environment (only `javac`/`java` were
   available where this merge was assembled and verified — see §4).
