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
package io.micrometer.core.tck;

import io.micrometer.api.instrument.observation.Observation;
import io.micrometer.api.instrument.observation.ObservationHandler;
import io.micrometer.api.instrument.observation.ObservationRegistry;
import io.micrometer.api.instrument.observation.SimpleObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Base class for {@link ObservationHandler} compatibility tests that support {@code null} contexts only.
 * To run a {@link ObservationHandler} implementation against this TCK, make a test class that extends this
 * and implement the abstract methods.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
public abstract class NullHandlerContextTimerRecordingHandlerCompatibilityKit {

    protected ObservationHandler<Observation.Context> handler;

    protected ObservationRegistry meterRegistry = new SimpleObservationRegistry();

    public abstract ObservationHandler<Observation.Context> handler();

    protected Observation sample = meterRegistry.observation("hello");

    @BeforeEach
    void setup() {
        // assigned here rather than at initialization so subclasses can use fields in their registry() implementation
        handler = handler();
    }

    @Test
    @DisplayName("compatibility test provides a null context accepting timer recording handler")
    void handlerSupportsNullContext() {
        assertThatCode(() -> handler.onStart(sample, null)).doesNotThrowAnyException();
        assertThatCode(() -> handler.onStop(sample, null)).doesNotThrowAnyException();
        assertThatCode(() -> handler.onError(sample, null, new RuntimeException())).doesNotThrowAnyException();
        assertThatCode(() -> handler.onScopeOpened(sample, null)).doesNotThrowAnyException();
        assertThatCode(() -> handler.supportsContext(null)).doesNotThrowAnyException();
        assertThat(handler.supportsContext(null)).as("Handler supports null context").isTrue();
    }
}

