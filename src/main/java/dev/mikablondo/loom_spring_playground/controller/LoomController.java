package dev.mikablondo.loom_spring_playground.controller;

import dev.mikablondo.loom_spring_playground.service.BenchmarkService;
import dev.mikablondo.loom_spring_playground.service.LoomService;
import dev.mikablondo.loom_spring_playground.service.ThreadMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/loom")
@RequiredArgsConstructor
public class LoomController {

    @Value("${loom.thread.create.tempo}")
    private long tempo;

    private final LoomService loomService;
    private final ThreadMetricsService metricsService;
    private final BenchmarkService benchmarkService;

    /**
     * Endpoint simple
     *
     * @return le message du thread
     * @throws InterruptedException en cas d'erreur
     */
    @Operation(summary = "Runs a single virtual thread task")
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
    @Operation(summary = "Runs 10 virtual thread tasks in parallel")
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

    /**
     * Endpoint permettant de pousser les métriques toutes les secondes via SSE
     *
     * @return un SseEmitter (voir https://html.spec.whatwg.org/multipage/server-sent-events.html)
     */
    @Operation(summary = "Pushes thread metrics every second via SSE")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(60_000L);

        Thread.ofVirtual().start(() -> {
            try {
                for (int i = 0; i < 60; i++) {
                    emitter.send(metricsService.getMetrics());
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * Endpoint permettant de lancer N virtual threads
     *
     * @param count nombre de threads à exécuter
     * @return message de fin
     * @throws InterruptedException en cas d'erreur
     */
    @Operation(summary = "Spawns N virtual threads to stress the JVM")
    @GetMapping("/stress")
    public String stress(@RequestParam(defaultValue = "100") int count) throws InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                int id = i;
                Thread.sleep(tempo);
                executor.submit(() -> loomService.slowTask(id));
            }
        }
        return "Stress test terminé : %d virtual threads lancés !".formatted(count);
    }

    @GetMapping("/benchmark")
    public Map<String, Object> benchmark() throws InterruptedException {
        return benchmarkService.run();
    }
}
