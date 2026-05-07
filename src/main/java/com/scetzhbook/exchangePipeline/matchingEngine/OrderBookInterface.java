package com.scetzhbook.exchangePipeline.matchingEngine;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.Side;
import com.scetzhbook.exchangePipeline.model.Trade;

public interface OrderBookInterface {

    void addOrderToBook(Order order, PriorityQueue<LinkedList<Order>> heap);
    Optional<List<Trade>> placeOrder(Order order);
    void cancelOrder(String orderId);
    Long getVolume(Double price, Side side);
}
