package com.backtester;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ManualTradingService {
    private static class MutablePosition {
        int quantity;
        double avgEntry;
    }

    private final PortfolioState portfolioState;
    private final Map<String, MutablePosition> positions = new HashMap<>();

    public ManualTradingService(double initialCash) {
        this.portfolioState = new PortfolioState(initialCash);
    }

    public synchronized PortfolioState getPortfolioState() {
        return portfolioState;
    }

    public synchronized OrderExecutionResult submitOrder(ManualOrder order, double marketPrice) {
        double requestedPrice = order.getTriggerPrice() == null ? marketPrice : order.getTriggerPrice();
        OrderExecutionResult result = validateAndExecute(order, marketPrice);
        portfolioState.appendOrder(new OrderRecord(
            Instant.now(),
            order.getSymbol(),
            order.getSide(),
            order.getOrderType(),
            order.getQuantity(),
            requestedPrice,
            result.getFillPrice(),
            result.getStatus(),
            result.getReason()
        ));
        return result;
    }

    public synchronized OrderExecutionResult closePosition(String symbol, double marketPrice) {
        MutablePosition pos = positions.get(symbol);
        if (pos == null || pos.quantity <= 0) {
            return new OrderExecutionResult(OrderStatus.REJECTED, "No open position to close.", marketPrice);
        }
        ManualOrder closeOrder = new ManualOrder(symbol, OrderType.MARKET, OrderSide.SELL, pos.quantity, marketPrice);
        return submitOrder(closeOrder, marketPrice);
    }

    public synchronized void refreshMark(String symbol, double marketPrice) {
        MutablePosition pos = positions.get(symbol);
        if (pos == null || pos.quantity <= 0) {
            portfolioState.removePosition(symbol);
            return;
        }
        portfolioState.upsertPosition(new PositionSnapshot(symbol, pos.quantity, pos.avgEntry, marketPrice));
    }

    private OrderExecutionResult validateAndExecute(ManualOrder order, double marketPrice) {
        if (order.getQuantity() <= 0) {
            return new OrderExecutionResult(OrderStatus.REJECTED, "Quantity must be greater than zero.", marketPrice);
        }

        Double triggerPrice = order.getTriggerPrice();
        if ((order.getOrderType() == OrderType.LIMIT || order.getOrderType() == OrderType.STOP)
            && (triggerPrice == null || triggerPrice <= 0)) {
            return new OrderExecutionResult(OrderStatus.REJECTED, "Price is required for limit/stop orders.", marketPrice);
        }

        boolean executable = switch (order.getOrderType()) {
            case MARKET -> true;
            case LIMIT -> order.getSide() == OrderSide.BUY
                ? marketPrice <= triggerPrice
                : marketPrice >= triggerPrice;
            case STOP -> order.getSide() == OrderSide.BUY
                ? marketPrice >= triggerPrice
                : marketPrice <= triggerPrice;
        };

        if (!executable) {
            return new OrderExecutionResult(OrderStatus.REJECTED, "Order conditions not met at current market price.", marketPrice);
        }

        if (order.getSide() == OrderSide.BUY) {
            double cost = marketPrice * order.getQuantity();
            if (portfolioState.getCashBalance() < cost) {
                return new OrderExecutionResult(OrderStatus.REJECTED, "Insufficient funds for this manual order.", marketPrice);
            }

            portfolioState.setCashBalance(portfolioState.getCashBalance() - cost);
            MutablePosition current = positions.computeIfAbsent(order.getSymbol(), ignored -> new MutablePosition());
            double weightedValue = (current.avgEntry * current.quantity) + (marketPrice * order.getQuantity());
            current.quantity += order.getQuantity();
            current.avgEntry = weightedValue / current.quantity;
            portfolioState.upsertPosition(new PositionSnapshot(order.getSymbol(), current.quantity, current.avgEntry, marketPrice));
            return new OrderExecutionResult(OrderStatus.FILLED, "Order filled.", marketPrice);
        }

        MutablePosition current = positions.get(order.getSymbol());
        if (current == null || current.quantity < order.getQuantity()) {
            return new OrderExecutionResult(OrderStatus.REJECTED, "Not enough shares to sell.", marketPrice);
        }

        double proceeds = marketPrice * order.getQuantity();
        portfolioState.setCashBalance(portfolioState.getCashBalance() + proceeds);
        current.quantity -= order.getQuantity();
        if (current.quantity == 0) {
            positions.remove(order.getSymbol());
            portfolioState.removePosition(order.getSymbol());
        } else {
            portfolioState.upsertPosition(new PositionSnapshot(order.getSymbol(), current.quantity, current.avgEntry, marketPrice));
        }
        return new OrderExecutionResult(OrderStatus.FILLED, "Order filled.", marketPrice);
    }
}
