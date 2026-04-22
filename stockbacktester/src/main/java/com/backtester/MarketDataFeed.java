package com.backtester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarketDataFeed {
    private final List<Candle> candles = new ArrayList<>();
    private int currentIndex = 0;

    public MarketDataFeed(String filepath) {
        load(filepath);
    }

    public double getCurrentPrice() {
        if (candles.isEmpty()) {
            return 0;
        }
        return candles.get(Math.min(currentIndex, candles.size() - 1)).getClose();
    }

    public List<Candle> getVisibleCandles(int points) {
        int end = Math.min(currentIndex + 1, candles.size());
        int start = Math.max(0, end - points);
        return Collections.unmodifiableList(candles.subList(start, end));
    }

    public boolean advance() {
        if (candles.isEmpty() || currentIndex >= candles.size() - 1) {
            return false;
        }
        currentIndex++;
        return true;
    }

    private void load(String filepath) {
        candles.clear();
        candles.addAll(StockDataLoader.loadCandles(filepath));
        if (candles.isEmpty()) {
            candles.add(new Candle("N/A", 0.0, 0.0, 0.0, 0.0, 0.0));
        }
        currentIndex = 0;
    }
}
