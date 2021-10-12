package io.micrometer.core.samples.richsample;

import io.micrometer.core.instrument.event.RichSample;
import io.micrometer.core.instrument.event.SimpleRichSampleListener;

public class SoutRichSampleListener implements SimpleRichSampleListener {
    @Override
    public void onStart(RichSample sample) {
        System.out.println("started: " + sample);
    }

    @Override
    public void onStop(RichSample sample) {
        System.out.println("stopped: " + sample);
    }

    @Override
    public void onError(RichSample sample) {
        System.out.println("error: " + sample);
    }
}
