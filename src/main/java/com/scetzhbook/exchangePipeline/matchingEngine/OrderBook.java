package com.scetzhbook.exchangePipeline.matchingEngine;

import static java.lang.Long.min;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;
import com.scetzhbook.exchangePipeline.model.BookLevelKey;
import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.Side;
import com.scetzhbook.exchangePipeline.model.Trade;

@Service
public class OrderBook implements OrderBookInterface {

    private final PriorityQueue<LinkedList<Order>> bestAsk =
            new PriorityQueue<>(Comparator.comparingDouble(o -> o.getFirst().getPrice()));
    private final PriorityQueue<LinkedList<Order>> bestBid =
            new PriorityQueue<>(Comparator.comparingDouble(o -> -o.getFirst().getPrice()));
    private final Map<String, Order> orderMap = new HashMap<>();
    private final Map<BookLevelKey, Long> volumeMap = new HashMap<>();
    private final Map<BookLevelKey, LinkedList<Order>> queueMap = new HashMap<>();

    @Override
    public void cancelOrder(String orderId) {
        Order order = orderMap.remove(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        removeFromQueues(order);
    }

    @Override
    public Long getVolume(Double price, Side side) {
        return volumeMap.getOrDefault(new BookLevelKey(price, side), 0L);
    }

    @Override
    public void addOrderToBook(Order order, PriorityQueue<LinkedList<Order>> heap) {
        orderMap.put(order.getOrderId(), order);
        BookLevelKey key = new BookLevelKey(order.getPrice(), order.getSide());
        LinkedList<Order> queue = queueMap.get(key);
        boolean newLevel = queue == null;
        if (newLevel) {
            queue = new LinkedList<>();
            queueMap.put(key, queue);
        }
        queue.add(order);
        if (newLevel) {
            heap.add(queue);
        }
        volumeMap.put(key, volumeMap.getOrDefault(key, 0L) + order.getQuantity());
    }

    @Override
    public Optional<List<Trade>> placeOrder(Order order) {
        PriorityQueue<LinkedList<Order>> oppositeSide = order.getSide() == Side.BUY ? bestAsk : bestBid;
        PriorityQueue<LinkedList<Order>> sameSide = order.getSide() == Side.BUY ? bestBid : bestAsk;
        List<Trade> trades = new ArrayList<>();

        while (order.getQuantity() > 0 && !oppositeSide.isEmpty()) {
            LinkedList<Order> queue = oppositeSide.peek();
            if (queue.isEmpty()) {
                oppositeSide.poll();
                continue;
            }
            Order resting = queue.getFirst();
            if ((order.getSide() == Side.BUY && resting.getPrice() > order.getPrice()) || (order.getSide() == Side.SELL && resting.getPrice() < order.getPrice())) {
                break;
            }

            long tradeQuantity = min(resting.getQuantity(), order.getQuantity());
            double tradePrice = resting.getPrice();
            resting.setQuantity(resting.getQuantity() - tradeQuantity);
            order.setQuantity(order.getQuantity() - tradeQuantity);

            BookLevelKey key = new BookLevelKey(resting.getPrice(), resting.getSide());
            volumeMap.put(key, volumeMap.getOrDefault(key, 0L) - tradeQuantity);

            if (resting.getQuantity() == 0) {
                queue.removeFirst();
                orderMap.remove(resting.getOrderId());
                if (queue.isEmpty()) {
                    oppositeSide.poll();
                    queueMap.remove(key);
                }
            }

            long tradeTime = System.currentTimeMillis();
            String tradeId = tradeTime + "-" + order.getSymbol();
            String buyOrderId = resting.getSide() == Side.BUY ? resting.getOrderId() : order.getOrderId();
            String sellOrderId = resting.getSide() == Side.SELL ? resting.getOrderId() : order.getOrderId();
            trades.add(new Trade(
                    tradeId, order.getSymbol(), buyOrderId, sellOrderId, order.getOrderId(),
                    tradePrice, tradeQuantity, tradeTime));
        }

        if (order.getQuantity() > 0) {
            addOrderToBook(order, sameSide);
        }
        return Optional.of(trades);
    }

    private void removeFromQueues(Order order) {
        BookLevelKey key = new BookLevelKey(order.getPrice(), order.getSide());
        LinkedList<Order> queue = queueMap.get(key);
        if (queue == null) {
            return;
        }
        queue.remove(order);
        volumeMap.put(key, Math.max(0, volumeMap.getOrDefault(key, 0L) - order.getQuantity()));
        if (queue.isEmpty()) {
            PriorityQueue<LinkedList<Order>> heap = order.getSide() == Side.SELL ? bestAsk : bestBid;
            heap.remove(queue);
            queueMap.remove(key);
        }
    }
}
