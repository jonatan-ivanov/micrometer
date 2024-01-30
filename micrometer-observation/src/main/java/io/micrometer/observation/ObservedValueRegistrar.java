package io.micrometer.observation;

public interface ObservedValueRegistrar {
    void register(ObservedValue observedValue);
}
