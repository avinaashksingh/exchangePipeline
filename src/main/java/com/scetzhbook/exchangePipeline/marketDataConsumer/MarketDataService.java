package com.scetzhbook.exchangePipeline.marketDataConsumer;

import org.springframework.stereotype.Service;

import com.scetzhbook.exchangePipeline.logging.PipelineLogger;
import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.OrderBookView;
import com.scetzhbook.exchangePipeline.model.Trade;

@Service
public class MarketDataService {

    private final WebSocketPublisher webSocketPublisher;
    private final OrderBookView orderBookView;
    private final PipelineLogger pipelineLogger;

    public MarketDataService(
            WebSocketPublisher webSocketPublisher,
            OrderBookView orderBookView,
            PipelineLogger pipelineLogger
    ) {
        this.webSocketPublisher = webSocketPublisher;
        this.orderBookView = orderBookView;
        this.pipelineLogger = pipelineLogger;
    }

    public void processOrder(Order order) {
        orderBookView.apply(order);
        var snapshot = orderBookView.snapshot();
        webSocketPublisher.publishOrderBook(snapshot);
        pipelineLogger.bookPublished(snapshot.bids().size(), snapshot.asks().size());
    }

    public void processTrade(Trade trade) {
        orderBookView.apply(trade);
        webSocketPublisher.publishTrade(trade);
        webSocketPublisher.publishOrderBook(orderBookView.snapshot());
        pipelineLogger.tradePublished(trade.getTradeId(), trade.getPrice(), trade.getQuantity());
    }
}
