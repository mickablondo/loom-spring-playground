package dev.mikablondo.loom_spring_playground.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoomService {

    @Value("${loom.thread.life.tempo}")
    private long tempo;

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
            if(log.isDebugEnabled()) {
                log.debug("Thread {} isVirtual: {}", id, Thread.currentThread().isVirtual());
            }
            Thread.sleep(tempo);
            return "Tâche %d réalisée par %s".formatted(id, Thread.currentThread());
        } finally {
            metricsService.decrementVirtual();
        }
    }
}
