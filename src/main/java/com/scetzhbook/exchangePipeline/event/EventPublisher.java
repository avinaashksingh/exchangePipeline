package com.scetzhbook.exchangePipeline.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.Trade;

@Service
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderEvent(Order order) {
        kafkaTemplate.send("orders", order);
    }

    public void publishTradeEvent(Trade trade) {
        kafkaTemplate.send("trades", trade);
    }
}
