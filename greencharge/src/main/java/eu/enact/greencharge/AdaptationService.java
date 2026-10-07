package eu.enact.greencharge;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import eu.enact.greencharge.model.AdaptationRecommendation;
import eu.enact.greencharge.model.ResourceMetrics;

@Service
public class AdaptationService {

    private static final Logger log =
            LoggerFactory.getLogger(AdaptationService.class);

    private Map<String, Object> policyModel;

    public AdaptationService() {
        loadPolicyModel();
    }

    private void loadPolicyModel() {
        try {
            ClassPathResource resource =
                    new ClassPathResource("reconciliation/policymodel.yml");

            try (InputStream is = resource.getInputStream()) {
                Yaml yaml = new Yaml();
                this.policyModel = yaml.load(is);

                log.info(
                        "Successfully loaded Application Policy Model: {}",
                        policyModel != null
                                ? policyModel.get("application")
                                : "null");
            }

        } catch (Exception e) {
            log.warn(
                    "Could not load reconciliation/policymodel.yml ({}); using embedded rules.",
                    e.getMessage());
        }
    }

    public AdaptationRecommendation evaluate(ResourceMetrics metrics) {

        List<String> violations = new ArrayList<>();
        boolean compliant = true;

        // Hard rule 1: Region
        if (metrics.region() != null &&
                !metrics.region().equalsIgnoreCase("eu-west")) {

            violations.add(
                    "HARD rule violated: Node region '" +
                    metrics.region() +
                    "' is not 'eu-west'");

            compliant = false;
        }

        // Hard rule 2: Availability
        if (metrics.availability() < 0.90) {

            violations.add(
                    "HARD rule violated: Availability (" +
                    metrics.availability() +
                    ") is below threshold 0.90");

            compliant = false;
        }

        // Soft rule: Green energy
        if (metrics.greenEnergyScore() < 0.60) {

            violations.add(
                    "SOFT rule warning: Green energy score (" +
                    metrics.greenEnergyScore() +
                    ") is below target 0.60");
        }

        // Elasticity recommendation
        String action = "no_action";

        if (metrics.cpuUtilization() > 80.0 ||
                metrics.memoryUtilization() > 85.0) {

            action = "scale_up";

        } else if (metrics.cpuUtilization() < 20.0 &&
                metrics.memoryUtilization() < 25.0) {

            action = "scale_down";
        }

        double score = calculateScore(metrics, compliant);

        return new AdaptationRecommendation(
                metrics.nodeId() != null
                        ? metrics.nodeId()
                        : "unknown-node",
                compliant,
                action,
                violations,
                score,
                Instant.now());
    }

    private double calculateScore(
            ResourceMetrics metrics,
            boolean compliant) {

        if (!compliant) {
            return 0.0;
        }

        return Math.min(
                1.0,
                (metrics.greenEnergyScore() * 0.6) +
                (metrics.availability() * 0.4));
    }
}
