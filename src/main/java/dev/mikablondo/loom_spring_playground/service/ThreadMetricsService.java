package dev.mikablondo.loom_spring_playground.service;

import dev.mikablondo.loom_spring_playground.dto.ThreadMetricsResponse;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Classe permettant de gérer les différentes métriques des threads JVM
 * - Virtual threads
 * - Platform threads
 * - Total des threads
 * - Peak threads
 *
 * @author mika_blondo
 */
@Service
public class ThreadMetricsService {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    // utilisation d'un AtomicLong pour garantir la thread-safety des opérations d'incrémentation et de décrémentation
    private final AtomicLong activeVirtualTasks = new AtomicLong(0);
    private final AtomicLong activePlatformTasks = new AtomicLong(0);

    public void incrementVirtualTask() { activeVirtualTasks.incrementAndGet(); }
    public void decrementVirtualTask() { activeVirtualTasks.decrementAndGet(); }

    public void incrementPlatformTask() { activePlatformTasks.incrementAndGet(); }
    public void decrementPlatformTask() { activePlatformTasks.decrementAndGet(); }

    /**
     * Récupère les infos des différents threads en cours d'exécution
     *
     * @return une Map des différentes métriques avec leur valeur
     */
    public ThreadMetricsResponse getMetrics() {
        long virtualTasks = activeVirtualTasks.get();
        long platformTasks = activePlatformTasks.get();
        long jvmPlatformThreads = threadMXBean.getThreadCount();

        return new ThreadMetricsResponse(
                virtualTasks,
                platformTasks,
                jvmPlatformThreads,
                threadMXBean.getPeakThreadCount(), // Does not include virtual threads.
                System.currentTimeMillis()
        );
    }
}
