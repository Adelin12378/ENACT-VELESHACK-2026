package eu.enact.greencharge.model;

public record ResourceMetrics(
        String nodeId,
        String region,
        double greenEnergyScore,
        double availability,
        double cpuUtilization,
        double memoryUtilization) {
}
