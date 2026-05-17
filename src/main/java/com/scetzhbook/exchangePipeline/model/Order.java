package com.scetzhbook.exchangePipeline.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {
    private String orderId;
    private String symbol;
    private Side side;
    private double price;
    private long quantity;
    private long timestamp;

    public Order() {
    }

    @JsonCreator
    public Order(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("side") Side side,
            @JsonProperty("price") double price,
            @JsonProperty("quantity") long quantity,
            @JsonProperty("timestamp") long timestamp
    ) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
