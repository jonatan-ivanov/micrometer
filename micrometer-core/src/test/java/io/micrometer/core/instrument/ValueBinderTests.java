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
package io.micrometer.core.instrument;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ValueBinder}.
 */
class ValueBinderTests {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    @Test
    void changeValue() {
        ValueBinder<String> valueBinder = ValueBinder.builder("test", "123", String::length)
            .description("Number of code units of the current string value")
            .tag("k1", "v1")
            .baseUnit("codeUnits")
            .build();
        valueBinder.bindTo(registry);

        assertThat(valueBinder.getValue()).isEqualTo("123");
        assertThat(registry.get("test").tag("k1", "v1").gauges()).hasSize(1);
        assertThat(registry.get("test").tag("k1", "v1").gauge().value()).isEqualTo(3);

        valueBinder.setValue("12345");

        assertThat(valueBinder.getValue()).isEqualTo("12345");
        assertThat(registry.get("test").tag("k1", "v1").gauges()).hasSize(1);
        assertThat(registry.get("test").tag("k1", "v1").gauge().value()).isEqualTo(5);
    }

    @Test
    void nullValue() {
        ValueBinder<String> valueBinder = ValueBinder.builder("nullTest", null, String::length).build();
        valueBinder.bindTo(registry);

        assertThat(valueBinder.getValue()).isNull();
        assertThat(registry.get("nullTest").gauges()).hasSize(1);
        assertThat(registry.get("nullTest").gauge().value()).isNaN();

        valueBinder.setValue("123");

        assertThat(valueBinder.getValue()).isEqualTo("123");
        assertThat(registry.get("nullTest").gauges()).hasSize(1);
        assertThat(registry.get("nullTest").gauge().value()).isEqualTo(3);
    }

}
