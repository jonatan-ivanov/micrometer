package io.micrometer.core.instrument.event;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import io.micrometer.core.instrument.Tag;

public class NoOpRichSample implements RichSample {
    private static final Set<Tag> TAGS = Collections.emptySet();

    @Override
    public String getName() {
        return "noop";
    }

    @Override
    public String getDetailedName() {
        return "noop";
    }

    @Override
    public RichSample detailedName(String detailedName) {
        return this;
    }

    @Override
    public String getDescription() {
        return "noop rich sample";
    }

    @Override
    public RichSample description(String description) {
        return this;
    }

    @Override
    public Duration getDuration() {
        return Duration.ZERO;
    }

    @Override
    public long getStartNanos() {
        return 0;
    }

    @Override
    public long getStopNanos() {
        return 0;
    }

    @Override
    public long getStartWallTime() {
        return 0;
    }

    @Override
    public Set<Tag> getTags() {
        return TAGS;
    }

    @Override
    public RichSample tag(String key, String value) {
        return this;
    }

    @Override
    public RichSample tag(Tag tag) {
        return this;
    }

    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public RichSample error(Throwable error) {
        return this;
    }

    @Override
    public RichSample start() {
        return this;
    }

    @Override
    public RichSample start(long wallTime, long monotonicTime) {
        return this;
    }

    @Override
    public void stop() {
    }

    @Override
    public void stop(long monotonicTime) {
    }

    @Override
    public <T> T getContext(RichSampleListener<T> listener) {
        return null;
    }
}
