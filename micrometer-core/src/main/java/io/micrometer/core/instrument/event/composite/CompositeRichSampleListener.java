package io.micrometer.core.instrument.event.composite;

import java.util.Arrays;
import java.util.List;

import io.micrometer.core.instrument.event.RichSample;
import io.micrometer.core.instrument.event.RichSampleListener;

public class CompositeRichSampleListener implements RichSampleListener<CompositeContext> {
    private final List<RichSampleListener<?>> listeners;

    public CompositeRichSampleListener(RichSampleListener<?>... listeners) {
        this(Arrays.asList(listeners));
    }

    public CompositeRichSampleListener(List<RichSampleListener<?>> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onStart(RichSample sample) {
        listeners.forEach(listener -> listener.onStart(sample));
    }

    @Override
    public void onStop(RichSample sample) {
        listeners.forEach(listener -> listener.onStop(sample));
    }

    @Override
    public void onError(RichSample sample) {
        listeners.forEach(listener -> listener.onError(sample));
    }

    @Override
    public CompositeContext createContext() {
        return new CompositeContext(listeners);
    }

    List<RichSampleListener<?>> getListeners() {
        return listeners;
    }
}
