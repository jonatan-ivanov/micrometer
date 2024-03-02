/*
 * Copyright 2024 VMware, Inc.
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

package io.micrometer.prometheus;

import io.micrometer.core.instrument.Clock;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.metrics.expositionformats.ExpositionFormats;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PrometheusDemo {

    private static final ExpositionFormats expositionFormats = ExpositionFormats.init();

    private static final CollectorRegistry collectorRegistry = new CollectorRegistry();

    private static final PrometheusRegistry prometheusRegistry = new PrometheusRegistry();

    public static void main(String[] args) throws IOException {
        PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry,
                prometheusRegistry, Clock.SYSTEM, null);
        meterRegistry.counter("mr.test", "status", "ok").increment();

        io.prometheus.client.Counter.build()
            .name("old_test")
            .help("old test counter")
            .labelNames("status")
            .register(collectorRegistry)
            .labels("ok")
            .inc();

        io.prometheus.metrics.core.metrics.Counter.builder()
            .name("new_test")
            .help("new test counter")
            .labelNames("status")
            .register(prometheusRegistry)
            .labelValues("ok")
            .inc();

         System.out.println("MR Scrape");
         System.out.println(meterRegistry.scrape());
         System.out.println();

         System.out.println("PR Scrape");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new PrometheusTextFormatWriter(false).write(outputStream, prometheusRegistry.scrape());
        System.out.println(outputStream);
    }

}
