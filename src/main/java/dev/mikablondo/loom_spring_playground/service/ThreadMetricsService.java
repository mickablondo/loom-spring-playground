package dev.mikablondo.loom_spring_playground.service;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;

/**
 * Classe permettant de gérer les différentes métriques des threads JVM
 *
 * @author mika_blondo
 */
@Service
public class ThreadMetricsService {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    /**
     * Récupère les infos des différents threads en cours d'exécution
     *
     * @return une Map des différentes métriques avec leur valeur
     */
    public Map<String, Object> getMetrics() {
        long virtualCount = Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isVirtual)
                .count();

        long platformCount = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> !t.isVirtual())
                .count();

        return Map.of(
                "virtualThreads", virtualCount,
                "platformThreads", platformCount,
                "totalThreads", threadMXBean.getThreadCount(),
                "peakThreads", threadMXBean.getPeakThreadCount(),
                "timestamp", System.currentTimeMillis()
        );
    }
}