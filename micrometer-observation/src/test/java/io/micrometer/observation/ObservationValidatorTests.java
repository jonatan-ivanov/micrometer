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
package io.micrometer.observation;

import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.Observation.Event;
import io.micrometer.observation.Observation.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ObservationValidator}.
 *
 * @author Jonatan Ivanov
 */
class ObservationValidatorTests {

    private TestConsumer testConsumer;

    private ObservationRegistry registry;

    @BeforeEach
    void setUp() {
        testConsumer = new TestConsumer();
        registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new ObservationValidator(testConsumer));
    }

    @Test
    void doubleStartShouldBeInvalid() {
        Observation.start("test", registry).start();
        assertThat(testConsumer.toString()).isEqualTo("Invalid start: Observation has already been started");
    }

    @Test
    void stopBeforeStartShouldBeInvalid() {
        Observation.createNotStarted("test", registry).stop();
        assertThat(testConsumer.toString()).isEqualTo("Invalid stop: Observation has not been started yet");
    }

    @Test
    void errorBeforeStartShouldBeInvalid() {
        Observation.createNotStarted("test", registry).error(new RuntimeException());
        assertThat(testConsumer.toString()).isEqualTo("Invalid error signal: Observation has not been started yet");
    }

    @Test
    void eventBeforeStartShouldBeInvalid() {
        Observation.createNotStarted("test", registry).event(Event.of("test"));
        assertThat(testConsumer.toString()).isEqualTo("Invalid event signal: Observation has not been started yet");
    }

    @Test
    void scopeBeforeStartShouldBeInvalid() {
        Scope scope = Observation.createNotStarted("test", registry).openScope();
        scope.reset();
        scope.close();
        assertThat(testConsumer.toString()).isEqualTo("Invalid scope opening: Observation has not been started yet\n"
                + "Invalid scope resetting: Observation has not been started yet\n"
                + "Invalid scope closing: Observation has not been started yet");
    }

    @Test
    void observeAfterStartShouldBeInvalid() {
        Observation.start("test", registry).observe(() -> "");
        assertThat(testConsumer.toString()).isEqualTo("Invalid start: Observation has already been started");
    }

    @Test
    void doubleStopShouldBeInvalid() {
        Observation observation = Observation.start("test", registry);
        observation.stop();
        observation.stop();
        assertThat(testConsumer.toString()).isEqualTo("Invalid stop: Observation has already been stopped");
    }

    @Test
    void errorAfterStopShouldBeInvalid() {
        Observation observation = Observation.start("test", registry);
        observation.stop();
        observation.error(new RuntimeException());
        assertThat(testConsumer.toString()).isEqualTo("Invalid error signal: Observation has already been stopped");
    }

    @Test
    void eventAfterStopShouldBeInvalid() {
        Observation observation = Observation.start("test", registry);
        observation.stop();
        observation.event(Event.of("test"));
        assertThat(testConsumer.toString()).isEqualTo("Invalid event signal: Observation has already been stopped");
    }

    @Test
    void scopeAfterStopShouldBeInvalid() {
        Observation observation = Observation.start("test", registry);
        observation.stop();
        Scope scope = observation.openScope();
        scope.reset();
        scope.close();
        assertThat(testConsumer.toString()).isEqualTo("Invalid scope opening: Observation has already been stopped\n"
                + "Invalid scope resetting: Observation has already been stopped\n"
                + "Invalid scope closing: Observation has already been stopped");
    }

    static class TestConsumer implements BiConsumer<String, Context> {

        private final StringBuilder stringBuilder = new StringBuilder();

        @Override
        public void accept(String message, Context context) {
            stringBuilder.append(message).append("\n");
        }

        @Override
        public String toString() {
            return stringBuilder.toString().trim();
        }

    }

}
