package com.scetzhbook.exchangePipeline.marketDataConsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MarketDataConsumer {
    
    @Autowired
    private final MarketDataService marketDataService;

    public MarketDataConsumer(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @KafkaListener(topics = {"orders", "trades"})
    public void consume(Object event) {
        marketDataService.process(event);
    }
}