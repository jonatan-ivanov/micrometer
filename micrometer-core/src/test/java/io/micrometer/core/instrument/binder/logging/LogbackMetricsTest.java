/*
 * Copyright 2017 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.binder.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.micrometer.core.Issue;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.cumulative.CumulativeCounter;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class LogbackMetricsTest {

    private MeterRegistry registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());

    private Logger logger = (Logger) LoggerFactory.getLogger("foo");

    LogbackMetrics logbackMetrics;

    @BeforeEach
    void bindLogbackMetrics() {
        // tag::setup[]
        logbackMetrics = new LogbackMetrics();
        logbackMetrics.bindTo(registry);
        // end::setup[]
    }

    @AfterEach
    void closeLogbackMetrics() {
        if (logbackMetrics != null) {
            logbackMetrics.close();
        }
    }

    @Test
    void logbackLevelMetrics() {
        assertThat(registry.get("logback.events").counter().count()).isEqualTo(0.0);

        // tag::example[]
        logger.setLevel(Level.INFO);

        logger.warn("warn");
        logger.error("error");
        logger.debug("debug"); // shouldn't record a metric

        assertThat(registry.get("logback.events").tags("level", "warn").counter().count()).isEqualTo(1.0);
        assertThat(registry.get("logback.events").tags("level", "debug").counter().count()).isEqualTo(0.0);
        // end::example[]
    }

    @Issue("#183")
    @Test
    void isLevelEnabledDoesntContributeToCounts() {
        logger.isErrorEnabled();

        assertThat(registry.get("logback.events").tags("level", "error").counter().count()).isEqualTo(0.0);
    }

    @Issue("#411, #3623")
    @Test
    void ignoringLogMetricsInsideCounters() {
        registry = new LoggingCounterMeterRegistry();
        try (LogbackMetrics logbackMetrics = new LogbackMetrics()) {
            logbackMetrics.bindTo(registry);
            LoggerFactory.getLogger("test").info("This should be counted once");
        }
        assertThat(registry.get("logback.events").tags("level", "info").counter().count()).isOne();
    }

    @Issue("#421")
    @Test
    void removeFilterFromLoggerContextOnClose() {
        LoggerContext loggerContext = new LoggerContext();

        LogbackMetrics logbackMetrics = new LogbackMetrics(emptyList(), loggerContext);
        logbackMetrics.bindTo(registry);

        assertThat(loggerContext.getTurboFilterList()).hasSize(1);
        logbackMetrics.close();
        assertThat(loggerContext.getTurboFilterList()).isEmpty();
    }

    @Issue("#2028")
    @Test
    void reAddFilterToLoggerContextAfterReset() {
        LoggerContext loggerContext = new LoggerContext();
        assertThat(loggerContext.getTurboFilterList()).isEmpty();

        LogbackMetrics logbackMetrics = new LogbackMetrics(emptyList(), loggerContext);
        logbackMetrics.bindTo(registry);

        assertThat(loggerContext.getTurboFilterList()).hasSize(1);
        loggerContext.reset();
        assertThat(loggerContext.getTurboFilterList()).hasSize(1);
    }

    @Issue("#2270")
    @Test
    void resetIgnoreMetricsWhenRunnableThrows() {
        Counter infoLogCounter = registry.get("logback.events").tag("level", "info").counter();
        logger.info("hi");
        assertThat(infoLogCounter.count()).isEqualTo(1);
        try {
            LogbackMetrics.ignoreMetrics(() -> {
                throw new RuntimeException();
            });
        }
        catch (RuntimeException ignore) {
        }
        logger.info("hi");
        assertThat(infoLogCounter.count()).isEqualTo(2);
    }

    @Issue("#3891")
    @Test
    void threadLocalNotUsedWhenLogLevelNotEnabled() {
        ThreadLocal<Boolean> priorThreadLocal = LogbackMetrics.ignoreMetrics;
        AtomicInteger interactions = new AtomicInteger();
        try {
            LogbackMetrics.ignoreMetrics = new ThreadLocal<Boolean>() {
                @Override
                public Boolean get() {
                    interactions.incrementAndGet();
                    return super.get();
                }

                @Override
                public void set(Boolean value) {
                    interactions.incrementAndGet();
                    super.set(value);
                }

                @Override
                public void remove() {
                    interactions.incrementAndGet();
                    super.remove();
                }
            };
            logger.trace("trace");
            logger.isErrorEnabled();
            assertThat(interactions.get()).isZero();
        }
        finally {
            LogbackMetrics.ignoreMetrics = priorThreadLocal;
        }
    }

    @NullMarked
    private static class LoggingCounterMeterRegistry extends SimpleMeterRegistry {

        @Override
        protected Counter newCounter(Meter.Id id) {
            return new LoggingCounter(id);
        }

    }

    private static class LoggingCounter extends CumulativeCounter {

        org.slf4j.Logger logger = LoggerFactory.getLogger(LoggingCounter.class);

        LoggingCounter(Id id) {
            super(id);
        }

        @Override
        public void increment() {
            logger.info("beep"); // this is necessary to test gh-3623
            super.increment();
        }

    }

}
