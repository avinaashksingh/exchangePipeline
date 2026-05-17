package com.scetzhbook.exchangePipeline.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Trade {

    private String tradeId;
    private String symbol;
    private String buyOrderId;
    private String sellOrderId;
    /** Incoming order that triggered the match; used to update the resting (maker) side in the market-data view. */
    private String takerOrderId;
    private double price;
    private long quantity;
    private long timestamp;

    public Trade() {
    }

    @JsonCreator
    public Trade(
            @JsonProperty("tradeId") String tradeId,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("buyOrderId") String buyOrderId,
            @JsonProperty("sellOrderId") String sellOrderId,
            @JsonProperty("takerOrderId") String takerOrderId,
            @JsonProperty("price") double price,
            @JsonProperty("quantity") long quantity,
            @JsonProperty("timestamp") long timestamp
    ) {
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.takerOrderId = takerOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public void setBuyOrderId(String buyOrderId) {
        this.buyOrderId = buyOrderId;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public void setSellOrderId(String sellOrderId) {
        this.sellOrderId = sellOrderId;
    }

    public String getTakerOrderId() {
        return takerOrderId;
    }

    public void setTakerOrderId(String takerOrderId) {
        this.takerOrderId = takerOrderId;
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
