package dev.mikablondo.loom_spring_playground.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoomService {

    private final ThreadMetricsService metricsService;

    /**
     * Simule une action plutôt lente
     * @param id identifiant de la tâche
     * @return un message pour indiquer le thread qui travaille
     * @throws InterruptedException en cas d'erreur
     */
    public String slowTask(int id) throws InterruptedException {
        metricsService.incrementVirtual();
        try {
            log.info("Thread {} isVirtual: {}", id, Thread.currentThread().isVirtual());
            Thread.sleep(1000);
            return "Tâche %d réalisée par %s".formatted(id, Thread.currentThread());
        } finally {
            metricsService.decrementVirtual();
        }
    }
}
