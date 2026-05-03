package dev.mikablondo.loom_spring_playground.service;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Classe permettant de gérer les différentes métriques des threads JVM
 *
 * @author mika_blondo
 */
@Service
public class ThreadMetricsService {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    // utilisation d'un AotmicLong pour garantir la thread-safety des opérations d'incrémentation et de décrémentation
    private final AtomicLong activeVirtualThreads = new AtomicLong(0);

    public void incrementVirtual() { activeVirtualThreads.incrementAndGet(); }
    public void decrementVirtual() { activeVirtualThreads.decrementAndGet(); }

    /**
     * Récupère les infos des différents threads en cours d'exécution
     *
     * @return une Map des différentes métriques avec leur valeur
     */
    public Map<String, Object> getMetrics() {
        long virtualCount = activeVirtualThreads.get();
        long platformCount = threadMXBean.getThreadCount();

        return Map.of(
                "virtualThreads", virtualCount,
                "platformThreads", platformCount,
                "totalThreads", virtualCount + platformCount,
                "peakThreads", threadMXBean.getPeakThreadCount(),
                "timestamp", System.currentTimeMillis()
        );
    }
}