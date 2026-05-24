package dev.mikablondo.loom_spring_playground.dto;

public record LoomInfoResponse(
        String applicationName,
        String javaVersion,
        boolean virtualThreadsEnabled,
        int availableProcessors,
        long threadCreateTempoMs,
        long threadLifeTempoMs
) {
}
