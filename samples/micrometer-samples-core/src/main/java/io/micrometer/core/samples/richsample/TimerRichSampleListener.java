package io.micrometer.core.samples.richsample;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.event.RichSample;
import io.micrometer.core.instrument.event.SimpleRichSampleListener;

public class TimerRichSampleListener implements SimpleRichSampleListener {
    private final MeterRegistry registry;

    public TimerRichSampleListener(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onStart(RichSample sample) {
    }

    @Override
    public void onStop(RichSample sample) {
        Timer.builder(sample.getName())
                .description(sample.getDescription())
                .tags(sample.getTags()) // .filter(tag -> tag.getCardinality() == Cardinality.LOW) ???
                .tag("error", sample.getError() != null ? sample.getError().getClass().getSimpleName() : "none")
                .register(registry)
                .record(sample.getDuration());
    }

    @Override
    public void onError(RichSample sample) {
    }
}
