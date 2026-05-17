package com.scetzhbook.exchangePipeline.model;

import static java.lang.Long.min;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.stereotype.Component;
@Component
public class OrderBookView {

    private final PriorityQueue<LinkedList<Order>> bestAsk =
            new PriorityQueue<>(Comparator.comparingDouble(o -> o.getFirst().getPrice()));
    private final PriorityQueue<LinkedList<Order>> bestBid =
            new PriorityQueue<>(Comparator.comparingDouble(o -> -o.getFirst().getPrice()));
    private final Map<String, Order> orderMap = new HashMap<>();
    private final Map<BookLevelKey, Long> volumeMap = new HashMap<>();
    private final Map<BookLevelKey, LinkedList<Order>> queueMap = new HashMap<>();

    public void apply(Object event) {
        if (event instanceof Order order) {
            applyOrder(order);
        } else if (event instanceof Trade trade) {
            applyTrade(trade);
        }
    }

    private void applyOrder(Order order) {
        if (order.getQuantity() <= 0) {
            return;
        }

        PriorityQueue<LinkedList<Order>> oppositeSide = order.getSide() == Side.BUY ? bestAsk : bestBid;
        PriorityQueue<LinkedList<Order>> sameSide = order.getSide() == Side.BUY ? bestBid : bestAsk;

        while (order.getQuantity() > 0 && !oppositeSide.isEmpty()) {
            LinkedList<Order> queue = oppositeSide.peek();
            if (queue.isEmpty()) {
                oppositeSide.poll();
                continue;
            }
            Order resting = queue.getFirst();
            if ((order.getSide() == Side.BUY && resting.getPrice() > order.getPrice())
                    || (order.getSide() == Side.SELL && resting.getPrice() < order.getPrice())) {
                break;
            }

            long tradeQuantity = min(resting.getQuantity(), order.getQuantity());
            resting.setQuantity(resting.getQuantity() - tradeQuantity);
            order.setQuantity(order.getQuantity() - tradeQuantity);

            BookLevelKey key = new BookLevelKey(resting.getPrice(), resting.getSide());
            volumeMap.put(key, Math.max(0, volumeMap.getOrDefault(key, 0L) - tradeQuantity));

            if (resting.getQuantity() == 0) {
                queue.removeFirst();
                orderMap.remove(resting.getOrderId());
                if (queue.isEmpty()) {
                    oppositeSide.poll();
                    queueMap.remove(key);
                }
            }
        }

        if (order.getQuantity() > 0) {
            addOrderToBook(order, sameSide);
        }
    }

    /**
     * Applies fills to resting (maker) orders. The taker is updated via {@link #applyOrder};
     * fully filled takers are published with quantity 0 and never reach {@link #applyOrder}.
     */
    private void applyTrade(Trade trade) {
        for (String orderId : List.of(trade.getBuyOrderId(), trade.getSellOrderId())) {
            if (orderId.equals(trade.getTakerOrderId())) {
                continue;
            }
            Order order = orderMap.get(orderId);
            if (order == null) {
                continue;
            }
            long tradeQuantity = trade.getQuantity();
            BookLevelKey key = new BookLevelKey(order.getPrice(), order.getSide());
            volumeMap.put(key, Math.max(0, volumeMap.getOrDefault(key, 0L) - tradeQuantity));
            order.setQuantity(order.getQuantity() - tradeQuantity);
            if (order.getQuantity() == 0) {
                removeFromBook(order);
            }
        }
    }

    public Snapshot snapshot() {
        List<PriceLevel> bids = new ArrayList<>();
        List<PriceLevel> asks = new ArrayList<>();
        for (Map.Entry<BookLevelKey, Long> entry : volumeMap.entrySet()) {
            BookLevelKey key = entry.getKey();
            long volume = entry.getValue();
            if (volume <= 0) {
                continue;
            }
            if (key.side() == Side.BUY) {
                bids.add(new PriceLevel(key.price(), volume));
            } else {
                asks.add(new PriceLevel(key.price(), volume));
            }
        }
        bids.sort(Comparator.comparingDouble(PriceLevel::price).reversed());
        asks.sort(Comparator.comparingDouble(PriceLevel::price));
        return new Snapshot(bids, asks);
    }

    private void removeFromBook(Order order) {
        orderMap.remove(order.getOrderId());
        BookLevelKey key = new BookLevelKey(order.getPrice(), order.getSide());
        LinkedList<Order> queue = queueMap.get(key);
        if (queue == null) {
            return;
        }
        queue.remove(order);
        if (queue.isEmpty()) {
            PriorityQueue<LinkedList<Order>> heap = order.getSide() == Side.SELL ? bestAsk : bestBid;
            heap.remove(queue);
            queueMap.remove(key);
        }
    }

    private void addOrderToBook(Order order, PriorityQueue<LinkedList<Order>> heap) {
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

    public record PriceLevel(double price, long volume) {}

    public record Snapshot(List<PriceLevel> bids, List<PriceLevel> asks) {}
}
