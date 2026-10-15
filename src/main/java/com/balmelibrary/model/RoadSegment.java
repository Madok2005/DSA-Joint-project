package com.balmelibrary.model;

import java.util.Objects;

/**
 * Immutable record for one row of {@code roads.csv}: an edge in the internal
 * navigation network between two {@link LibraryLocation}s.
 *
 * <p><b>Provenance:</b> distances, travel times and condition weights are a
 * modelled estimate (not stopwatch-timed), per {@code docs/Evidence_note.txt}.
 * They should be validated against real timings before final submission.</p>
 *
 * @param roadId          unique identifier, e.g. {@code "R014"}
 * @param fromLocationId  {@link LibraryLocation#locationId()} at one end
 * @param toLocationId    {@link LibraryLocation#locationId()} at the other end
 * @param distanceKm      estimated distance in kilometres
 * @param travelTimeMin   estimated travel time in minutes
 * @param conditionWeight judgement-assigned penalty (e.g. stairs score higher)
 */
public record RoadSegment(
        String roadId,
        String fromLocationId,
        String toLocationId,
        double distanceKm,
        int travelTimeMin,
        double conditionWeight
) implements Comparable<RoadSegment> {

    public RoadSegment {
        Objects.requireNonNull(roadId, "roadId must not be null");
        Objects.requireNonNull(fromLocationId, "fromLocationId must not be null");
        Objects.requireNonNull(toLocationId, "toLocationId must not be null");
    }

    /** Natural ordering: ascending {@code roadId}. */
    @Override
    public int compareTo(RoadSegment other) {
        return this.roadId.compareTo(other.roadId);
    }

    /**
     * @param f fields in column order: road_id, from_location_id, to_location_id,
     *          distance_km, travel_time_min, condition_weight
     */
    public static RoadSegment fromCsvRow(String[] f) {
        return new RoadSegment(
                f[0].trim(),
                f[1].trim(),
                f[2].trim(),
                Double.parseDouble(f[3].trim()),
                Integer.parseInt(f[4].trim()),
                Double.parseDouble(f[5].trim())
        );
    }
}
