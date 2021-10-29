package io.micrometer.core.samples;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Timer timer = Timer.builder("test")
                .tags(Tags.from(Main::tagsProvider))
                .tags(Tags.from(Main::iterableOfTagProvider))
                .tags(Tags.from(new CustomTagsProvider()))
                .register(registry);

        Timer.Sample sample = Timer.start(registry);
        Thread.sleep(1_000);
        sample.stop(timer);

        System.out.println(registry.scrape());
    }

    private static Tags tagsProvider() {
        return Tags.of("a", "b");
    }

    private static Iterable<Tag> iterableOfTagProvider() {
        Set<Tag> tags = new HashSet<>();
        tags.add(Tag.of("c", "d"));

        return tags;
    }

    static class CustomTagsProvider implements Supplier<Iterable<Tag>> {
        @Override
        public Iterable<Tag> get() {
            return Tags.of("e", "f");
        }
    }
}
