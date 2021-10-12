package io.micrometer.core.instrument.event;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.event.composite.CompositeContext;

public class SimpleRichSample implements RichSample {
    private final String name;
    private final Clock clock;
    private final RichSampleListener<?> listener;
    private final CompositeContext context;

    private String detailedName;
    private String description = null;
    private Duration duration = Duration.ZERO;
    private long started = 0;
    private long stopped = 0;
    private long startWallTime = 0;
    private final Set<Tag> tags = new LinkedHashSet<>();
    private Throwable error = null;

    public SimpleRichSample(String name, Clock clock, RichSampleListener<CompositeContext> listener) {
        this.name = name;
        this.detailedName = name;
        this.listener = listener;
        this.context = listener.createContext();
        this.clock = clock;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDetailedName() {
        return detailedName;
    }

    @Override
    public SimpleRichSample detailedName(String detailedName) {
        verifyIfHasNotStopped();
        this.detailedName = detailedName;
        return this;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public RichSample description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public Duration getDuration() {
        return this.duration;
    }

    @Override
    public long getStartNanos() {
        return this.started;
    }

    @Override
    public long getStopNanos() {
        return this.stopped;
    }

    @Override
    public long getStartWallTime() {
        return this.startWallTime;
    }

    @Override
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(this.tags);
    }

    @Override
    public SimpleRichSample tag(String key, String value) {
        return tag(Tag.of(key, value));
    }

    @Override
    public SimpleRichSample tag(Tag tag) {
        verifyIfHasNotStopped();
        this.tags.add(tag);
        return this;
    }

    @Override
    public Throwable getError() {
        return this.error;
    }

    @Override
    public SimpleRichSample error(Throwable error) {
        verifyIfHasStarted();
        verifyIfHasNotStopped();
        this.error = error;
        this.listener.onError(this);
        return this;
    }

    @Override
    public SimpleRichSample start() {
        return start(clock.wallTime(), clock.monotonicTime());
    }

    @Override
    public SimpleRichSample start(long wallTime, long monotonicTime) {
        if (this.started != 0) {
            throw new IllegalStateException("SimpleRichSample has already been started");
        }

        this.startWallTime = wallTime;
        this.started = monotonicTime;
        this.listener.onStart(this);

        return this;
    }

    @Override
    public void stop() {
        stop(clock.monotonicTime());
    }

    @Override
    public void stop(long monotonicTime) {
        verifyIfHasStarted();
        verifyIfHasNotStopped();

        this.stopped = monotonicTime;
        this.duration = Duration.ofNanos(this.stopped - this.started);
        this.listener.onStop(this);
    }

    @Override
    public <T> T getContext(RichSampleListener<T> listener) {
        return this.context.byListener(listener);
    }

    @Override
    public String toString() {
        return "SimpleRichSample{" +
                "name='" + name + '\'' +
                ", detailedName='" + detailedName + '\'' +
                ", startWallTime=" + startWallTime +
                ", duration=" + duration +
                ", tags=" + tags +
                ", error=" + error +
                '}';
    }

    private void verifyIfHasStarted() {
        if (this.started == 0) {
            throw new IllegalStateException("SimpleRichSample hasn't been started");
        }
    }

    private void verifyIfHasNotStopped() {
        if (this.stopped != 0) {
            throw new IllegalStateException("SimpleRichSample has already been stopped");
        }
    }
}
