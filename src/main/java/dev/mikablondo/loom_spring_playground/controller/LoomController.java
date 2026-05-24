package dev.mikablondo.loom_spring_playground.controller;

import dev.mikablondo.loom_spring_playground.config.LoomThreadProperties;
import dev.mikablondo.loom_spring_playground.dto.BenchmarkResult;
import dev.mikablondo.loom_spring_playground.dto.LoomInfoResponse;
import dev.mikablondo.loom_spring_playground.dto.PinningResult;
import dev.mikablondo.loom_spring_playground.service.BenchmarkService;
import dev.mikablondo.loom_spring_playground.service.LoomService;
import dev.mikablondo.loom_spring_playground.service.PinningService;
import dev.mikablondo.loom_spring_playground.service.ThreadMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Contrôleur Loom !
 *
 * @author mikablondo
 */
@RestController
@RequestMapping("/loom")
@RequiredArgsConstructor
@Validated
public class LoomController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.threads.virtual.enabled:false}")
    private boolean virtualThreadsEnabled;

    private final LoomService loomService;
    private final ThreadMetricsService metricsService;
    private final BenchmarkService benchmarkService;
    private final PinningService pinningService;
    private final LoomThreadProperties threadProperties;

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

    @Operation(summary = "Returns runtime information about the Loom playground")
    @GetMapping("/info")
    public LoomInfoResponse info() {
        Runtime runtime = Runtime.getRuntime();

        return new LoomInfoResponse(
                applicationName,
                Runtime.version().toString(),
                virtualThreadsEnabled,
                runtime.availableProcessors(),
                threadProperties.create().tempo(),
                threadProperties.life().tempo()
        );
    }

    /**
     * Endpoint qui lance N tâches en parallèle
     *
     * @return les messages des threads
     * @throws Exception en cas d'erreur
     */
    @Operation(summary = "Runs multiple virtual thread tasks in parallel")
    @GetMapping("/parallel")
    public List<String> parallel(@RequestParam(defaultValue = "10") @Min(1) @Max(10_000) int count) throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.rangeClosed(1, count)
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
     * @return un SseEmitter (voir <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html">...</a>)
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
    public String stress(@RequestParam(defaultValue = "100") @Min(1) @Max(100_000) int count) throws InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                int id = i;
                Thread.sleep(threadProperties.create().tempo());
                executor.submit(() -> loomService.slowTask(id));
            }
        }
        return "Stress test terminé : %d virtual threads lancés !".formatted(count);
    }

    @GetMapping("/benchmark")
    public BenchmarkResult benchmark() {
        return benchmarkService.run();
    }

    //region PINNING
    // pinning : quand le virtual thread reste collé au carrier thread
    // carrier thread : le platform thread qui porte le virtual thread le temps qu'il s'exécute

    /**
     * Endpoint pour tester l'effet pinning avec du synchronized.
     * Le carrier thread est bloqué pendant le sleep, les virtual threads se mettent en file d'attente.
     *
     * @return les résultats du test de pinning avec synchronized
     */
    @GetMapping("/pinning/bad")
    public PinningResult pinningBad() {
        long time = pinningService.runWithSynchronized();
        return new PinningResult("synchronized", PinningService.TASK_COUNT, time);
    }

    /**
     * Endpoint pour tester le ReentrantLock : bonne pratique pour éviter le pinning
     * Le carrier thread est libéré pendant le sleep, les virtual threads s'exécutent en parallèle.
     *
     * @return les résultats du test de pinning avec ReentrantLock
     */
    @GetMapping("/pinning/good")
    public PinningResult pinningGood() {
        long time = pinningService.runWithReentrantLock();
        return new PinningResult("ReentrantLock", PinningService.TASK_COUNT, time);
    }
    //endregion
}
