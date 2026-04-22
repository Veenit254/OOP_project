package com.backtester;

import java.time.Instant;

public class ManualOrder {
    private final String symbol;
    private final OrderType orderType;
    private final OrderSide side;
    private final int quantity;
    private final Double triggerPrice;
    private final Instant createdAt;

    public ManualOrder(String symbol, OrderType orderType, OrderSide side, int quantity, Double triggerPrice) {
        this.symbol = symbol;
        this.orderType = orderType;
        this.side = side;
        this.quantity = quantity;
        this.triggerPrice = triggerPrice;
        this.createdAt = Instant.now();
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderSide getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public Double getTriggerPrice() {
        return triggerPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
