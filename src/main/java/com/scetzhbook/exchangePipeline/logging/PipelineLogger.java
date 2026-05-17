package com.scetzhbook.exchangePipeline.logging;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.scetzhbook.exchangePipeline.model.Side;

/**
 * Offloads log I/O to a background thread so the matching / Kafka / WebSocket
 * hot path never blocks on console appenders. Drops messages if the queue fills.
 */
@Component
public class PipelineLogger {

    private static final Logger log = LoggerFactory.getLogger("pipeline");

    private final ExecutorService executor = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(8192),
            r -> {
                Thread t = new Thread(r, "pipeline-log");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.DiscardPolicy()
    );

    public void orderAccepted(String orderId, String symbol, Side side, double price, long quantity, int tradeCount) {
        submit(() -> log.info(
                "order accepted id={} symbol={} side={} price={} qty={} trades={}",
                orderId, symbol, side, price, quantity, tradeCount
        ));
    }

    public void kafkaConsumed(String topic, String eventId) {
        submit(() -> log.debug("kafka consumed topic={} id={}", topic, eventId));
    }

    public void tradePublished(String tradeId, double price, long quantity) {
        submit(() -> log.info("trade published id={} price={} qty={}", tradeId, price, quantity));
    }

    public void bookPublished(int bidLevels, int askLevels) {
        submit(() -> log.debug("orderbook published bids={} asks={}", bidLevels, askLevels));
    }

    public void kafkaPublish(String topic, String eventId) {
        submit(() -> log.debug("kafka published topic={} id={}", topic, eventId));
    }

    public void error(String message, Throwable cause) {
        submit(() -> log.error(message, cause));
    }

    private void submit(Runnable task) {
        executor.execute(task);
    }
}
