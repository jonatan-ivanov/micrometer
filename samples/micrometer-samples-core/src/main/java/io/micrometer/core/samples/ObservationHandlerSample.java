/*
 * Copyright 2021 VMware, Inc.
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
package io.micrometer.core.samples;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import io.micrometer.api.instrument.Observation;
import io.micrometer.api.instrument.ObservationHandler;
import io.micrometer.api.instrument.Tags;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class ObservationHandlerSample {
    private static final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    public static void main(String[] args) throws InterruptedException {
        registry.config().observationHandler(new SampleHandler());

        Observation observation = registry.observation("sample.operation", new CustomContext())
                .displayName("CALL sampleOperation")
                .lowCardinalityTag("a", "1")
                .highCardinalityTag("time", Instant.now().toString())
                .start();
        try (Observation.Scope scope = observation.openScope()) {
            Thread.sleep(1_000);
            observation.error(new IOException("simulated"));
        }
        observation.stop();

        registry.observation("sample.operation").start().stop();
        registry.observation("sample.operation", new UnsupportedHandlerContext()).start().stop();

        System.out.println();
        System.out.println(registry.scrape());
    }

    static class SampleHandler implements ObservationHandler<CustomContext> {
        @Override
        public void onStart(Observation observation, CustomContext context) {
            System.out.println("start: " + observation);
        }

        @Override
        public void onError(Observation observation, CustomContext context, Throwable error) {
            System.out.println("error: " + error + " " + observation);

        }

        @Override
        public void onScopeOpened(Observation observation, CustomContext context) {
            System.out.println("context-opened: " + observation);
        }

        @Override
        public void onScopeClosed(Observation observation, CustomContext context) {
            System.out.println("context-closed: " + observation);
        }

        @Override
        public void onStop(Observation observation, CustomContext context, Duration duration) {
            System.out.println("stop: " + duration + " " + observation);

        }

        @Override
        public boolean supportsContext(Observation.Context context) {
            return context instanceof CustomContext;
        }
    }

    static class CustomContext extends Observation.Context {
        private final UUID uuid = UUID.randomUUID();

        @Override
        public Tags getLowCardinalityTags() {
            return Tags.of("status", "ok");
        }

        @Override
        public Tags getHighCardinalityTags() {
            return Tags.of("userId", uuid.toString());
        }

        @Override
        public String toString() {
            return "CustomHandlerContext{" + uuid + '}';
        }
    }

    static class UnsupportedHandlerContext extends Observation.Context {
        @Override
        public String toString() {
            return "sorry";
        }
    }
}
