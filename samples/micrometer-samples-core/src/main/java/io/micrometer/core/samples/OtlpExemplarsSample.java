/*
 * Copyright 2025 VMware, Inc.
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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.registry.otlp.*;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class OtlpExemplarsSample {

    public static void main(String[] args) throws InterruptedException {
        OtlpConfig config = new OtlpConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            public AggregationTemporality aggregationTemporality() {
                return AggregationTemporality.DELTA;
            }

            @Override
            public String get(String key) {
                return null;
            }
        };

        MeterRegistry registry = OtlpMeterRegistry.builder(config)
            .metricsSender(new TestMetricsSender())
            .exemplarContextProvider(new TestExemplarContextProvider())
            .build();

        for (int i = 0; i < 130; i++) {
            System.out.print(i + 1 + " ");
            registry.counter("test").increment();
            Thread.sleep(1_000);
        }

        registry.close();
    }

    static class TestExemplarContextProvider implements ExemplarContextProvider {

        private final AtomicLong counter = new AtomicLong(1001);

        @Override
        public OtlpExemplarContext getExemplarContext() {
            String suffix = String.valueOf(counter.getAndIncrement());
            return new OtlpExemplarContext("66fd7359621b3043e2321480aaaa" + suffix, "e2321480aaaa" + suffix);
        }

    }

    static class TestMetricsSender implements OtlpMetricsSender {

        @Override
        public void send(Request request) throws Exception {
            System.out.println("Publishing...");
            System.out.println(ExportMetricsServiceRequest.parseFrom(request.getMetricsData()));
        }

    }

}
