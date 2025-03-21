/*
 * Copyright 2024 VMware, Inc.
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
package io.micrometer.java21.instrument.binder.jdk;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import jdk.jfr.consumer.RecordingStream;

import java.time.Duration;

import static java.util.Collections.emptyList;

/**
 * Instrumentation support for Virtual Threads, see:
 * https://openjdk.org/jeps/425#JDK-Flight-Recorder-JFR
 *
 * @author Artyom Gabeev
 * @since 1.14.0
 */
public class VirtualThreadMetrics extends JfrMeterBinder {

    private static final String PINNED_EVENT = "jdk.VirtualThreadPinned";

    private static final String SUBMIT_FAILED_EVENT = "jdk.VirtualThreadSubmitFailed";

    private final Iterable<Tag> tags;

    public VirtualThreadMetrics() {
        this(emptyList());
    }

    public VirtualThreadMetrics(Iterable<Tag> tags) {
        this.tags = tags;
    }

    @Override
    protected void configure(RecordingStream recordingStream) {
        super.configure(recordingStream);
        recordingStream.enable(PINNED_EVENT).withThreshold(Duration.ofMillis(20));
        recordingStream.enable(SUBMIT_FAILED_EVENT);
    }

    @Override
    protected void register(MeterRegistry registry, RecordingStream recordingStream) {
        Timer pinnedTimer = Timer.builder("jvm.threads.virtual.pinned")
            .description("The duration while the virtual thread was pinned without releasing its platform thread")
            .tags(tags)
            .register(registry);

        Counter submitFailedCounter = Counter.builder("jvm.threads.virtual.submit.failed")
            .description("The number of events when starting or unparking a virtual thread failed")
            .tags(tags)
            .register(registry);

        recordingStream.onEvent(PINNED_EVENT, event -> pinnedTimer.record(event.getDuration()));
        recordingStream.onEvent(SUBMIT_FAILED_EVENT, event -> submitFailedCounter.increment());
    }

}
