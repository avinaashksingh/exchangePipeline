package com.scetzhbook.exchangePipeline.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.util.Tuple;

@Component
public class OrderBookView {

    private PriorityQueue<LinkedList<Order>> bestAsk = new PriorityQueue<>(Comparator.comparingDouble(o -> o.getFirst().getPrice()));
    private PriorityQueue<LinkedList<Order>> bestBid = new PriorityQueue<>(Comparator.comparingDouble(o -> -o.getFirst().getPrice()));
    private Map<String, Order> orderMap = new HashMap<>();

    private Map<Tuple<Double, Side>, Long> volumeMap = new HashMap<>();
    private Map<Tuple<Double, Side>, LinkedList<Order>> queueMap = new HashMap<>();

    public void apply(Object event) {
        if (event instanceof Order order) {
            applyOrder(order);
        } else if (event instanceof Trade trade) {
            applyTrade(trade);
        }
    }

    private void applyOrder(Order order) {
        if (order.getQuantity() > 0) {
            PriorityQueue<LinkedList<Order>> sameSide = order.getSide() == Side.BUY ? bestBid : bestAsk;
            addOrderToBook(order, sameSide);
        }
    }

    private void applyTrade(Trade trade) {
        for (String orderId : List.of(trade.getBuyOrderId(), trade.getSellOrderId())) {
            Order order = orderMap.get(orderId);
            if (order == null) {
                continue;
            }
            long tradeQuantity = trade.getQuantity();
            order.setQuantity(order.getQuantity() - tradeQuantity);
            if (order.getQuantity() == 0) {
                cancelOrder(order.getOrderId());
            }
        }
    }

    public Snapshot snapshot() {
        List<PriceLevel> bids = new ArrayList<>();
        List<PriceLevel> asks = new ArrayList<>();
        for (Map.Entry<Tuple<Double, Side>, Long> entry : volumeMap.entrySet()) {
            Tuple<Double, Side> key = entry.getKey();
            long volume = entry.getValue();
            if (volume <= 0) {
                continue;
            }
            if (key._2() == Side.BUY) {
                bids.add(new PriceLevel(key._1(), volume));
            } else {
                asks.add(new PriceLevel(key._1(), volume));
            }
        }
        bids.sort(Comparator.comparingDouble(PriceLevel::price).reversed());
        asks.sort(Comparator.comparingDouble(PriceLevel::price));
        return new Snapshot(bids, asks);
    }

    private void cancelOrder(String orderId) {
        Order order = orderMap.remove(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        Queue<Order> queue = queueMap.get(new Tuple<>(order.getPrice(), order.getSide()));
        if (queue == null) {
            throw new IllegalArgumentException("Order not found");
        }
        queue.remove(order);
        volumeMap.put(new Tuple<>(order.getPrice(), order.getSide()), volumeMap.get(new Tuple<>(order.getPrice(), order.getSide())) - order.getQuantity());
        if (queue.isEmpty()) {
            if (order.getSide() == Side.SELL) {
                bestAsk.remove(queue);
            } else {
                bestBid.remove(queue);
            }
            queueMap.remove(new Tuple<>(order.getPrice(), order.getSide()));
        }
    }

    private Long getVolume(Double price, Side side) {
        return volumeMap.getOrDefault(new Tuple<>(price, side), 0L);
    }

    private void addOrderToBook(Order order, PriorityQueue<LinkedList<Order>> heap) {
        orderMap.put(order.getOrderId(), order);
        LinkedList<Order> queue = queueMap.getOrDefault(new Tuple<>(order.getPrice(), order.getSide()), new LinkedList<Order>());
        queue.add(order);
        heap.add(queue);
        queueMap.put(new Tuple<>(order.getPrice(), order.getSide()), queue);
        Long volume = volumeMap.getOrDefault(new Tuple<>(order.getPrice(), order.getSide()), 0L);
        volumeMap.put(new Tuple<>(order.getPrice(), order.getSide()), volume + order.getQuantity());
    }

    public record PriceLevel(double price, long volume) {}

    public record Snapshot(List<PriceLevel> bids, List<PriceLevel> asks) {}
}
