package com.scetzhbook.exchangePipeline.marketDataConsumer;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketPublisher {

    private final SimpMessagingTemplate template;

    public WebSocketPublisher(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void publishOrderBook(Object data) {
        template.convertAndSend("/topic/orderbook", data);
    }

    public void publishTrade(Object trade) {
        template.convertAndSend("/topic/trades", trade);
    }
}
