package com.scetzhbook.exchangePipeline.matchingEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.Side;

class OrderBookTest {

    private OrderBook book;

    @BeforeEach
    void setUp() {
        book = new OrderBook();
    }

    @Test
    void buyCrossingBestAskProducesTrade() {
        book.placeOrder(order("s1", Side.SELL, 100.0, 10));
        var trades = book.placeOrder(order("b1", Side.BUY, 104.0, 5)).orElse(List.of());

        assertEquals(1, trades.size());
        assertEquals(100.0, trades.getFirst().getPrice());
        assertEquals(5L, trades.getFirst().getQuantity());
        assertEquals(5L, book.getVolume(100.0, Side.SELL));
        assertEquals(0L, book.getVolume(104.0, Side.BUY));
    }

    @Test
    void sellCrossingBestBidProducesTrade() {
        book.placeOrder(order("b1", Side.BUY, 104.0, 10));
        var trades = book.placeOrder(order("s1", Side.SELL, 100.0, 4)).orElse(List.of());

        assertEquals(1, trades.size());
        assertEquals(104.0, trades.getFirst().getPrice());
        assertEquals(4L, trades.getFirst().getQuantity());
        assertEquals(6L, book.getVolume(104.0, Side.BUY));
    }

    @Test
    void multipleRestingOrdersAtSamePrice() {
        book.placeOrder(order("s1", Side.SELL, 100.0, 10));
        book.placeOrder(order("s2", Side.SELL, 100.0, 10));
        
        var trades = book.placeOrder(order("b1", Side.BUY, 100.0, 15)).orElse(List.of());
        
        assertEquals(2, trades.size());
        assertEquals(10L, trades.get(0).getQuantity());
        assertEquals(5L, trades.get(1).getQuantity());
        assertEquals(5L, book.getVolume(100.0, Side.SELL));
    }

    @Test
    void cancelOrderReducesVolume() {
        book.placeOrder(order("b1", Side.BUY, 100.0, 10));
        assertEquals(10L, book.getVolume(100.0, Side.BUY));
        
        book.cancelOrder("b1");
        assertEquals(0L, book.getVolume(100.0, Side.BUY));
    }

    @Test
    void partialFillLeavesRestingOrder() {
        book.placeOrder(order("s1", Side.SELL, 100.0, 10));
        var trades = book.placeOrder(order("b1", Side.BUY, 100.0, 15)).orElse(List.of());
        
        assertEquals(1, trades.size());
        assertEquals(5L, book.getVolume(100.0, Side.BUY));
        assertEquals(0L, book.getVolume(100.0, Side.SELL));
    }

    private static Order order(String id, Side side, double price, long qty) {
        return new Order(id, "TST", side, price, qty, System.currentTimeMillis());
    }
}
