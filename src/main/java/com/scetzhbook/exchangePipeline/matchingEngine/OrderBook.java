package com.scetzhbook.exchangePipeline.matchingEngine;

import static java.lang.Long.min;
import java.util.ArrayList;
import java.util.PriorityQueue;

import com.scetzhbook.exchangePipeline.model.Order;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import org.yaml.snakeyaml.util.Tuple;

import com.scetzhbook.exchangePipeline.model.Side;
import com.scetzhbook.exchangePipeline.model.Trade;

public class OrderBook implements OrderBookInterface {

    private PriorityQueue<LinkedList<Order>> bestAsk = new PriorityQueue<>(Comparator.comparingDouble(o -> o.getFirst().price()));
    private PriorityQueue<LinkedList<Order>> bestBid = new PriorityQueue<>(Comparator.comparingDouble(o -> -o.getFirst().price()));
    private Map<String, Order> orderMap = new HashMap<>();

    private Map<Tuple<Double, Side>, Long> volumeMap = new HashMap<>();
    private Map<Tuple<Double, Side>, LinkedList<Order>> queueMap = new HashMap<>();


    @Override
    public void cancelOrder(String orderId) {
        Order order = orderMap.remove(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        Queue<Order> queue = queueMap.get(new Tuple<>(order.getPrice(), order.getSide()));
        if (queue == null) {
            throw new IllegalArgumentException("Order not found");
        }
        queue.remove(order);
        volumeMap.put(new Tuple<>(order.getPrice(), order.getSide()), volumeMap.get(new Tuple<>(order.price(), order.side())) - order.quantity());
        if (queue.isEmpty()) {
            if (order.getSide() == Side.SELL) {
                bestAsk.remove(queue);
            } else {
                bestBid.remove(queue);
            }
            queueMap.remove(new Tuple<>(order.getPrice(), order.getSide()));
        }
    }

    @Override
    public Long getVolume(Double price, Side side) {
        return volumeMap.getOrDefault(new Tuple<>(price, side), 0L);
    }

    @Override
    public void addOrderToBook(Order order, PriorityQueue<LinkedList<Order>> heap) {
        orderMap.put(order.getOrderId(), order);
        LinkedList<Order> queue = queueMap.getOrDefault(new Tuple<>(order.getPrice(), order.getSide()), new LinkedList<Order>());
        queue.add(order);
        heap.add(queue);
        queueMap.put(new Tuple<>(order.getPrice(), order.getSide()), queue);
        Long volume = volumeMap.getOrDefault(new Tuple<>(order.getPrice(), order.getSide()), 0L);
        volumeMap.put(new Tuple<>(order.getPrice(), order.getSide()), volume + order.getQuantity());
    }

    @Override
    public Optional<List<Trade>> placeOrder(Order order) {
        PriorityQueue<LinkedList<Order>> oppositeSide = order.getSide() == Side.BUY? bestAsk: bestBid;
        PriorityQueue<LinkedList<Order>> sameSide = order.getSide() == Side.BUY? bestBid: bestAsk;
        List<Trade> trades = new ArrayList<Trade>();
        orderMap.put(order.getOrderId(), order);
        while(order.getQuantity()>0 && !oppositeSide.isEmpty() && oppositeSide.peek().getFirst().getPrice() <= order.getPrice()){
            Order otherOrder = oppositeSide.peek().getFirst();
            Double tradePrice = otherOrder.getPrice();
            Long tradeQuantity = min(otherOrder.getQuantity(), order.getQuantity());
            otherOrder.setQuantity(otherOrder.getQuantity() - tradeQuantity);
            order.setQuantity(order.getQuantity() - tradeQuantity);

            if(otherOrder.getQuantity() == 0) cancelOrder(otherOrder.getOrderId());
            trades.add(new Trade(
                System.currentTimeMillis().toString()
            ));
        }
        return Optional.of(trades);

    }

}
