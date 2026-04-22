package com.backtester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PortfolioState {
    private double cashBalance;
    private final Map<String, PositionSnapshot> positions;
    private final List<OrderRecord> orderHistory;

    public PortfolioState(double initialCash) {
        this.cashBalance = initialCash;
        this.positions = new LinkedHashMap<>();
        this.orderHistory = new ArrayList<>();
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public Map<String, PositionSnapshot> getPositions() {
        return Collections.unmodifiableMap(positions);
    }

    public void upsertPosition(PositionSnapshot position) {
        positions.put(position.getSymbol(), position);
    }

    public void removePosition(String symbol) {
        positions.remove(symbol);
    }

    public List<OrderRecord> getOrderHistory() {
        return Collections.unmodifiableList(orderHistory);
    }

    public void appendOrder(OrderRecord record) {
        orderHistory.add(record);
    }
}
