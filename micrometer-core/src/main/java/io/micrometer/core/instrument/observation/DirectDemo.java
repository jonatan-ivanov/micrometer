/*
 * Copyright 2022 VMware, Inc.
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

package io.micrometer.core.instrument.observation;

import java.io.IOException;

import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.Observation;

public class DirectDemo {
    public static void main(String[] args) {
        MockClock clock = new MockClock();
        SimpleMeterRegistry registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, clock);

        Observation observation = DirectMetersObservation.createNotStarted("observation", registry)
                .lowCardinalityKeyValue("a", "42")
                .start();
        clock.addSeconds(1);

        observation.event(new Observation.Event("event"));
        observation.error(new IOException("simulated"));

        System.out.println(registry.getMetersAsString());

        clock.addSeconds(1);
        observation.stop();

        System.out.println("---");
        System.out.println(registry.getMetersAsString());
    }
}
