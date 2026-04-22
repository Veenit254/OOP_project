package com.backtester;

import java.time.Instant;

public class OrderRecord {
    private final Instant timestamp;
    private final String symbol;
    private final OrderSide side;
    private final OrderType orderType;
    private final int quantity;
    private final double requestedPrice;
    private final double fillPrice;
    private final OrderStatus status;
    private final String reason;

    public OrderRecord(
        Instant timestamp,
        String symbol,
        OrderSide side,
        OrderType orderType,
        int quantity,
        double requestedPrice,
        double fillPrice,
        OrderStatus status,
        String reason
    ) {
        this.timestamp = timestamp;
        this.symbol = symbol;
        this.side = side;
        this.orderType = orderType;
        this.quantity = quantity;
        this.requestedPrice = requestedPrice;
        this.fillPrice = fillPrice;
        this.status = status;
        this.reason = reason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getRequestedPrice() {
        return requestedPrice;
    }

    public double getFillPrice() {
        return fillPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}
