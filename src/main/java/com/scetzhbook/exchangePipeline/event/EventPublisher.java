package com.scetzhbook.exchangePipeline.event;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.scetzhbook.exchangePipeline.logging.PipelineLogger;
import com.scetzhbook.exchangePipeline.model.Order;
import com.scetzhbook.exchangePipeline.model.Trade;

@Service
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PipelineLogger pipelineLogger;
    private final ExecutorService executor = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(16384),
            r -> {
                Thread t = new Thread(r, "kafka-publish");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.DiscardPolicy()
    );

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate, PipelineLogger pipelineLogger) {
        this.kafkaTemplate = kafkaTemplate;
        this.pipelineLogger = pipelineLogger;
    }

    public void publishOrderEvent(Order order, List<Trade> trades) {
        executor.execute(() -> {
            if (trades != null) {
                for (Trade trade : trades) {
                    send("order-and-trades", order.getSymbol(), trade);
                }
            }
            send("order-and-trades", order.getSymbol(), order);
        });
    }

    private void send(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, payload).get(2, TimeUnit.SECONDS);
            pipelineLogger.kafkaPublish(topic, key);
        } catch (Exception ex) {
            pipelineLogger.error("kafka publish failed topic=" + topic + " id=" + key, ex);
        }
    }
}
