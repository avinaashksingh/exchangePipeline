package com.scetzhbook.exchangePipeline.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class MetricServiceTest {

    private MeterRegistry registry;
    private MetricService metricService;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metricService = new MetricService(registry);
    }

    @Test
    void recordLatencyUpdatesTimer() {
        metricService.recordLatency(1000L);
        Timer timer = registry.find("exchange.order.latency").timer();
        assertEquals(1, timer.count());
        assertEquals(1000.0, timer.totalTime(java.util.concurrent.TimeUnit.NANOSECONDS));
    }
}
