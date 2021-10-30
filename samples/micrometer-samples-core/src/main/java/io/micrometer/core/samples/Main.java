package io.micrometer.core.samples;

import java.util.UUID;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import static io.micrometer.core.instrument.Tag.Cardinality.HIGH;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Timer timer = Timer.builder("test")
                .tag("color", "green")
                .tags(Tags.of(Tag.of("id", UUID.randomUUID().toString(), HIGH)))
                .register(registry);

        Timer.Sample sample = Timer.start(registry);
        Thread.sleep(1_000);
        sample.stop(timer);

        System.out.println(timer.getId().getTags());
        System.out.println(timer.getId().getHighCardinalityTagsAsIterable());
        System.out.println();

        System.out.println(registry.scrape());
    }
}
