package io.micrometer.core.samples;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.micrometer.core.samples.FakeClockDemo.FakeClock.startWithFakeClock;
import static io.micrometer.core.samples.FakeClockDemo.FakeClock.withFakeClock;

public class FakeClockDemo {

    public static void main(String[] args) {
        FakeClock clock = new FakeClock(Clock.SYSTEM);
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, new PrometheusRegistry(), clock);
        LongTaskTimer ltt = LongTaskTimer.builder("ltt").register(registry);

        LongTaskTimer.Sample sample1 = ltt.start();
        System.out.printf(registry.scrape());
        sample1.stop();

        System.out.println("---");
        LongTaskTimer.Sample sample2 = startWithFakeClock(System.nanoTime() - Duration.ofMinutes(10).toNanos(), ltt);
        System.out.printf(registry.scrape());
        sample2.stop();

        System.out.println("---");
        withFakeClock(System.nanoTime() - Duration.ofMinutes(10).toNanos(), () -> printTime(clock));
    }

    static void printTime(FakeClock clock) {
        System.out.println("Wall time diff: " + (System.currentTimeMillis() - clock.wallTime()));
        System.out.println("Mono time diff: " + (System.nanoTime() - clock.monotonicTime()));
    }

    static class FakeClock implements Clock {
        private static final ThreadLocal<@Nullable Clock> fake = new ThreadLocal<>();
        private final Clock delegate;

        FakeClock(Clock delegate) {
            this.delegate = delegate;
        }

        @Override
        public long wallTime() {
            Clock fakeClock = fake.get();
            return fakeClock != null ? fakeClock.wallTime() : delegate.wallTime();
        }

        @Override
        public long monotonicTime() {
            Clock fakeClock = fake.get();
            return fakeClock != null ? fakeClock.monotonicTime() : delegate.monotonicTime();
        }

        static LongTaskTimer.Sample startWithFakeClock(long startTime, LongTaskTimer longTaskTimer) {
            return withFakeClock(startTime, longTaskTimer::start);
        }

        static <T> T withFakeClock(long startTime, Supplier<T> supplier) {
            fake.set(new ConstantClock(startTime));
            T result = supplier.get();
            fake.remove();
            return result;
        }

        static void withFakeClock(long startTime, Runnable runnable) {
            fake.set(new ConstantClock(startTime));
            runnable.run();
            fake.remove();
        }
    }

    static class ConstantClock implements Clock {

        private final long time;

        ConstantClock(long time) {
            this.time = time;
        }

        @Override
        public long wallTime() {
            return System.currentTimeMillis() - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time);
        }

        @Override
        public long monotonicTime() {
            return time;
        }
    }

}
