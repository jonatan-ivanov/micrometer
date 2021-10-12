package io.micrometer.core.instrument.event;

public interface ContextFactory<T> {
    T createContext();
}
