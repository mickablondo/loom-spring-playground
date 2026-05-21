package dev.mikablondo.loom_spring_playground.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Service permettant l'exécution du benchmark sur les deux types de threads
 *
 * @author mikablondo
 */
@Service
@RequiredArgsConstructor
public class BenchmarkService {

    private final ThreadMetricsService threadMetricsService;

    private static final int TASK_COUNT = 10000;
    private static final int SLEEP_MS = 100;

    /**
     * Exécute le benchmark : Virtual Threads vs Platform Threads
     *
     * @return toutes les données permettant de justifier qui a gagné !
     */
    public Map<String, Object> run() {
        long virtualTime = calculVirtualThreadTime();
        long platformTime = calculPlatformThreadTime();

        return Map.of(
                "Nombre de tâches en parallèle", TASK_COUNT,
                "Temps d'attente pour accès bdd", SLEEP_MS,
                "temps virtual threads", virtualTime,
                "temps platform threads", platformTime,
                "And the winner is ...", virtualTime < platformTime ? "Virtual Threads" : "Platform Threads",
                "Plus rapide de", "%.2fx".formatted((double) platformTime / virtualTime)
        );
    }

    /**
     * Simulation d'une action de la part du thread.
     * 1. prend 100 ms pour simuler un appel à une bdd
     * 2. réalise un "gros" calcul
     */
    private void simulateAction() {
        threadMetricsService.incrementVirtual();
        try {
            Thread.sleep(SLEEP_MS);
            long result = IntStream.range(0, 1000).asLongStream().sum();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            threadMetricsService.decrementVirtual();
        }
    }

    /**
     * Calcule le temps mis par les threads virtuels
     *
     * @return temps d'exécution
     */
    private long calculVirtualThreadTime() {
        long startVirtual = System.currentTimeMillis();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, TASK_COUNT)
                    .mapToObj(i -> executor.submit(this::simulateAction))
                    .toList();
            for (var f : futures) {
                try { f.get(); } catch (Exception e) { Thread.currentThread().interrupt(); }
            }
        }
        return System.currentTimeMillis() - startVirtual;
    }

    /**
     * Calcule le temps mis par les threads de l'OS aka Platform Threads
     *
     * @return temps d'exécution
     */
    private long calculPlatformThreadTime() {
        long startPlatform = System.currentTimeMillis();
        try (var executor = Executors.newFixedThreadPool(200)) {
            var futures = IntStream.range(0, TASK_COUNT)
                    .mapToObj(i -> executor.submit(this::simulateAction))
                    .toList();
            for (var f : futures) {
                try { f.get(); } catch (Exception e) { Thread.currentThread().interrupt(); }
            }
        }
        return System.currentTimeMillis() - startPlatform;
    }
}
