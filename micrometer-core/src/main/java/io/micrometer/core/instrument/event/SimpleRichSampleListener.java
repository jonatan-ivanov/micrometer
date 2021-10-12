package io.micrometer.core.instrument.event;

public interface SimpleRichSampleListener extends RichSampleListener<Void> {
    @Override
    default Void createContext() {
        return null;
    }
}
