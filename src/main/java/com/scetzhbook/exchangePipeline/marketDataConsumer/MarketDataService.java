package com.scetzhbook.exchangePipeline.marketDataConsumer;

import org.springframework.stereotype.Service;

import com.scetzhbook.exchangePipeline.model.OrderBookView;

@Service
public class MarketDataService {

    private final WebSocketPublisher webSocketPublisher;
    private final OrderBookView orderBookView;

    public MarketDataService(WebSocketPublisher webSocketPublisher, OrderBookView orderBookView) {
        this.webSocketPublisher = webSocketPublisher;
        this.orderBookView = orderBookView;
    }

    public void process(Object event) {
        OrderBookView.Snapshot view = buildView(event);
        webSocketPublisher.publish(view);
    }

    private OrderBookView.Snapshot buildView(Object event) {
        orderBookView.apply(event);
        return orderBookView.snapshot();
    }
}
