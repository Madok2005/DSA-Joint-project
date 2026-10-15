package com.balmelibrary.model;

import java.util.Objects;

/**
 * Immutable record for one row of {@code service_requests.csv}.
 *
 * <p><b>Provenance:</b> per {@code docs/Evidence_note.txt}, most of these 300
 * requests are a modelled expansion built to reach the project brief's minimum
 * record count, using sequential synthetic IDs rather than real patron data.
 * No patron names, student IDs, phone numbers or card numbers are present in
 * this or any other table, by design.</p>
 *
 * @param requestId            unique identifier, e.g. {@code "Q014"}
 * @param sourceLocationId     {@link LibraryLocation#locationId()} the request originates from
 * @param destinationLocationId {@link LibraryLocation#locationId()} the request is directed to
 * @param category             request type, e.g. {@code "Hold/Reservation"}
 * @param urgency              integer urgency score (higher = more urgent)
 * @param timeSubmitted        ISO-8601 timestamp string
 * @param deadline             ISO-8601 timestamp string
 * @param status               lifecycle status, e.g. {@code "NEW"}, {@code "COMPLETED"}
 */
public record ServiceRequestRecord(
        String requestId,
        String sourceLocationId,
        String destinationLocationId,
        String category,
        int urgency,
        String timeSubmitted,
        String deadline,
        String status
) implements Comparable<ServiceRequestRecord> {

    public ServiceRequestRecord {
        Objects.requireNonNull(requestId, "requestId must not be null");
    }

    /** Natural ordering: ascending {@code requestId}. */
    @Override
    public int compareTo(ServiceRequestRecord other) {
        return this.requestId.compareTo(other.requestId);
    }

    /**
     * @param f fields in column order: request_id, source_location_id,
     *          destination_location_id, category, urgency, time_submitted,
     *          deadline, status
     */
    public static ServiceRequestRecord fromCsvRow(String[] f) {
        return new ServiceRequestRecord(
                f[0].trim(),
                f[1].trim(),
                f[2].trim(),
                f[3].trim(),
                Integer.parseInt(f[4].trim()),
                f[5].trim(),
                f[6].trim(),
                f[7].trim()
        );
    }
}
