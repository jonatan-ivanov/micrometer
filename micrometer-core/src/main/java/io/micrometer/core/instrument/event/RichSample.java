package io.micrometer.core.instrument.event;

import java.time.Duration;
import java.util.Set;

import io.micrometer.core.instrument.Tag;

public interface RichSample {
    String getName();
    String getDetailedName();
    RichSample detailedName(String detailedName);
    String getDescription();
    RichSample description(String description);

    Duration getDuration();
    long getStartNanos();
    long getStopNanos();
    long getStartWallTime();

    Set<Tag> getTags();
    RichSample tag(String key, String value);
    RichSample tag(Tag tag);

    Throwable getError();
    RichSample error(Throwable error);

    RichSample start();
    RichSample start(long wallTime, long monotonicTime);

    void stop();
    void stop(long monotonicTime);

    <T> T getContext(RichSampleListener<T> listener);
}
