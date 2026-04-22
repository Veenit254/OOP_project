package com.backtester;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class SyntheticDataGenerator {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void ensureDataFile(String ticker) {
        String filename = ticker + "_test.csv";
        File file = new File(filename);
        if (!file.exists()) {
            generateData(ticker, 500, seedPrice(ticker), seedVolatility(ticker));
        }
    }

    public static void generateData(String ticker, int days, double startPrice, double volatility) {
        String filename = ticker + "_test.csv";
        Random random = new Random();
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            out.println("Date,Open,High,Low,Close,Volume");
            double currentClose = startPrice;
            LocalDate currentDate = LocalDate.now().minusDays(days);
            int generated = 0;
            while (generated < days) {
                if (currentDate.getDayOfWeek().getValue() >= 6) {
                    currentDate = currentDate.plusDays(1);
                    continue;
                }

                double open = currentClose;
                double dailyReturn = random.nextGaussian() * volatility;
                double close = Math.max(0.01, open * (1 + dailyReturn));
                double maxOC = Math.max(open, close);
                double minOC = Math.min(open, close);
                double high = maxOC + (Math.abs(random.nextGaussian()) * volatility * open * 0.5);
                double low = Math.max(0.01, minOC - (Math.abs(random.nextGaussian()) * volatility * open * 0.5));
                long volume = 1_000_000 + (long) (random.nextDouble() * 500_000);

                out.printf("%s,%.2f,%.2f,%.2f,%.2f,%d%n",
                    currentDate.format(DATE_FMT), open, high, low, close, volume);
                currentClose = close;
                currentDate = currentDate.plusDays(1);
                generated++;
            }
        } catch (Exception ignored) {
        }
    }

    private static double seedPrice(String ticker) {
        return switch (ticker) {
            case "MSFT" -> 250.0;
            case "GOOGL" -> 100.0;
            default -> 150.0;
        };
    }

    private static double seedVolatility(String ticker) {
        return switch (ticker) {
            case "MSFT" -> 0.02;
            case "GOOGL" -> 0.035;
            default -> 0.015;
        };
    }
}
