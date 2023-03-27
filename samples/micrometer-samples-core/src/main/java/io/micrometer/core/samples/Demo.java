package io.micrometer.core.samples;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.time.Duration;
import java.util.function.Function;

public class Demo {

    private static final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    private static final Function<Tags, Timer> timerFactory = Timer.builder("cache.initialization.latency")
        .description("Time initialize cache from database")
        .tag("static", "abc")
        .publishPercentiles(0.99, 0.999)
        .publishPercentileHistogram()
        .minimumExpectedValue(Duration.ofSeconds(10))
        .maximumExpectedValue(Duration.ofSeconds(600))
        .with(registry);

    public static void main(String[] args) {
        doSomething("test");
    }

    private static void doSomething(String cacheName) {
        timerFactory.apply(Tags.of("cacheName", cacheName)).record(Duration.ofMillis(100));
        System.out.println(registry.scrape());
    }

}
