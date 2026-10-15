package com.balmelibrary.model;

import java.util.Objects;

/**
 * Immutable record for one row of {@code resources.csv}: a staff role or
 * piece of equipment stationed at a {@link LibraryLocation}.
 *
 * <p><b>Provenance:</b> staff roles and equipment types were directly observed;
 * exact counts and live availability are estimated from typical shift patterns
 * rather than a live roster (see {@code docs/Evidence_note.txt}).</p>
 *
 * @param resourceId          unique identifier, e.g. {@code "S014"}
 * @param resourceType        e.g. {@code "Circulation Officer"}, {@code "Photocopy Machine"}
 * @param homeLocationId      {@link LibraryLocation#locationId()} this resource is based at
 * @param capacity            how many concurrent requests/units this resource can serve
 * @param availabilityStatus  e.g. {@code "AVAILABLE"}, {@code "BUSY"}, {@code "OFF_DUTY"}
 */
public record LibraryResource(
        String resourceId,
        String resourceType,
        String homeLocationId,
        int capacity,
        String availabilityStatus
) implements Comparable<LibraryResource> {

    public LibraryResource {
        Objects.requireNonNull(resourceId, "resourceId must not be null");
    }

    /** Natural ordering: ascending {@code resourceId}. */
    @Override
    public int compareTo(LibraryResource other) {
        return this.resourceId.compareTo(other.resourceId);
    }

    /**
     * @param f fields in column order: resource_id, resource_type,
     *          home_location_id, capacity, availability_status
     */
    public static LibraryResource fromCsvRow(String[] f) {
        return new LibraryResource(
                f[0].trim(),
                f[1].trim(),
                f[2].trim(),
                Integer.parseInt(f[3].trim()),
                f[4].trim()
        );
    }
}
