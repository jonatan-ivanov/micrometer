package io.micrometer.core.samples.richsample;

import java.io.IOException;

import brave.Tracing;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.event.RichSample;
import io.micrometer.core.samples.richsample.zipkin.SoutSender;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;

public class RichSampleSample {
    private static final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    private static final AsyncZipkinSpanHandler soutSpanHandler = AsyncZipkinSpanHandler.create(new SoutSender());
    private static final Tracing tracing = Tracing.newBuilder().localServiceName("example-service").addSpanHandler(soutSpanHandler).build();

    public static void main(String[] args) throws InterruptedException {
        registry.config().richSampleListener(
                new SoutRichSampleListener(),
                new SpanRichSampleListener(tracing.tracer()),
                new TimerRichSampleListener(registry)
        );

//        Timer timer = Timer.builder("test-timer")
//                .description("this is a test timer")
//                .tag("a", "b")
//                .register(registry);
//        Timer.Sample sample = Timer.start();
//        Thread.sleep(1_000);
//        sample.stop(timer);

        RichSample sample = Timer.richSample("test-sample", registry)
                .description("sample to test")
                .tag("a", "b")
                .start();

        Thread.sleep(1_000);
        Timer.richSample("nested-sample", registry).description("sample to nest").start().stop();

        sample.error(new IOException("simulated"))
                .detailedName("test-sample-for-something-with-error")
                .stop();

        System.out.println();
        System.out.println("------- METERS -------");
        System.out.println(registry.scrape());

        soutSpanHandler.close();
        registry.close();
    }
}
