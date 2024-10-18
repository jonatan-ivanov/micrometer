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
package io.micrometer.registry.otlp;

import com.google.protobuf.ByteString;
import io.micrometer.common.lang.Nullable;
import io.micrometer.core.instrument.Clock;
import io.opentelemetry.proto.metrics.v1.Exemplar;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

class OtlpExemplarSampler implements ExemplarSampler {

    private final ExemplarContextProvider exemplarContextProvider;

    private final Clock clock;

    @Nullable
    private volatile Exemplar lastExemplar;

    OtlpExemplarSampler(ExemplarContextProvider exemplarContextProvider, Clock clock) {
        this.exemplarContextProvider = exemplarContextProvider;
        this.clock = clock;
    }

    @Override
    public void sampleMeasurement(double measurement) {
        OtlpExemplarContext exemplarContext = exemplarContextProvider.getExemplarContext();
        lastExemplar = exemplarContext != null ? createExemplar(measurement, exemplarContext) : null;
    }

    @Override
    public List<Exemplar> collectExemplars() {
        return lastExemplar != null ? Collections.singletonList(lastExemplar) : Collections.emptyList();
    }

    private Exemplar createExemplar(double measurement, OtlpExemplarContext exemplarContext) {
        return Exemplar.newBuilder()
            .setAsDouble(measurement)
            .setSpanId(ByteString.fromHex(exemplarContext.getSpanId()))
            .setTraceId(ByteString.fromHex(exemplarContext.getTraceId()))
            .setTimeUnixNano(TimeUnit.MILLISECONDS.toNanos(clock.wallTime()))
            .build();
    }

}
