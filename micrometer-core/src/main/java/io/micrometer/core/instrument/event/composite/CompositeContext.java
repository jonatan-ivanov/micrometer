package io.micrometer.core.instrument.event.composite;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import io.micrometer.core.instrument.event.RichSampleListener;

public class CompositeContext {
    private final Map<RichSampleListener<?>, Object> contexts = new IdentityHashMap<>();

    CompositeContext(RichSampleListener<?>... listeners) {
        this(Arrays.asList(listeners));
    }

    CompositeContext(List<? extends RichSampleListener<?>> listeners) {
        addContexts(listeners);
    }

    private void addContexts(List<? extends RichSampleListener<?>> listeners) {
        for (RichSampleListener<?> listener : listeners) {
            if (listener instanceof CompositeRichSampleListener) {
                addContexts(((CompositeRichSampleListener) listener).getListeners());
            }
            else {
                this.contexts.put(listener, listener.createContext());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T byListener(RichSampleListener<?> listener) {
        return (T) this.contexts.get(listener);
    }
}
