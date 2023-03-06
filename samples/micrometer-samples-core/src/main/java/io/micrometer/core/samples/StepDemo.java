package io.micrometer.core.samples;

import io.micrometer.common.lang.Nullable;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.pause.NoPauseDetector;
import io.micrometer.core.instrument.step.StepCounter;
import io.micrometer.core.instrument.step.StepDistributionSummary;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepTimer;
import io.micrometer.elastic.ElasticConfig;
import io.micrometer.elastic.ElasticMeterRegistry;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class StepDemo {
    public static void main(String[] args) {
        // Counter, Timer, DistributionSummary work
        demoWithMeters();

        // Counter, Timer, DistributionSummary work
//        demoWithElastic();

        // Counter, Timer, DistributionSummary work
//        demoWithInflux();
    }

    private static void demoWithMeters() {
        MockClock clock = new MockClock();
        Meter.Id id = new Meter.Id("test", Tags.empty(), "calls", "", Meter.Type.COUNTER);
        StepTimer timer = new StepTimer(id, clock, DistributionStatisticConfig.DEFAULT, new NoPauseDetector(), TimeUnit.MILLISECONDS, 1_000, true);
        timer.record(100, TimeUnit.MILLISECONDS);
        timer.record(200, TimeUnit.MILLISECONDS);
        timer.record(300, TimeUnit.MILLISECONDS);

        System.out.println();
        System.out.println("--- StepTimer ---");
        System.out.println("measureCurrent BEFORE " + timer.measureCurrent());
        System.out.println("measure        BEFORE " + timer.measure());
        clock.addSeconds(1);
        System.out.println("measureCurrent AFTER  " + timer.measureCurrent());
        System.out.println("measure        AFTER  " + timer.measure());

        StepDistributionSummary ds = new StepDistributionSummary(id, clock, DistributionStatisticConfig.DEFAULT, 1.0, 1_000, true);
        ds.record(1.0);
        ds.record(2.0);
        ds.record(3.0);

        System.out.println();
        System.out.println("--- StepDistributionSummary ---");
        System.out.println("measureCurrent BEFORE " + ds.measureCurrent());
        System.out.println("measure        BEFORE " + ds.measure());
        clock.addSeconds(1);
        System.out.println("measureCurrent AFTER  " + ds.measureCurrent());
        System.out.println("measure        AFTER  " + ds.measure());

        StepCounter counter = new StepCounter(new Meter.Id("test.counter", Tags.empty(), "calls", "", Meter.Type.COUNTER), clock, 1_000);

        counter.increment();
        counter.increment();
        counter.increment();

        System.out.println();
        System.out.println("--- StepCounter ---");
        System.out.println("currentCount BEFORE " + counter.currentCount());
        System.out.println("count        BEFORE " + counter.count());
        clock.addSeconds(1);
        System.out.println("currentCount AFTER  " + counter.currentCount());
        System.out.println("count        AFTER  " + counter.count());
    }

    private static void demoWithElastic() {
        ElasticMeterRegistry registry = ElasticMeterRegistry.builder(new ElasticConfig() {
            @Nullable
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(4);
            }
        })
        .httpClient(new LoggingHttpSender(System.out::println))
        .build();

        demoWithRegistry(registry);
    }

    private static void demoWithInflux() {
        InfluxMeterRegistry registry = InfluxMeterRegistry.builder(new InfluxConfig() {
            @Nullable
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public boolean compressed() {
                return false;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(4);
            }
        })
        .httpClient(new LoggingHttpSender(System.out::println))
        .build();

        demoWithRegistry(registry);
    }

    private static void demoWithRegistry(StepMeterRegistry registry) {
        Counter counter = registry.counter("test.counter");
        Timer timer = registry.timer("test.timer");
        DistributionSummary summary = registry.summary("test.summary");

        for (int i = 1; i <= 5; i++) {
            counter.increment();
            timer.record(Duration.ofMillis(i));
            summary.record(i);
            System.out.println("val: " + i);
            sleep(1_000);
        }

        System.out.println("Closing...");
        registry.close();

    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
