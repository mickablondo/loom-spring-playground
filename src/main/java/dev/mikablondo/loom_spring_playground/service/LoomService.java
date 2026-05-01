package dev.mikablondo.loom_spring_playground.service;

import org.springframework.stereotype.Service;

@Service
public class LoomService {
    /**
     * Simule une action plutôt lente
     * @param id identifiant de la tâche
     * @return un message pour indiquer le thread qui travaille
     * @throws InterruptedException en cas d'erreur
     */
    public String slowTask(int id) throws InterruptedException {
        Thread.sleep(1000);
        return "Tâche %d réalisée par %s".formatted(id, Thread.currentThread());
    }
}
