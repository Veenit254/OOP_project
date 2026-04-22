package com.backtester;

public class PositionSnapshot {
    private final String symbol;
    private final int quantity;
    private final double averageEntryPrice;
    private final double markPrice;
    private final double pnl;

    public PositionSnapshot(String symbol, int quantity, double averageEntryPrice, double markPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageEntryPrice = averageEntryPrice;
        this.markPrice = markPrice;
        this.pnl = (markPrice - averageEntryPrice) * quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAverageEntryPrice() {
        return averageEntryPrice;
    }

    public double getMarkPrice() {
        return markPrice;
    }

    public double getPnl() {
        return pnl;
    }
}
