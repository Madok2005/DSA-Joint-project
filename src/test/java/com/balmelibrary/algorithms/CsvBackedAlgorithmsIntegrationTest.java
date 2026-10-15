package com.balmelibrary.algorithms;

import com.balmelibrary.io.CsvDatasetLoader;
import com.balmelibrary.model.LibraryLocation;
import com.balmelibrary.model.RoadSegment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests that exercise the search and sort algorithms against the
 * real Balme Library dataset (locations.csv, roads.csv), instead of synthetic
 * arrays only. This is the "use the project files as resources" evidence for
 * the Algorithm Developer module -- run against
 * {@code src/test/resources/data}, a copy of the team's own CSVs.
 */
class CsvBackedAlgorithmsIntegrationTest {

    private static LibraryLocation[] locations;
    private static RoadSegment[] roads;

    @BeforeAll
    static void loadDataset() throws IOException, URISyntaxException {
        Path dataDir = Paths.get(CsvBackedAlgorithmsIntegrationTest.class
                .getResource("/data/locations.csv").toURI()).getParent();
        locations = CsvDatasetLoader.loadLocations(dataDir.resolve("locations.csv"));
        roads = CsvDatasetLoader.loadRoads(dataDir.resolve("roads.csv"));
    }

    @Test
    @DisplayName("dataset loads with the record counts stated in Evidence_note.txt")
    void datasetLoadsWithExpectedRecordCounts() {
        assertEquals(50, locations.length, "locations.csv should have 50 records");
        assertEquals(100, roads.length, "roads.csv should have 100 records");
    }

    @Test
    @DisplayName("mergeSort by name, then binarySearch, finds a real location")
    void mergeSortThenBinarySearchFindsARealLocationByName() {
        LibraryLocation[] byName = locations.clone();
        SortAlgorithms.mergeSort(byName, Comparator.comparing(LibraryLocation::name));
        assertTrue(SearchAlgorithms.isSorted(byName, Comparator.comparing(LibraryLocation::name)));

        LibraryLocation needle = locations[locations.length / 2];
        LibraryLocation probe = new LibraryLocation(needle.locationId(), needle.name(), "", "", 0, 0);
        SearchOutcome outcome = SearchAlgorithms.binarySearch(byName, probe, Comparator.comparing(LibraryLocation::name));

        assertTrue(outcome.found());
        assertEquals(needle.name(), byName[outcome.index()].name());
    }

    @Test
    @DisplayName("insertionSort and selectionSort agree on roads.csv ordered by distance_km")
    void insertionSortAndSelectionSortAgreeOnRoadsByDistance() {
        RoadSegment[] a = roads.clone();
        RoadSegment[] b = roads.clone();
        Comparator<RoadSegment> byDistance = Comparator.comparingDouble(RoadSegment::distanceKm);

        SortAlgorithms.insertionSort(a, byDistance);
        SortAlgorithms.selectionSort(b, byDistance);

        for (int i = 0; i < a.length; i++) {
            assertEquals(0, byDistance.compare(a[i], b[i]),
                    "insertionSort and selectionSort should agree at distance-order position " + i);
        }
    }

    @Test
    @DisplayName("binarySearch rejects roads.csv's own road_id order when searched by distance")
    void binarySearchRejectsRoadsInRoadIdOrderWhenSearchedByDistance() {
        // roads.csv is stored in road_id order (R001, R002, ...), not distance
        // order, so searching it by distance_km must trip the stated precondition.
        Comparator<RoadSegment> byDistance = Comparator.comparingDouble(RoadSegment::distanceKm);
        assertFalse(SearchAlgorithms.isSorted(roads, byDistance),
                "raw roads.csv order is not expected to already be sorted by distance_km");
        assertThrows(IllegalArgumentException.class,
                () -> SearchAlgorithms.binarySearch(roads, roads[0], byDistance));
    }

    @Test
    @DisplayName("linearSearch finds a road by id in the original CSV order")
    void linearSearchFindsARoadByIdInOriginalCsvOrder() {
        RoadSegment target = roads[roads.length - 1];
        SearchOutcome outcome = SearchAlgorithms.linearSearch(roads, target,
                Comparator.comparing(RoadSegment::roadId));
        assertEquals(roads.length - 1, outcome.index());
    }
}
