package io.micrometer.core.instrument;

import java.io.IOException;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.event.listener.composite.CompositeContext;

public class SampleTest {

    @Test
    void letsSee() {
        MeterRegistry registry = new SimpleMeterRegistry();
        registry.setListener(new TestListener());

        Timer.Sample sample = Timer.start(registry);

        sample.start();
        sample.tag(Tag.of("k1", "v1"));
        sample.error(new IOException("simulated"));
        sample.stop();

        sample.stop(Timer.builder("test").register(registry));
    }

    static class TestListener implements RecordingListener<CompositeContext> {
        @Override
        public CompositeContext createContext() {
            return null;
        }

        @Override
        public void onStart(Timer.Sample sample) {
            System.out.println("start "  + sample);
        }

        @Override
        public void onStop(Timer.Sample sample) {
            System.out.println("stop "  + sample);
        }

        @Override
        public void onError(Timer.Sample sample) {
            System.out.println("error "  + sample);
        }
    }
}
