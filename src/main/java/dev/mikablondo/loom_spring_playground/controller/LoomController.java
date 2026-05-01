package dev.mikablondo.loom_spring_playground.controller;

import dev.mikablondo.loom_spring_playground.service.LoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/loom")
public class LoomController {

    private final LoomService loomService;

    public LoomController(LoomService loomService) {
        this.loomService = loomService;
    }

    /**
     * Endpoint simple
     *
     * @return le message du thread
     * @throws InterruptedException en cas d'erreur
     */
    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        return loomService.slowTask(1);
    }

    /**
     * Endpoint qui lance 10 tâches en parallèle
     * TODO : rendre paramétrable
     *
     * @return les messages des threads
     * @throws Exception en cas d'erreur
     */
    @GetMapping("/parallel")
    public List<String> parallel() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, 10)
                    .mapToObj(i -> executor.submit(() -> loomService.slowTask(i)))
                    .toList();

            var results = new ArrayList<String>();
            for (var f : futures) results.add(f.get());
            return results;
        }
    }
}
