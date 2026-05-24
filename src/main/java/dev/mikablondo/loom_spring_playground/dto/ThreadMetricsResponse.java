package dev.mikablondo.loom_spring_playground.dto;

public record ThreadMetricsResponse(
        long activeVirtualTasks,
        long activePlatformTasks,
        long jvmPlatformThreads,
        int peakJvmPlatformThreads,
        long timestamp
) {
}
