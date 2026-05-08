package dev.mikablondo.loom_spring_playground.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Service
public class PinningService {

    private static final int TASK_COUNT = 100;
    private static final int SLEEP_MS = 100;

    private final Object lock = new Object();

    /**
     * Force le pinning avec un synchronized
     *
     * @return le temps d'exécution
     */
    public long runWithSynchronized() {
        long start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, TASK_COUNT)
                    .mapToObj(i -> executor.submit(() -> {
                        synchronized (lock) {
                            try {
                                Thread.sleep(SLEEP_MS);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }))
                    .toList();

            for (var f : futures) {
                try { f.get(); } catch (Exception e) { Thread.currentThread().interrupt(); }
            }
        }

        return System.currentTimeMillis() - start;
    }

    /**
     * Utilisation de ReentrantLock pour éviter le pinning.
     *
     * @return le temps d'exécution
     */
    public long runWithReentrantLock() {
        long start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, TASK_COUNT)
                    .mapToObj(i -> executor.submit(() -> {
                        ReentrantLock localLock = new ReentrantLock();
                        localLock.lock();
                        try {
                            Thread.sleep(SLEEP_MS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            localLock.unlock();
                        }
                    }))
                    .toList();

            for (var f : futures) {
                try { f.get(); } catch (Exception e) { Thread.currentThread().interrupt(); }
            }
        }

        return System.currentTimeMillis() - start;
    }
}
