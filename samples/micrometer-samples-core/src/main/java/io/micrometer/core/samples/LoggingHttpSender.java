package io.micrometer.core.samples;

import java.util.function.Consumer;

import io.micrometer.core.ipc.http.HttpSender;

public class LoggingHttpSender implements HttpSender {
    private static final Response RESPONSE = new Response(200, null);
    private final Consumer<String> consumer;

    public LoggingHttpSender(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Response send(Request request) {
        String msg = String.format("%s %s", request.getMethod(), request.getEntity() != null ? new String(request.getEntity()) : "null");
        consumer.accept(msg);
        return RESPONSE;
    }
}
