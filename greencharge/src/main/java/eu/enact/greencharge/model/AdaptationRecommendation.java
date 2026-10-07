package eu.enact.greencharge.model;

import java.time.Instant;
import java.util.List;

public record AdaptationRecommendation(
        String nodeId,
        boolean compliant,
        String recommendedAction,
        List<String> violations,
        double complianceScore,
        Instant timestamp) {
}
