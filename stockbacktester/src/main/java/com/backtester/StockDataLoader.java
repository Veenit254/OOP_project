package com.backtester;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class StockDataLoader {
    public static List<Candle> loadCandles(String filepath) {
        List<Candle> candles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 6) {
                    continue;
                }
                candles.add(new Candle(
                    values[0],
                    Double.parseDouble(values[1]),
                    Double.parseDouble(values[2]),
                    Double.parseDouble(values[3]),
                    Double.parseDouble(values[4]),
                    Double.parseDouble(values[5])
                ));
            }
        } catch (Exception ignored) {
            candles.clear();
        }
        return candles;
    }
}
