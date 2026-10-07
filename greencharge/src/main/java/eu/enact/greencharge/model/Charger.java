package eu.enact.greencharge.model;

/** A single EV charging station in a city district. */
public record Charger(
        String id,
        String name,
        String district,
        int availableSlots,
        int totalSlots) {
}
