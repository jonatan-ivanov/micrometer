package io.micrometer.core.samples.richsample.zipkin;

import java.util.List;

import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

import static zipkin2.codec.Encoding.JSON;

public class SoutSender extends Sender {
    @Override
    public Encoding encoding() {
        return JSON;
    }

    @Override
    public int messageMaxBytes() {
        return 500 * 1024; //500 KiB
    }

    @Override
    public int messageSizeInBytes(List<byte[]> encodedSpans) {
        return encodedSpans.stream()
                .mapToInt(encodedSpan -> encodedSpan.length)
                .sum();
    }

    @Override
    public Call<Void> sendSpans(List<byte[]> encodedSpans) {
        encodedSpans.stream()
                .map(String::new)
                .forEach(System.out::println);

        return Call.create(null);
    }
}
