package io.micrometer.core.samples.richsample;

import java.util.concurrent.TimeUnit;

import brave.Span;
import brave.Tracer;
import io.micrometer.core.instrument.event.RichSample;
import io.micrometer.core.instrument.event.RichSampleListener;

public class SpanRichSampleListener implements RichSampleListener<SpanRichSampleListener.BraveContext> {
    private final Tracer tracer;

    public SpanRichSampleListener(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void onStart(RichSample sample) {
        Span span = tracer.nextSpan().start(getStartTimeInMicros(sample));
        sample.getContext(this).setSpanAndScope(span, tracer.withSpanInScope(span));
    }

    @Override
    public void onStop(RichSample sample) {
        BraveContext context = sample.getContext(this);
        Span span = context.getSpan().name(sample.getDetailedName());
        sample.getTags().forEach(tag -> span.tag(tag.getKey(), tag.getValue()));
        context.getSpanInScope().close();
        span.finish(getStopTimeInMicros(sample));
    }

    @Override
    public void onError(RichSample sample) {
        sample.getContext(this).getSpan().error(sample.getError());
    }

    private long getStartTimeInMicros(RichSample richSample) {
        return TimeUnit.NANOSECONDS.toMicros(richSample.getStartWallTime());
    }

    private long getStopTimeInMicros(RichSample richSample) {
        return TimeUnit.NANOSECONDS.toMicros(richSample.getStartWallTime() + richSample.getDuration().toNanos());
    }

    @Override
    public BraveContext createContext() {
        return new BraveContext();
    }

    static class BraveContext {
        private Span span;
        private Tracer.SpanInScope spanInScope;

        Span getSpan() {
            return span;
        }

        Tracer.SpanInScope getSpanInScope() {
            return spanInScope;
        }

        void setSpanAndScope(Span span, Tracer.SpanInScope spanInScope) {
            this.span = span;
            this.spanInScope = spanInScope;
        }
    }
}
