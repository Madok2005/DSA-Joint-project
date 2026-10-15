# Trace Tables — Search & Sort (Module M4, Algorithm Developer)

These traces use real rows from the team's `locations.csv` / `roads.csv`
(not synthetic filler data), per the brief's Table 8 evidence requirement.
Values were computed directly from the algorithm implementations in
`com.balmelibrary.algorithms`, so they will match what `mvn test` and
`Main` print exactly.

## 1. `binarySearch` — find `L034` in `locations.csv` sorted by `locationId`

**Precondition check:** `locations.csv` is already stored in ascending
`locationId` order (`L001..L050`), so `isSorted` passes and no exception is
thrown. Array has 50 elements, indices `0..49`.

| Step | low | high | mid | array[mid].locationId | Compare to L034 | Action |
|---|---|---|---|---|---|---|
| 1 | 0 | 49 | 24 | L025 | L025 < L034 | low = mid + 1 = 25 |
| 2 | 25 | 49 | 37 | L038 | L038 > L034 | high = mid - 1 = 36 |
| 3 | 25 | 36 | 30 | L031 | L031 < L034 | low = mid + 1 = 31 |
| 4 | 31 | 36 | 33 | **L034** | match | return index 33 |

**Result:** index 33, 4 comparisons, for n = 50 — matches ⌈log₂ 50⌉ = 6 as an
upper bound (4 ≤ 6, as expected since the target wasn't a worst-case leaf).

**Precondition-violation counterexample:** running the same search on
`roads.csv` (which is stored in `road_id` order) using a `distanceKm`
comparator throws `IllegalArgumentException` before any midpoint is
computed, instead of silently returning a wrong or missed index — see
`SearchAlgorithmsTest.rejectsUnsortedInput` and
`CsvBackedAlgorithmsIntegrationTest.binarySearchRejectsRoadsInRoadIdOrderWhenSearchedByDistance`.

## 2. `insertionSort` — first 6 rows of `roads.csv`, ordered by `distanceKm`

Initial array (in `roads.csv` file order):

| road_id | R001 | R002 | R003 | R004 | R005 | R006 |
|---|---|---|---|---|---|---|
| distance_km | 0.08 | 0.14 | 0.6 | 0.35 | 0.08 | 0.15 |

| i | key (road_id, km) | Array after pass i |
|---|---|---|
| 1 | (R002, 0.14) | R001·0.08, R002·0.14, R003·0.6, R004·0.35, R005·0.08, R006·0.15 |
| 2 | (R003, 0.6) | R001·0.08, R002·0.14, R003·0.6, R004·0.35, R005·0.08, R006·0.15 |
| 3 | (R004, 0.35) | R001·0.08, R002·0.14, **R004·0.35**, R003·0.6, R005·0.08, R006·0.15 |
| 4 | (R005, 0.08) | R001·0.08, **R005·0.08**, R002·0.14, R004·0.35, R003·0.6, R006·0.15 |
| 5 | (R006, 0.15) | R001·0.08, R005·0.08, R002·0.14, **R006·0.15**, R004·0.35, R003·0.6 |

**Final order:** R001(0.08), R005(0.08), R002(0.14), R006(0.15), R004(0.35), R003(0.6).
Note R001 stays before R005 despite equal distance — this is the stability
property required for a deterministic, reproducible dispatch/route ordering.

## 3. `mergeSort` — merge step trace on the same 6-value array

Split: `[R001,R002,R003]` (left) vs `[R004,R005,R006]` (right), each already
sorted individually by recursion before this final merge:

- Left run (sorted): R001·0.08, R002·0.14, R003·0.6
- Right run (sorted): R005·0.08, R004·0.35, R006·0.15 → after its own
  recursive sort: R005·0.08, R006·0.15, R004·0.35

| Step | i (left) | j (right) | Compare | Write to output | Output so far |
|---|---|---|---|---|---|
| 1 | R001·0.08 | R005·0.08 | 0.08 ≤ 0.08 → take left | R001 | R001 |
| 2 | R002·0.14 | R005·0.08 | 0.14 > 0.08 → take right | R005 | R001, R005 |
| 3 | R002·0.14 | R006·0.15 | 0.14 ≤ 0.15 → take left | R002 | R001, R005, R002 |
| 4 | R003·0.6 | R006·0.15 | 0.6 > 0.15 → take right | R006 | R001, R005, R002, R006 |
| 5 | R003·0.6 | R004·0.35 | 0.6 > 0.35 → take right | R004 | R001, R005, R002, R006, R004 |
| 6 | R003·0.6 | (right exhausted) | copy remaining left | R003 | R001, R005, R002, R006, R004, R003 |

**Result** matches insertion sort's result exactly (both stable, both correct):
R001(0.08), R005(0.08), R002(0.14), R006(0.15), R004(0.35), R003(0.6).

## 4. Complexity summary

| Algorithm | Best | Average | Worst | Space | Stable? |
|---|---|---|---|---|---|
| `linearSearch` | O(1) | O(n) | O(n) | O(1) | n/a |
| `binarySearch` | O(1) | O(log n) | O(log n) | O(1) | n/a |
| `insertionSort` | O(n) | O(n²) | O(n²) | O(1) | yes |
| `selectionSort` | O(n²) | O(n²) | O(n²) | O(1) | no |
| `mergeSort` | O(n log n) | O(n log n) | O(n log n) | O(n) | yes |

`binarySearch`'s O(log n) figure is for `binarySearchUnchecked`; the
precondition-checking `binarySearch` entry point adds an O(n)
`isSorted` scan on top, which is worth stating explicitly in the report
alongside the stated precondition.
