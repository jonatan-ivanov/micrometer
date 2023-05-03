/*
 * Copyright 2023 VMware, Inc.
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
package io.micrometer.registry.otlp;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.instrument.step.StepTimer;
import io.micrometer.core.instrument.util.TimeUtils;

import java.util.concurrent.TimeUnit;

class OtlpStepTimer extends StepTimer {

    private final StepMax max;

    /**
     * Create a new {@code StepTimer}.
     * @param id ID
     * @param clock clock
     * @param distributionStatisticConfig distribution statistic configuration
     * @param pauseDetector pause detector
     * @param baseTimeUnit base time unit
     * @param stepDurationMillis step in milliseconds
     */
    public OtlpStepTimer(Id id, Clock clock, DistributionStatisticConfig distributionStatisticConfig,
            PauseDetector pauseDetector, TimeUnit baseTimeUnit, long stepDurationMillis) {
        super(id, clock, distributionStatisticConfig, pauseDetector, baseTimeUnit, stepDurationMillis, OtlpMeterRegistry
            .getHistogram(clock, distributionStatisticConfig, AggregationTemporality.DELTA, stepDurationMillis));
        max = new StepMax(clock, stepDurationMillis);
    }

    @Override
    protected void recordNonNegative(final long amount, final TimeUnit unit) {
        super.recordNonNegative(amount, unit);
        // double work
        final long nanoAmount = (long) TimeUtils.convert(amount, unit, TimeUnit.NANOSECONDS);
        max.record(nanoAmount);
    }

    @Override
    public double max(final TimeUnit unit) {
        return TimeUtils.nanosToUnit(max.poll(), unit);
    }

    @Override
    public void _closingRollover() {
        super._closingRollover();
        max._closingRollover();
    }

}
