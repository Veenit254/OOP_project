package com.backtester;

public class OrderExecutionResult {
    private final OrderStatus status;
    private final String reason;
    private final double fillPrice;

    public OrderExecutionResult(OrderStatus status, String reason, double fillPrice) {
        this.status = status;
        this.reason = reason;
        this.fillPrice = fillPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public double getFillPrice() {
        return fillPrice;
    }
}
