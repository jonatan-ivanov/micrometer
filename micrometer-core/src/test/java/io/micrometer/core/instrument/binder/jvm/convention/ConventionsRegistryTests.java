/*
 * Copyright 2026 VMware, Inc.
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
package io.micrometer.core.instrument.binder.jvm.convention;

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.convention.micrometer.MicrometerJvmClassLoadingMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmClassLoadingMeterConventions;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static io.micrometer.core.instrument.binder.jvm.convention.ConventionsRegistryTests.FrameworkProperties.Variants.DEFAULT;
import static io.micrometer.core.instrument.binder.jvm.convention.ConventionsRegistryTests.FrameworkProperties.Variants.OTEL;
import static org.assertj.core.api.Assertions.assertThat;

class ConventionsRegistryTests {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    @Test
    void convention() {
        // User defines the property
        FrameworkProperties properties = new FrameworkProperties("variant", DEFAULT);

        // auto-configuration does this
        ConventionsRegistry conventionsRegistry = conventionsRegistry();
        JvmClassLoadingMeterConventions conventions = jvmClassLoadingMeterConventions(conventionsRegistry, properties);
        ClassLoaderMetrics classLoaderMetrics = classLoaderMetrics(conventions);
        classLoaderMetrics.bindTo(registry);

        System.out.println(registry.getMetersAsString());
        assertThat(registry.get("jvm.classes.loaded").gauge().value()).isGreaterThan(42);
//         assertThat(registry.get("jvm.class.count").gauge().value()).isGreaterThan(42);
    }

    // @Bean
    // @ConditionalOnMissingBean
    ConventionsRegistry conventionsRegistry() {
        ConventionsRegistry conventionsRegistry = new ConventionsRegistry();
        conventionsRegistry.register(JvmClassLoadingMeterConventions.class, DEFAULT, new MicrometerJvmClassLoadingMeterConventions());
        conventionsRegistry.register(JvmClassLoadingMeterConventions.class, OTEL, new OpenTelemetryJvmClassLoadingMeterConventions());
        return conventionsRegistry;
    }

    // @Bean
    // @ConditionalOnMissingBean
    JvmClassLoadingMeterConventions jvmClassLoadingMeterConventions(ConventionsRegistry conventionsRegistry, FrameworkProperties properties) {
        JvmClassLoadingMeterConventions conventions = conventionsRegistry.get(JvmClassLoadingMeterConventions.class, properties.getVariant());
        if (conventions == null) {
            throw new IllegalStateException("No JvmClassLoadingMeterConventions found for variant: " + properties.getVariant());
        }
        return conventions;
    }

    // @Bean
    // @ConditionalOnMissingBean
    ClassLoaderMetrics classLoaderMetrics(JvmClassLoadingMeterConventions conventions) {
        return new ClassLoaderMetrics(conventions);
    }

    static class FrameworkProperties extends Properties {

        FrameworkProperties(String key, Object value) {
            this.put(key, value);
        }

        ConventionsRegistry.Variant getVariant() {
            ConventionsRegistry.Variant variant = (ConventionsRegistry.Variant) get("variant");
            return variant != null ? variant : DEFAULT;
        }

        enum Variants implements ConventionsRegistry.Variant {

            DEFAULT, OTEL;

            @Override
            public @NonNull String getName() {
                return this.name();
            }

        }

    }

}
