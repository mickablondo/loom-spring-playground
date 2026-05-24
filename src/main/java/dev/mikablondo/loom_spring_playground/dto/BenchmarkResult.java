package dev.mikablondo.loom_spring_playground.dto;

public record BenchmarkResult(
        int taskCount,
        int simulatedBlockingTimeMs,
        long virtualThreadsTimeMs,
        long platformThreadsTimeMs,
        String winner,
        String speedup
) {
}
