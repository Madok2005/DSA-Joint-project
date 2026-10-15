package com.balmelibrary.io;

import com.balmelibrary.model.LibraryLocation;
import com.balmelibrary.model.LibraryResource;
import com.balmelibrary.model.RoadSegment;
import com.balmelibrary.model.ServiceRequestRecord;
import com.balmelibrary.structures.DynamicArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads the four Balme Library CSV extracts (locations, roads,
 * service_requests, resources) ready to be handed to
 * {@link com.balmelibrary.algorithms.SearchAlgorithms} and
 * {@link com.balmelibrary.algorithms.SortAlgorithms}.
 *
 * <p>This class is deliberately I/O plumbing only: it performs no algorithmic
 * work (that is the Algorithm Developer's job) and no reimplementation of a
 * data structure's public contract (that is the Data Structure Developer's
 * job). What changed once the two modules were connected: the growable
 * staging buffer used while a file's length is unknown in advance is now the
 * Data Structure Developer's own {@link DynamicArray} rather than
 * {@code java.util.ArrayList} -- this module no longer imports anything from
 * {@code java.util.*} for storage, only for I/O ({@code BufferedReader},
 * {@code Path}, ...). Every {@code loadX} method still hands back a plain
 * array (unchanged public contract, so existing callers and tests keep
 * compiling); the {@code loadXAsDynamicArray} variants hand back the
 * {@link DynamicArray} directly for callers who want to keep working with the
 * custom structure end-to-end (see {@code Main} for both styles side by
 * side).</p>
 */
public final class CsvDatasetLoader {

    private CsvDatasetLoader() {
    }

    public static LibraryLocation[] loadLocations(Path csvPath) throws IOException {
        DynamicArray<LibraryLocation> rows = loadLocationsAsDynamicArray(csvPath);
        LibraryLocation[] out = new LibraryLocation[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            out[i] = rows.get(i);
        }
        return out;
    }

    public static DynamicArray<LibraryLocation> loadLocationsAsDynamicArray(Path csvPath) throws IOException {
        DynamicArray<LibraryLocation> rows = new DynamicArray<>();
        for (String[] f : readDataRows(csvPath, 6)) {
            rows.add(LibraryLocation.fromCsvRow(f));
        }
        return rows;
    }

    public static RoadSegment[] loadRoads(Path csvPath) throws IOException {
        DynamicArray<RoadSegment> rows = loadRoadsAsDynamicArray(csvPath);
        RoadSegment[] out = new RoadSegment[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            out[i] = rows.get(i);
        }
        return out;
    }

    public static DynamicArray<RoadSegment> loadRoadsAsDynamicArray(Path csvPath) throws IOException {
        DynamicArray<RoadSegment> rows = new DynamicArray<>();
        for (String[] f : readDataRows(csvPath, 6)) {
            rows.add(RoadSegment.fromCsvRow(f));
        }
        return rows;
    }

    public static ServiceRequestRecord[] loadServiceRequests(Path csvPath) throws IOException {
        DynamicArray<ServiceRequestRecord> rows = loadServiceRequestsAsDynamicArray(csvPath);
        ServiceRequestRecord[] out = new ServiceRequestRecord[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            out[i] = rows.get(i);
        }
        return out;
    }

    public static DynamicArray<ServiceRequestRecord> loadServiceRequestsAsDynamicArray(Path csvPath) throws IOException {
        DynamicArray<ServiceRequestRecord> rows = new DynamicArray<>();
        for (String[] f : readDataRows(csvPath, 8)) {
            rows.add(ServiceRequestRecord.fromCsvRow(f));
        }
        return rows;
    }

    public static LibraryResource[] loadResources(Path csvPath) throws IOException {
        DynamicArray<LibraryResource> rows = loadResourcesAsDynamicArray(csvPath);
        LibraryResource[] out = new LibraryResource[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            out[i] = rows.get(i);
        }
        return out;
    }

    public static DynamicArray<LibraryResource> loadResourcesAsDynamicArray(Path csvPath) throws IOException {
        DynamicArray<LibraryResource> rows = new DynamicArray<>();
        for (String[] f : readDataRows(csvPath, 5)) {
            rows.add(LibraryResource.fromCsvRow(f));
        }
        return rows;
    }

    /**
     * Reads every non-blank data row (header skipped) of a UTF-8,
     * comma-separated file, stripping a possible UTF-8 byte-order mark and
     * validating column count per row. Does not attempt to handle quoted
     * commas because none of the four project CSVs quote embedded commas.
     *
     * <p>The row buffer itself is a {@link DynamicArray}&lt;String[]&gt;
     * rather than {@code java.util.ArrayList}, so the whole load path --
     * from raw line to typed record -- runs on the Data Structure Developer's
     * own resizable array.</p>
     *
     * @throws IOException if a row does not have {@code expectedColumns} fields
     */
    private static DynamicArray<String[]> readDataRows(Path csvPath, int expectedColumns) throws IOException {
        DynamicArray<String[]> rows = new DynamicArray<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(csvPath), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // header, discarded
            if (line == null) {
                throw new IOException("Empty CSV file: " + csvPath);
            }
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                String[] fields = line.split(",", -1);
                if (fields.length != expectedColumns) {
                    throw new IOException("Malformed row at " + csvPath.getFileName()
                            + ":" + lineNumber + " - expected " + expectedColumns
                            + " columns, found " + fields.length + ": " + line);
                }
                rows.add(fields);
            }
        }
        return rows;
    }
}
