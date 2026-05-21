package com.scetzhbook.exchangePipeline.marketDataConsumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.scetzhbook.exchangePipeline.logging.PipelineLogger;
import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.Side;
import com.scetzhbook.exchangePipeline.model.Trade;

class MarketDataConsumerTest {

    private MarketDataService marketDataService;
    private PipelineLogger pipelineLogger;
    private MarketDataConsumer marketDataConsumer;

    @BeforeEach
    void setUp() {
        marketDataService = mock(MarketDataService.class);
        pipelineLogger = mock(PipelineLogger.class);
        marketDataConsumer = new MarketDataConsumer(marketDataService, pipelineLogger);
    }

    @Test
    void onOrderCallsService() {
        Order order = new Order("o1", "TST", Side.BUY, 100.0, 10, 12345L);
        marketDataConsumer.onOrder(order);
        verify(marketDataService).processOrder(order);
        verify(pipelineLogger).kafkaConsumed("order-and-trades", "o1");
    }

    @Test
    void onTradeCallsService() {
        Trade trade = new Trade("t1", "TST", "b1", "s1", "s1", 100.0, 10, 12345L);
        marketDataConsumer.onTrade(trade);
        verify(marketDataService).processTrade(trade);
        verify(pipelineLogger).kafkaConsumed("order-and-trades", "t1");
    }
}
