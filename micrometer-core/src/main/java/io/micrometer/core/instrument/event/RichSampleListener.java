package io.micrometer.core.instrument.event;

public interface RichSampleListener<T> extends ContextFactory<T> {
    void onStart(RichSample sample);
    void onStop(RichSample sample);
    void onError(RichSample sample);
}
