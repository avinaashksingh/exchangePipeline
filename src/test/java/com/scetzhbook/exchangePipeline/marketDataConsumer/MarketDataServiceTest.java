package com.scetzhbook.exchangePipeline.marketDataConsumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.scetzhbook.exchangePipeline.logging.PipelineLogger;
import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.OrderBookView;
import com.scetzhbook.exchangePipeline.model.Side;
import com.scetzhbook.exchangePipeline.model.Trade;

class MarketDataServiceTest {

    private WebSocketPublisher webSocketPublisher;
    private OrderBookView orderBookView;
    private PipelineLogger pipelineLogger;
    private MarketDataService marketDataService;

    @BeforeEach
    void setUp() {
        webSocketPublisher = mock(WebSocketPublisher.class);
        orderBookView = mock(OrderBookView.class);
        pipelineLogger = mock(PipelineLogger.class);
        marketDataService = new MarketDataService(webSocketPublisher, orderBookView, pipelineLogger);
    }

    @Test
    void processOrderAppliesToViewAndPublishes() {
        Order order = new Order("o1", "TST", Side.BUY, 100.0, 10, 12345L);
        OrderBookView.Snapshot snapshot = new OrderBookView.Snapshot(List.of(), List.of());
        when(orderBookView.snapshot()).thenReturn(snapshot);

        marketDataService.processOrder(order);

        verify(orderBookView).apply(order);
        verify(webSocketPublisher).publishOrderBook(snapshot);
        verify(pipelineLogger).bookPublished(0, 0);
    }

    @Test
    void processTradeAppliesToViewAndPublishes() {
        Trade trade = new Trade("t1", "TST", "b1", "s1", "s1", 100.0, 10, 12345L);
        OrderBookView.Snapshot snapshot = new OrderBookView.Snapshot(List.of(), List.of());
        when(orderBookView.snapshot()).thenReturn(snapshot);

        marketDataService.processTrade(trade);

        verify(orderBookView).apply(trade);
        verify(webSocketPublisher).publishTrade(trade);
        verify(webSocketPublisher).publishOrderBook(snapshot);
        verify(pipelineLogger).tradePublished("t1", 100.0, 10L);
    }
}
