package com.balmelibrary.algorithms;

/**
 * Algorithm parameters derived from team member index numbers, as required
 * by the project brief: <em>"at least three algorithm parameters derived
 * from team member index numbers (e.g. priority weight, route penalty,
 * hash-table size, random seed, budget constraint)"</em>.
 *
 * <p><b>ACTION REQUIRED BEFORE FINAL SUBMISSION:</b> the index numbers below
 * are placeholders. Replace {@link #INDEX_NUMBER_A}, {@link #INDEX_NUMBER_B}
 * and {@link #INDEX_NUMBER_C} with three real University of Ghana index
 * numbers from the team, then re-derive the constants that follow using the
 * documented formulas. Keep the formula comments in the Javadoc -- the
 * examiner will ask for them during the oral defense, and Testing &amp; QA
 * needs them to reproduce the experiment parameters exactly.</p>
 */
public final class IndexNumberParameters {

    /** TODO: replace with a real team member index number, e.g. 10912345. */
    public static final long INDEX_NUMBER_A = 22309418;

    /** TODO: replace with a real team member index number. */
    public static final long INDEX_NUMBER_B = 22297388;

    /** TODO: replace with a real team member index number. */
    public static final long INDEX_NUMBER_C = 22393957;

    /**
     * Priority weight for the scheduling engine's priority-dispatch rule.
     * <br>Formula: {@code (INDEX_NUMBER_A mod 10) + 1}, kept in the range [1, 10]
     * so it can scale a request's urgency score without dominating it.
     */
    public static final int PRIORITY_WEIGHT = (int) (INDEX_NUMBER_A % 10) + 1;

    /**
     * Route penalty: multiplier applied to {@code RoadSegment.conditionWeight}
     * by the graph route engine's edge-cost function (Dijkstra/Prim/Kruskal).
     * <br>Formula: {@code 1.0 + (INDEX_NUMBER_B mod 5) / 10.0}, kept in the
     * range [1.0, 1.4] so it nudges route choice without ever letting a
     * heavily-penalised edge look shorter than a genuinely shorter one.
     */
    public static final double ROUTE_PENALTY = 1.0 + (INDEX_NUMBER_B % 5) / 10.0;

    /**
     * Random seed used everywhere pseudo-randomness is needed (e.g. shuffling
     * an already-sorted array to build a worst-case-avoiding test fixture, or
     * sampling service_requests for the performance experiments), so every
     * team member's run is reproducible.
     * <br>Formula: {@code INDEX_NUMBER_C}, used directly as a {@code
     * java.util.Random} seed.
     */
    public static final long RANDOM_SEED = INDEX_NUMBER_C;

    private IndexNumberParameters() {
    }
}
