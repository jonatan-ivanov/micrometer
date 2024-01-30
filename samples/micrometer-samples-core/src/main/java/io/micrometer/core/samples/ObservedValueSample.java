package io.micrometer.core.samples;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.ObservedValue;
import io.micrometer.observation.ObservedValueRegistrar;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.util.ArrayList;
import java.util.List;

public class ObservedValueSample {
    public static void main(String[] args) {
        // setup
        PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        observationRegistry.observationConfig().observedValueRegistrar(new MicrometerObservedValueRegistrar(meterRegistry));

        // instrumentation
        new ObservedValue("roomTemperature", ObservedValueSample::fetchTemperature)
            .lowCardinalityKeyValues(KeyValue.of("room", "kitchen"))
            .unit("celsius")
            .register(observationRegistry);

//        Gauge.builder("roomTemperature", ObservedValueSample::fetchTemperature)
//            .tags("room", "kitchen")
//            .baseUnit("celsius")
//            .register(meterRegistry);

        // reporting simulation
        for (int i = 0; i < 3; i++) {
            System.out.println(meterRegistry.scrape());
        }
    }

    public static double fetchTemperature() {
        return Math.random() * 5 + 20;
    }

    static class MicrometerObservedValueRegistrar implements ObservedValueRegistrar {
        private final MeterRegistry meterRegistry;

        MicrometerObservedValueRegistrar(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        @Override
        public void register(ObservedValue observedValue) {
            Gauge.builder(observedValue.name(), observedValue::value)
                .tags(createTags(observedValue))
                .baseUnit(observedValue.unit())
                .register(meterRegistry);
        }

        private List<Tag> createTags(ObservedValue observedValue) {
            List<Tag> tags = new ArrayList<>();
            for (KeyValue keyValue : observedValue.lowCardinalityKeyValues()) {
                tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
            }
            return tags;
        }
    }
}
