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

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;
import io.micrometer.core.instrument.*;
import io.micrometer.observation.*;

public class DirectMetersObservation implements Observation {
    private static final Context EMPTY_CONTEXT = new EmptyContext();
    private final String name;
    private final MeterRegistry registry;
    private final Set<Tag> tags;
    @Nullable
    private Throwable error;
    @Nullable
    private LongTaskTimer.Sample longTaskSample;
    @Nullable
    private Timer.Sample sample;

    public static Observation start(String name, MeterRegistry registry) {
        return createNotStarted(name, registry).start();
    }

    public static Observation createNotStarted(String name, MeterRegistry registry) {
        // TODO: Make the registry nullable and return a noop observation
        return new DirectMetersObservation(name, registry);
    }

    static Observation start(ObservationConvention<Context> observationConvention, MeterRegistry registry) {
        return createNotStarted(observationConvention, registry).start();
    }

    static Observation createNotStarted(ObservationConvention<Context> observationConvention, MeterRegistry registry) {
        // TODO: Make the registry nullable and return a noop observation
        return new DirectMetersObservation(observationConvention, registry);
    }

    DirectMetersObservation(String name, MeterRegistry registry) {
        this.name = name;
        this.registry = registry;
        this.tags = new LinkedHashSet<>();
    }

    DirectMetersObservation(ObservationConvention<Context> convention, MeterRegistry registry) {
        if (convention.supportsContext(EMPTY_CONTEXT)) {
            this.name = convention.getName();
            this.registry = registry;
            this.tags = new LinkedHashSet<>();
            this.observationConvention(convention);
        }
        else {
            throw new IllegalStateException("Convention [" + convention + "] doesn't support context [" + EMPTY_CONTEXT + "]");
        }
    }

    @Override
    public Observation contextualName(@Nullable String contextualName) {
        return this;
    }

    @Override
    public Observation parentObservation(Observation parentObservation) {
        return this;
    }

    @Override
    public Observation lowCardinalityKeyValue(KeyValue keyValue) {
        this.tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
        return this;
    }

    @Override
    public Observation highCardinalityKeyValue(KeyValue keyValue) {
        return this;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Observation observationConvention(ObservationConvention<? extends Context> observationConvention) {
        if (observationConvention.supportsContext(EMPTY_CONTEXT)) {
            this.lowCardinalityKeyValues(((ObservationConvention) observationConvention).getLowCardinalityKeyValues(EMPTY_CONTEXT));
        }
        return this;
    }

    @Override
    public Observation error(Throwable error) {
        this.error = error;
        return this;
    }

    @Override
    public Observation event(Event event) {
        Counter.builder(this.name + "." + event.getName())
                .tags(this.tags)
                .register(this.registry)
                .increment();
        return this;
    }

    @Override
    public Observation start() {
        this.longTaskSample = LongTaskTimer.builder(this.name + ".active")
                .tags(this.tags)
                .register(this.registry)
                .start();
        this.sample = Timer.start(this.registry);
        return this;
    }

    @Override
    public ContextView getContext() {
        return EMPTY_CONTEXT;
    }

    @Override
    public void stop() {
        this.sample.stop(
                Timer.builder(this.name)
                        .tags(this.tags)
                        .tags(createErrorTags(this.error))
                        .register(this.registry)
        );
        this.longTaskSample.stop();
    }

    private Tags createErrorTags(@Nullable Throwable error) {
        if (error != null) {
            return Tags.of("error", error.getClass().getSimpleName());
        }
        else {
            return Tags.of("error", "none");
        }
    }

    @Override
    public Scope openScope() {
        return Scope.NOOP;
    }

    /**
     * Only needed because {@link Observation#getContext()} can't return null.
     */
    private static class EmptyContext extends Context {
        @Override
        public String getName() {
            return "";
        }

        @Override
        public void setName(String name) {
        }

        @Override
        public String getContextualName() {
            return "";
        }

        @Override
        public void setContextualName(@Nullable String contextualName) {
        }

        @Nullable
        @Override
        public Observation getParentObservation() {
            return null;
        }

        @Override
        public void setParentObservation(@Nullable Observation parentObservation) {
        }

        @Override
        public Optional<Throwable> getError() {
            return Optional.empty();
        }

        @Override
        public void setError(Throwable error) {
        }

        @Override
        public <T> Context put(Object key, T object) {
            return this;
        }

        @Nullable
        @Override
        public <T> T get(Object key) {
            return null;
        }

        @Override
        public Object remove(Object key) {
            return null;
        }

        @NonNull
        @Override
        public <T> T getRequired(Object key) {
            throw new IllegalArgumentException("Context does not have an entry for key [" + key + "]");
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public <T> T getOrDefault(Object key, T defaultObject) {
            return defaultObject;
        }

        @Override
        public <T> T computeIfAbsent(Object key, Function<Object, ? extends T> mappingFunction) {
            return null;
        }

        @Override
        public void clear() {
        }

        @NonNull
        @Override
        public KeyValues getLowCardinalityKeyValues() {
            return KeyValues.empty();
        }

        @NonNull
        @Override
        public KeyValues getHighCardinalityKeyValues() {
            return KeyValues.empty();
        }

        @NonNull
        @Override
        public KeyValues getAllKeyValues() {
            return KeyValues.empty();
        }

        @Override
        public String toString() {
            return "EmptyContext";
        }
    }
}
