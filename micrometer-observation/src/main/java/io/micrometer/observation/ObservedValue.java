package io.micrometer.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.common.lang.Nullable;

import java.util.function.Supplier;

public class ObservedValue {

    private final String name;
    private final Supplier<Double> valueSupplier;
    private KeyValues lowCardinalityKeyValues;

    @Nullable
    private String unit;

    public ObservedValue(String name, Supplier<Double> valueSupplier) {
        this.name = name;
        this.valueSupplier = valueSupplier;
        this.lowCardinalityKeyValues = KeyValues.empty();
    }

    public String name() {
        return name;
    }

    public double value() {
        return valueSupplier.get();
    }

    public KeyValues lowCardinalityKeyValues() {
        return lowCardinalityKeyValues;
    }

    public ObservedValue lowCardinalityKeyValues(KeyValue... lowCardinalityKeyValues) {
        this.lowCardinalityKeyValues = this.lowCardinalityKeyValues.and(lowCardinalityKeyValues);
        return this;
    }

    @Nullable
    public String unit() {
        return unit;
    }

    public ObservedValue unit(String unit) {
        this.unit = unit;
        return this;
    }

    public void register(ObservationRegistry registry) {
        for (ObservedValueRegistrar registrar : registry.observationConfig().getObservedValueRegistrars()) {
            registrar.register(this);
        }
    }
}
