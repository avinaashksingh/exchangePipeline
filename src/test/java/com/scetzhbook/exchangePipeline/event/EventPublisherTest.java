package com.scetzhbook.exchangePipeline.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.scetzhbook.exchangePipeline.logging.PipelineLogger;
import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.Side;
import com.scetzhbook.exchangePipeline.model.Trade;

class EventPublisherTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private PipelineLogger pipelineLogger;
    private EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        pipelineLogger = mock(PipelineLogger.class);
        eventPublisher = new EventPublisher(kafkaTemplate, pipelineLogger);
    }

    @Test
    void publishOrderEventSendsToKafka() throws Exception {
        Order order = new Order("o1", "TST", Side.BUY, 100.0, 10, 12345L);
        Trade trade = new Trade("t1", "TST", "o1", "s1", "o1", 100.0, 5, 12346L);
        
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.complete(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        eventPublisher.publishOrderEvent(order, List.of(trade));

        // Use timeout because it's executed in a background thread
        verify(kafkaTemplate, timeout(1000)).send(eq("order-and-trades"), eq("TST"), eq(order));
        verify(kafkaTemplate, timeout(1000)).send(eq("order-and-trades"), eq("TST"), eq(trade));
    }
}
