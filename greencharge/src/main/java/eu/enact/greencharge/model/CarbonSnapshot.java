package eu.enact.greencharge.model;

import java.util.Map;

/**
 * Per-district grid carbon intensity (gCO2/kWh) plus a normalised 0-100 green
 * score, and whether it came from the live dataspace feed or the built-in mock.
 */
public record CarbonSnapshot(
        String source,
        Map<String, Double> intensity,
        Map<String, Integer> greenScore) {
}
