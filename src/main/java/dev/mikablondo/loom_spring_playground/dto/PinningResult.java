package dev.mikablondo.loom_spring_playground.dto;

public record PinningResult(
        String strategy,
        int taskCount,
        long timeMs
) {
}
