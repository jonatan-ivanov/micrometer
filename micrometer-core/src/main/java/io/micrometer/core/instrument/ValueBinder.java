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

import io.micrometer.common.lang.Nullable;
import io.micrometer.common.util.internal.logging.WarnThenDebugLogger;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;

public class ValueBinder<T> implements MeterBinder {

    private static final WarnThenDebugLogger logger = new WarnThenDebugLogger(ValueBinder.class);

    private final AtomicReference<T> valueReference;

    private final String name;

    private final ToDoubleFunction<T> toDoubleFunction;

    private final Tags tags;

    @Nullable
    private final String description;

    @Nullable
    private final String baseUnit;

    static <T> ValueBinder.Builder<T> builder(String name, @Nullable T initialValue,
            ToDoubleFunction<T> toDoubleFunction) {
        return new ValueBinder.Builder<>(name, initialValue, toDoubleFunction);
    }

    private ValueBinder(String name, @Nullable T initialValue, ToDoubleFunction<T> toDoubleFunction, Tags tags,
            @Nullable String description, @Nullable String baseUnit) {
        this.name = name;
        this.valueReference = new AtomicReference<>(initialValue);
        this.toDoubleFunction = toDoubleFunction;
        this.tags = tags;
        this.description = description;
        this.baseUnit = baseUnit;
    }

    @Nullable
    public T getValue() {
        return valueReference.get();
    }

    public void setValue(@Nullable T value) {
        valueReference.set(value);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        registry.gauge(new Meter.Id(name, tags, baseUnit, description, Meter.Type.GAUGE, null), valueReference,
                this::toDouble);
    }

    private double toDouble(AtomicReference<T> valueReference) {
        return toDouble(valueReference.get());
    }

    private double toDouble(T value) {
        try {
            return toDoubleFunction.applyAsDouble(value);
        }
        catch (Exception e) {
            logger.log("Failed to convert value to double.", e);
            return Double.NaN;
        }
    }

    static class Builder<T> {

        private final String name;

        @Nullable
        private final T initialValue;

        private final ToDoubleFunction<T> toDoubleFunction;

        private Tags tags = Tags.empty();

        @Nullable
        private String description;

        @Nullable
        private String baseUnit;

        private Builder(String name, @Nullable T initialValue, ToDoubleFunction<T> toDoubleFunction) {
            this.name = name;
            this.initialValue = initialValue;
            this.toDoubleFunction = toDoubleFunction;
        }

        public Builder<T> tags(String... tags) {
            return tags(Tags.of(tags));
        }

        public Builder<T> tags(Iterable<Tag> tags) {
            this.tags = this.tags.and(tags);
            return this;
        }

        public Builder<T> tag(String key, String value) {
            this.tags = tags.and(key, value);
            return this;
        }

        public Builder<T> description(@Nullable String description) {
            this.description = description;
            return this;
        }

        public Builder<T> baseUnit(@Nullable String unit) {
            this.baseUnit = unit;
            return this;
        }

        public ValueBinder<T> build() {
            return new ValueBinder<>(name, initialValue, toDoubleFunction, tags, description, baseUnit);
        }

    }

}
