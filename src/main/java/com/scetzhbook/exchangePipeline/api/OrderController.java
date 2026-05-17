package com.scetzhbook.exchangePipeline.api;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scetzhbook.exchangePipeline.event.EventPublisher;
import com.scetzhbook.exchangePipeline.logging.PipelineLogger;
import com.scetzhbook.exchangePipeline.matchingEngine.OrderBook;
import com.scetzhbook.exchangePipeline.metrics.MetricService;
import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.OrderRequest;
import com.scetzhbook.exchangePipeline.model.Trade;
import com.scetzhbook.exchangePipeline.util.OrderIdGenerator;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final OrderIdGenerator ORDER_ID_GENERATOR = new OrderIdGenerator();

    @Autowired
    private OrderBook book;
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    private MetricService metricService;
    @Autowired
    private PipelineLogger pipelineLogger;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request) {
        long start = System.nanoTime();
        long timestamp = System.currentTimeMillis();
        String orderId = ORDER_ID_GENERATOR.next(
                request.getSymbol(),
                request.getSide(),
                request.getPrice(),
                request.getQuantity(),
                timestamp
        );
        Order order = new Order(
                orderId, request.getSymbol(),
                request.getSide(),
                request.getPrice(),
                request.getQuantity(),
                timestamp
        );
        Optional<List<Trade>> trades = book.placeOrder(order);
        List<Trade> tradeList = trades.orElse(List.of());
        int tradeCount = tradeList.size();
        metricService.recordLatency(System.nanoTime() - start);
        eventPublisher.publishOrderEvent(order, tradeList.isEmpty() ? null : tradeList);
        pipelineLogger.orderAccepted(orderId, order.getSymbol(), order.getSide(), order.getPrice(), order.getQuantity(), tradeCount);
        return ResponseEntity.ok(trades);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable String id, String symbol) {
        long start = System.nanoTime();
        try {
            book.cancelOrder(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Order not found");

        }

        metricService.recordLatency(System.nanoTime() - start);

        return ResponseEntity.ok("Order Cancelled");
    }
}
