package dev.mikablondo.loom_spring_playground.service;

import dev.mikablondo.loom_spring_playground.dto.BenchmarkResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Service permettant l'execution du benchmark sur les deux types de threads.
 *
 * @author mikablondo
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class BenchmarkService {

    private static final int TASK_COUNT = 10000;
    private static final int SLEEP_MS = 1000;

    private final ThreadMetricsService threadMetricsService;

    /**
     * Execute le benchmark : Virtual Threads vs Platform Threads.
     *
     * @return les donnees du benchmark
     */
    public BenchmarkResult run() {
        log.info("Starting virtual threads benchmark");
        long virtualTime = calculVirtualThreadTime();

        log.info("Starting platform threads benchmark");
        long platformTime = calculPlatformThreadTime();

        return new BenchmarkResult(
                TASK_COUNT,
                SLEEP_MS,
                virtualTime,
                platformTime,
                virtualTime < platformTime ? "Virtual Threads" : "Platform Threads",
                "%.2fx".formatted((double) platformTime / virtualTime)
        );
    }

    /**
     * Simulation d'une action bloquante suivie d'un petit calcul.
     */
    private void simulateAction() {
        try {
            Thread.sleep(SLEEP_MS);
            IntStream.range(0, 1000).asLongStream().sum();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulateVirtualAction() {
        threadMetricsService.incrementVirtualTask();
        try {
            simulateAction();
        } finally {
            threadMetricsService.decrementVirtualTask();
        }
    }

    private void simulatePlatformAction() {
        threadMetricsService.incrementPlatformTask();
        try {
            simulateAction();
        } finally {
            threadMetricsService.decrementPlatformTask();
        }
    }

    /**
     * Calcule le temps mis par les threads virtuels.
     *
     * @return temps d'execution
     */
    private long calculVirtualThreadTime() {
        long startVirtual = System.currentTimeMillis();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, TASK_COUNT)
                    .mapToObj(i -> executor.submit(this::simulateVirtualAction))
                    .toList();
            for (var f : futures) {
                try { f.get(); } catch (Exception e) { Thread.currentThread().interrupt(); }
            }
        }
        return System.currentTimeMillis() - startVirtual;
    }

    /**
     * Calcule le temps mis par les platform threads.
     *
     * @return temps d'execution
     */
    private long calculPlatformThreadTime() {
        long startPlatform = System.currentTimeMillis();
        try (var executor = Executors.newFixedThreadPool(200)) {
            var futures = IntStream.range(0, TASK_COUNT)
                    .mapToObj(i -> executor.submit(this::simulatePlatformAction))
                    .toList();
            for (var f : futures) {
                try { f.get(); } catch (Exception e) { Thread.currentThread().interrupt(); }
            }
        }
        return System.currentTimeMillis() - startPlatform;
    }
}
