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

package io.micrometer.concurrencytests;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.Z_Result;

import java.util.UUID;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

public class ObservationContextConcurrencyTest {

    @JCStressTest
    @State
    @Outcome(id = "true", expect = ACCEPTABLE)
    @Outcome(expect = FORBIDDEN)
    public static class ConsistentKeyValues {

        private final Observation.Context context = new TestContext();

        @Actor
        public void read(Z_Result r) {
            try {
                context.getHighCardinalityKeyValues();
                r.r1 = true;
            }
            catch (Exception e) {
                r.r1 = false;
            }
        }

        @Actor
        public void write(Z_Result r) {
            try {
                String uuid = UUID.randomUUID().toString();
                context.addHighCardinalityKeyValue(KeyValue.of(uuid, uuid));
                r.r1 = true;
            }
            catch (Exception e) {
                r.r1 = false;
            }
        }

    }

    static class TestContext extends Observation.Context {

    }

}
