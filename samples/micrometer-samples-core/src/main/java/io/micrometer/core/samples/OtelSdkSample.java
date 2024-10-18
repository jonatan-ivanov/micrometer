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

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;

public class OtelSdkSample {

    public static void main(String[] args) {
        OtlpHttpMetricExporter metricExporter = OtlpHttpMetricExporter.builder().build();
        PeriodicMetricReader metricReader = PeriodicMetricReader.builder(metricExporter).build();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().setSampler(Sampler.alwaysOn()).build();
        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
            .setMeterProvider(meterProvider)
            .setTracerProvider(tracerProvider)
            .build();
        Meter meter = openTelemetrySdk.getSdkMeterProvider()
            .meterBuilder("sample")
            .setInstrumentationVersion("0.1.0")
            .build();
        Tracer tracer = openTelemetrySdk.getSdkTracerProvider()
            .tracerBuilder("sample")
            .setInstrumentationVersion("0.1.0")
            .build();

        for (int i = 0; i < 20; i++) {
            Span span = tracer.spanBuilder("test.span").startSpan();
            try (Scope ignored = span.makeCurrent()) {
                meter.counterBuilder("test.counter").build().add(1);
            }
            finally {
                span.end();
            }
        }

        // LongHistogram histogram = meter.histogramBuilder("dice-lib.rolls")
        // .ofLongs() // Required to get a LongHistogram, default is DoubleHistogram
        // .setDescription("A distribution of the value of the rolls.")
        // .setExplicitBucketBoundariesAdvice(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L))
        // .setUnit("points")
        // .build();
        // histogram.record(7);

        openTelemetrySdk.close();
    }

}
