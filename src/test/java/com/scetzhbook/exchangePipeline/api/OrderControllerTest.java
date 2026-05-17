package com.scetzhbook.exchangePipeline.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.scetzhbook.exchangePipeline.event.EventPublisher;
import com.scetzhbook.exchangePipeline.logging.PipelineLogger;
import com.scetzhbook.exchangePipeline.matchingEngine.OrderBook;
import com.scetzhbook.exchangePipeline.metrics.MetricService;

@SpringBootTest
class OrderControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private OrderBook book;
    @MockitoBean
    private EventPublisher eventPublisher;
    @MockitoBean
    private MetricService metricService;
    @MockitoBean
    private PipelineLogger pipelineLogger;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void placeOrderReturnsOk() throws Exception {
        when(book.placeOrder(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"symbol\":\"TST\",\"side\":\"BUY\",\"price\":100.0,\"quantity\":10}"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrderReturnsOk() throws Exception {
        mockMvc.perform(post("/orders/cancel/o1")
                .param("symbol", "TST"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrderReturnsNotFoundWhenMissing() throws Exception {
        doThrow(new IllegalArgumentException("Order not found")).when(book).cancelOrder("o1");

        mockMvc.perform(post("/orders/cancel/o1")
                .param("symbol", "TST"))
                .andExpect(status().isNotFound());
    }
}
