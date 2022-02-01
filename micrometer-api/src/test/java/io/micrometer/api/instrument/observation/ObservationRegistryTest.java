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
package io.micrometer.api.instrument.observation;

import javax.annotation.Nonnull;

import io.micrometer.api.instrument.Counter;
import io.micrometer.api.instrument.Meter;
import io.micrometer.api.instrument.MeterRegistry;
import io.micrometer.api.instrument.NoopObservation;
import io.micrometer.api.instrument.Timer;
import io.micrometer.api.instrument.config.MeterFilter;
import io.micrometer.api.instrument.config.MeterFilterReply;
import io.micrometer.api.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.api.instrument.distribution.pause.PauseDetector;
import io.micrometer.api.instrument.noop.NoopCounter;
import io.micrometer.api.instrument.noop.NoopTimer;
import io.micrometer.api.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link MeterRegistry}.
 *
 * @author Jon Schneider
 * @author Johnny Lim
 */
class ObservationRegistryTest {
    private ObservationRegistry registry = new SimpleObservationRegistry();
    
    @Test
    void openingScopeShouldSetSampleAsCurrent() {
        Observation sample = registry.start("test.timer");
        Observation.Scope scope = sample.openScope();

        assertThat(registry.getCurrentObservation()).isSameAs(sample);

        scope.close();
        sample.stop();

        assertThat(registry.getCurrentObservation()).isNull();
    }

    @Test
    void timerRecordingHandlerShouldAddThePassedHandler() {
        ObservationHandler<?> handler1 = mock(ObservationHandler.class);
        ObservationHandler<?> handler2 = mock(ObservationHandler.class);

        registry.config().observationHandler(handler1);
        assertThat(registry.config().getObservationHandlers()).containsExactly(handler1);

        registry.config().observationHandler(handler2);
        assertThat(registry.config().getObservationHandlers()).containsExactlyInAnyOrder(handler1, handler2);
    }


    @Test
    void observationShouldBeNoOpWhenPredicateApplicable() {
        registry.config().observationPredicate((name, context) -> !name.equals("test.timer"));

        Observation sample = registry.start("test.timer");

        assertThat(sample).isSameAs(NoopObservation.INSTANCE);
    }
}
