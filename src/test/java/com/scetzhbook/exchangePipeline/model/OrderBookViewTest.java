package com.scetzhbook.exchangePipeline.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderBookViewTest {

    private OrderBookView view;

    @BeforeEach
    void setUp() {
        view = new OrderBookView();
    }

    @Test
    void fullyFilledTakerUpdatesRestingBid() {
        view.apply(order("b1", Side.BUY, 104.0, 10));
        view.apply(trade("t1", "b1", "s1", "s1", 104.0, 10));
        view.apply(order("s1", Side.SELL, 100.0, 0));

        var snapshot = view.snapshot();
        assertTrue(snapshot.bids().isEmpty(), "bid at 104 should be consumed");
        assertTrue(snapshot.asks().isEmpty());
    }

    @Test
    void buyCrossingAskClearsCrossedBook() {
        view.apply(order("s1", Side.SELL, 100.0, 10));
        view.apply(order("b1", Side.BUY, 104.0, 5));

        var snapshot = view.snapshot();
        assertTrue(snapshot.bids().isEmpty());
        assertEquals(1, snapshot.asks().size());
        assertEquals(5L, snapshot.asks().getFirst().volume());
        assertEquals(100.0, snapshot.asks().getFirst().price());
    }

    @Test
    void partialTradeUpdatesVolume() {
        view.apply(order("b1", Side.BUY, 100.0, 10));
        view.apply(trade("t1", "b1", "s1", "s1", 100.0, 4));
        
        var snapshot = view.snapshot();
        assertEquals(1, snapshot.bids().size());
        assertEquals(6L, snapshot.bids().getFirst().volume());
    }

    @Test
    void multipleLevelsInSnapshot() {
        view.apply(order("b1", Side.BUY, 100.0, 10));
        view.apply(order("b2", Side.BUY, 99.0, 20));
        view.apply(order("s1", Side.SELL, 101.0, 5));
        
        var snapshot = view.snapshot();
        assertEquals(2, snapshot.bids().size());
        assertEquals(1, snapshot.asks().size());
        assertEquals(100.0, snapshot.bids().get(0).price());
        assertEquals(99.0, snapshot.bids().get(1).price());
    }

    private static Order order(String id, Side side, double price, long qty) {
        return new Order(id, "TST", side, price, qty, System.currentTimeMillis());
    }

    private static Trade trade(
            String tradeId, String buyId, String sellId, String takerId, double price, long qty
    ) {
        return new Trade(tradeId, "TST", buyId, sellId, takerId, price, qty, System.currentTimeMillis());
    }
}
