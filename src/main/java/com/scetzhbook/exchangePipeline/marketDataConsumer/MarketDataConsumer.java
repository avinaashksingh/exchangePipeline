package com.scetzhbook.exchangePipeline.marketDataConsumer;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.scetzhbook.exchangePipeline.logging.PipelineLogger;
import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.Trade;

@Service
@KafkaListener(topics = "order-and-trades", groupId = "exchange-pipeline-market-data")
public class MarketDataConsumer {

    private final MarketDataService marketDataService;
    private final PipelineLogger pipelineLogger;

    public MarketDataConsumer(MarketDataService marketDataService, PipelineLogger pipelineLogger) {
        this.marketDataService = marketDataService;
        this.pipelineLogger = pipelineLogger;
    }

    @KafkaHandler
    public void onOrder(Order order) {
        pipelineLogger.kafkaConsumed("order-and-trades", order.getOrderId());
        marketDataService.processOrder(order);
    }

    @KafkaHandler
    public void onTrade(Trade trade) {
        pipelineLogger.kafkaConsumed("order-and-trades", trade.getTradeId());
        marketDataService.processTrade(trade);
    }

    @KafkaHandler(isDefault = true)
    public void onUnknown(Object message) {
        pipelineLogger.error("Received unknown message type from Kafka: " + 
            (message != null ? message.getClass().getName() : "null"), null);
    }
}
