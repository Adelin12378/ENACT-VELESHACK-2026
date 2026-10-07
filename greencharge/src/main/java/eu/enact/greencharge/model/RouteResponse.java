package eu.enact.greencharge.model;

import java.time.Instant;
import java.util.List;

/** The routing decision: the pick, the full ranking, and where the carbon data came from. */
public record RouteResponse(
        String carbonSource,
        ScoredCharger recommended,
        List<ScoredCharger> ranked,
        Instant timestamp) {
}
