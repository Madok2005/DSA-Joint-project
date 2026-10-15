package com.balmelibrary.model;

import java.util.Objects;

/**
 * Immutable record for one row of {@code locations.csv}.
 *
 * <p><b>Provenance</b> (see {@code docs/Evidence_note.txt}): the location names,
 * wings, floors and room types were directly observed from Balme Library staff
 * on 3-5 August 2026. {@code xCoordM}/{@code yCoordM} are a modelled local grid
 * used to reason about the internal network; they are not GPS coordinates.</p>
 *
 * @param locationId   unique identifier, e.g. {@code "L014"}
 * @param name         display name, e.g. {@code "IAC"}
 * @param area         wing/floor description, e.g. {@code "West Wing Second floor"}
 * @param locationType category, e.g. {@code "Reading-Room"}
 * @param xCoordM      local grid x-coordinate in metres (relative, not GPS)
 * @param yCoordM      local grid y-coordinate in metres (relative, not GPS)
 */
public record LibraryLocation(
        String locationId,
        String name,
        String area,
        String locationType,
        double xCoordM,
        double yCoordM
) implements Comparable<LibraryLocation> {

    public LibraryLocation {
        Objects.requireNonNull(locationId, "locationId must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }

    /** Natural ordering: ascending {@code locationId} (e.g. L001 &lt; L002 &lt; ...). */
    @Override
    public int compareTo(LibraryLocation other) {
        return this.locationId.compareTo(other.locationId);
    }

    /**
     * Builds a record from one already-split CSV row.
     *
     * @param f fields in column order: location_id, name, area, location_type, x_coord_m, y_coord_m
     */
    public static LibraryLocation fromCsvRow(String[] f) {
        return new LibraryLocation(
                f[0].trim(),
                f[1].trim(),
                f[2].trim(),
                f[3].trim(),
                Double.parseDouble(f[4].trim()),
                Double.parseDouble(f[5].trim())
        );
    }
}
