package dev.mikablondo.loom_spring_playground.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loom.thread")
public record LoomThreadProperties(ThreadTiming create, ThreadTiming life) {

    public record ThreadTiming(long tempo) {
    }
}
