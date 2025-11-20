/*
 * Copyright 2025 VMware, Inc.
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

package io.micrometer.benchmark.core;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class CompositeBenchmark {

    private SimpleMeterRegistry simpleMeterRegistry;

    private CompositeMeterRegistry composite1;

    private CompositeMeterRegistry composite2;

    private Counter counter1;

    private Counter counter2;

    @Setup
    public void setup() {
        simpleMeterRegistry = new SimpleMeterRegistry();

        composite1 = new CompositeMeterRegistry().add(simpleMeterRegistry);
        counter1 = composite1.counter("counter1");
        counter1.increment();

        composite2 = new CompositeMeterRegistry();
        counter2 = composite2.counter("counter2");
        counter2.increment();

        /*
         * If you comment out the for loop and the line that adds a registry to composite2
         * (having one non-empty and one empty composites), the multiComposite benchmark
         * below DOES reproduce the issue.
         *
         * If you comment out the for loop but leave the line that adds a registry to
         * composite2 (having two non-empty composites and meter registration is before
         * adding a registry to composite2) the multiComposite benchmark below DOES NOT
         * reproduce the issue.
         *
         * If you leave everything as-is (having two non-empty composites, meter
         * registration is before adding the registry to composite2, and there are
         * "enough" recordings) the multiComposite benchmark below DOES reproduce the
         * issue.
         *
         * If you lower the limiting condition in the for loop (e.g.: i < 200) (having two
         * non-empty composites, meter registration is before adding the registry to
         * composite2, and there are not "enough" recordings) the multiComposite benchmark
         * below DOES NOT reproduce the issue.
         *
         * The amount of recordings needed before adding the registry might be different
         * for you. I haven't checked compiler logs, right now I don't know why this is
         * happening (passing a limit in the execution profile, C1 kicks in early and
         * makes a decision for later tiers, etc.).
         */
        for (int i = 0; i < 250; i++) {
            counter1.increment();
            counter2.increment();
        }
        composite2.add(new SimpleMeterRegistry());

        System.out.println("Meters at setup:\n" + simpleMeterRegistry.getMetersAsString());
    }

    @TearDown
    public void tearDown() {
        System.out.println("\nMeters at tearDown:\n" + simpleMeterRegistry.getMetersAsString());
    }

    // This is only useful if you test with an empty composite
    // or don't hit the "recording limit" (see above), otherwise it also
    // see allocations like multiComposite benchmark.
    // @Benchmark
    public void baseline() {
        counter1.increment();
    }

    @Benchmark
    public void multiComposite() {
        counter1.increment();
        counter2.increment();
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(CompositeBenchmark.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .build()).run();
    }

}
