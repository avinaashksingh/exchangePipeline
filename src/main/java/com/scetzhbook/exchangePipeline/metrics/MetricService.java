package com.scetzhbook.exchangePipeline.metrics;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class MetricService {

    private final Timer orderLatencyTimer;

    public MetricService(MeterRegistry registry) {
        this.orderLatencyTimer = Timer.builder("exchange.order.latency")
                .description("Latency of order book operations (place, cancel)")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public void recordLatency(long nanos) {
        orderLatencyTimer.record(nanos, TimeUnit.NANOSECONDS);
    }
}
