/*
 * Copyright 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.observability.event.listener.composite;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.observability.event.listener.RecordingListener;

import io.micrometer.core.instrument.Timer.Sample;

/**
 * Using this {@link RecordingListener} implementation, you can register multiple
 * listeners but only the first matching one will be applied.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class FirstMatchingCompositeRecordingListener implements CompositeRecordingListener {

	private final List<? extends RecordingListener<CompositeContext>> listeners;

	/**
	 * @param listeners The listeners that are registered under the composite.
	 */
	public FirstMatchingCompositeRecordingListener(RecordingListener<CompositeContext>... listeners) {
		this(Arrays.asList(listeners));
	}

	/**
	 * @param listeners The listeners that are registered under the composite.
	 */
	public FirstMatchingCompositeRecordingListener(List<? extends RecordingListener<CompositeContext>> listeners) {
		this.listeners = listeners;
	}

	@Override
	public void onStart(Sample intervalRecording) {
		getFirstApplicableListener(intervalRecording)
				.ifPresent(listener -> listener.onStart(new IntervalRecordingView(listener, intervalRecording)));
	}

	@Override
	public void onStop(Sample intervalRecording) {
		getFirstApplicableListener(intervalRecording)
				.ifPresent(listener -> listener.onStop(new IntervalRecordingView(listener, intervalRecording)));
	}

	private Optional<? extends RecordingListener<CompositeContext>> getFirstApplicableListener(Sample sample) {
		return this.listeners.stream().filter(listener -> listener.isApplicable(sample)).findFirst();
	}

	@Override
	public void onError(Sample intervalRecording) {
		getFirstApplicableListener(intervalRecording)
				.ifPresent(listener -> listener.onError(new IntervalRecordingView(listener, intervalRecording)));
	}

	@Override
	public CompositeContext createContext() {
		return new CompositeContext(this.listeners);
	}

	@Override
	public List<? extends RecordingListener<CompositeContext>> getListeners() {
		return this.listeners;
	}

}
