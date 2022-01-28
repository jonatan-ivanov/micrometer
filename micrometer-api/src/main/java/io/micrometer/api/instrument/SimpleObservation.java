package io.micrometer.api.instrument;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.micrometer.api.lang.Nullable;

public class SimpleObservation implements Observation {
    private final String name;
    private final MeterRegistry registry;
    private String displayName;
    private final Set<Tag> lowCardinalityTags = new LinkedHashSet<>(); // We can add cardinality to the Tag interface (we did before) so that we can have just one Set<Tag> tags
    private final Set<Tag> highCardinalityTags = new LinkedHashSet<>(); // We can also use the Tags class, but it is an immutable collection which copies everything over to a new instance when you want to add something
    private long started = 0;
    private final Context context;
    @SuppressWarnings("rawtypes")
    private final Collection<ObservationHandler> handlers;

    public SimpleObservation(String name, MeterRegistry registry) {
        this(name, registry, new Context());
    }

    public SimpleObservation(String name, MeterRegistry registry, Context context) {
        this.name = name;
        this.registry = registry;
        this.displayName = name;
        this.context = context;
        this.handlers = registry.config().getObservationHandlers().stream()
                .filter(handler -> handler.supportsContext(this.context))
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public Observation displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public Iterable<Tag> getLowCardinalityTags() {
        return this.context.getLowCardinalityTags().and(this.lowCardinalityTags);
    }

    @Override
    public Observation lowCardinalityTag(Tag tag) {
        this.lowCardinalityTags.add(tag);
        return this;
    }

    @Override
    public Iterable<Tag> getHighCardinalityTags() {
        return this.context.getHighCardinalityTags().and(this.highCardinalityTags);
    }

    @Override
    public Observation highCardinalityTag(Tag tag) {
        this.highCardinalityTags.add(tag);
        return this;
    }

    @Override
    public Observation error(Throwable error) {
        this.notifyOnError(error);
        return this;
    }

    @Override
    public Observation start() {
        this.started = this.registry.config().clock().monotonicTime();
        this.notifyOnObservationStarted();
        return this;
    }

    @Override
    public void stop() {
        this.notifyOnObservationStopped(Duration.ofNanos(this.registry.config().clock().monotonicTime() - this.started));
    }

    @Override
    public Scope openScope() {
        Scope scope = new SimpleScope(this.registry, this);
        this.notifyOnScopeOpened();
        return scope;
    }

    @Override
    public String toString() {
        return "{"
                + "name=" + this.getName() + "(" + this.getDisplayName() + ")"
                + ", lowCardTags=" + this.getLowCardinalityTags()
                + ", highCardTags=" + this.getHighCardinalityTags()
                + ", context=" + this.context
                + '}';
    }

    @SuppressWarnings("unchecked")
    private void notifyOnObservationStarted() {
        this.handlers.forEach(handler -> handler.onStart(this, this.context));
    }

    @SuppressWarnings("unchecked")
    private void notifyOnError(Throwable error) {
        this.handlers.forEach(handler -> handler.onError(this, this.context, error));
    }

    @SuppressWarnings("unchecked")
    private void notifyOnScopeOpened() {
        this.handlers.forEach(handler -> handler.onScopeOpened(this, this.context));
    }

    @SuppressWarnings("unchecked")
    private void notifyOnScopeClosed() {
        this.handlers.forEach(handler -> handler.onScopeClosed(this, this.context));
    }

    @SuppressWarnings("unchecked")
    private void notifyOnObservationStopped(Duration duration) {
        this.handlers.forEach(handler -> handler.onStop(this, this.context, duration));
    }

    static class SimpleScope implements Scope {
        private final MeterRegistry registry;
        private final SimpleObservation currentObservation;
        @Nullable private final Observation previousObservation;

        SimpleScope(MeterRegistry registry, SimpleObservation current) {
            this.registry = registry;
            this.currentObservation = current;
            this.previousObservation = registry.getCurrentObservation();
            this.registry.setCurrentObservation(current);
        }

        @Override
        public Observation getCurrentObservation() {
            return this.currentObservation;
        }

        @Override
        public void close() {
            this.registry.setCurrentObservation(previousObservation);
            this.currentObservation.notifyOnScopeClosed();
        }
    }
}
