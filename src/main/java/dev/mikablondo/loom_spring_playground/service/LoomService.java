package dev.mikablondo.loom_spring_playground.service;

import dev.mikablondo.loom_spring_playground.config.LoomThreadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Service permettant l'exécution de tâches pour les virtual threads
 *
 * @author mikablondo
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class LoomService {

    private final ThreadMetricsService metricsService;
    private final LoomThreadProperties threadProperties;

    /**
     * Simule une action plutôt lente
     * @param id identifiant de la tâche
     * @return un message pour indiquer le thread qui travaille
     * @throws InterruptedException en cas d'erreur
     */
    public String slowTask(int id) throws InterruptedException {
        metricsService.incrementVirtualTask();
        try {
            if(log.isDebugEnabled()) {
                log.debug("Thread {} isVirtual: {}", id, Thread.currentThread().isVirtual());
            }
            Thread.sleep(threadProperties.life().tempo());
            return "Tâche %d réalisée par %s".formatted(id, Thread.currentThread());
        } finally {
            metricsService.decrementVirtualTask();
        }
    }
}
