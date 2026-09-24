package eu.enact.greencharge.model;

/** A charger enriched with the carbon score used to rank it. */
public record ScoredCharger(
        String id,
        String name,
        String district,
        double carbonIntensity,
        int greenScore,
        int availableSlots) {
}
