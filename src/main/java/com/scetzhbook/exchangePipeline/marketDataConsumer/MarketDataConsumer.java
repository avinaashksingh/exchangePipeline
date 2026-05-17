package com.scetzhbook.exchangePipeline.marketDataConsumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.scetzhbook.exchangePipeline.logging.PipelineLogger;
import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.Trade;

@Service
public class MarketDataConsumer {

    private final MarketDataService marketDataService;
    private final PipelineLogger pipelineLogger;

    public MarketDataConsumer(MarketDataService marketDataService, PipelineLogger pipelineLogger) {
        this.marketDataService = marketDataService;
        this.pipelineLogger = pipelineLogger;
    }

    @KafkaListener(topics = "orders", groupId = "exchange-pipeline-market-data")
    public void onOrder(Order order) {
        pipelineLogger.kafkaConsumed("orders", order.getOrderId());
        marketDataService.processOrder(order);
    }

    @KafkaListener(topics = "trades", groupId = "exchange-pipeline-market-data")
    public void onTrade(Trade trade) {
        pipelineLogger.kafkaConsumed("trades", trade.getTradeId());
        marketDataService.processTrade(trade);
    }
}
