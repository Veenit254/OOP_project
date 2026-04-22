package com.backtester;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PriceChartPanel extends JPanel {
    private List<Candle> candles = new ArrayList<>();
    private String title = "Price Chart";

    public PriceChartPanel() {
        setPreferredSize(new Dimension(620, 280));
        setBackground(new Color(15, 20, 34));
    }

    public void setCandles(String title, List<Candle> candles) {
        this.title = title;
        this.candles = new ArrayList<>(candles);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.drawString(title, 14, 18);
        if (candles.size() < 2) {
            return;
        }

        int left = 12;
        int right = getWidth() - 12;
        int top = 26;
        int bottom = getHeight() - 16;
        int volumeTop = top + (int) ((bottom - top) * 0.75);
        int priceBottom = volumeTop - 8;

        double min = candles.stream().mapToDouble(Candle::getLow).min().orElse(0.0);
        double max = candles.stream().mapToDouble(Candle::getHigh).max().orElse(1.0);
        double maxVolume = candles.stream().mapToDouble(Candle::getVolume).max().orElse(1.0);
        if (max - min < 0.0001) {
            max += 1;
            min -= 1;
        }

        g2.setColor(new Color(50, 66, 98));
        g2.drawRect(left, top, right - left, priceBottom - top);
        g2.drawRect(left, volumeTop, right - left, bottom - volumeTop);

        double slotWidth = (right - left) / (double) candles.size();
        int candleWidth = Math.max(5, (int) (slotWidth * 0.65));
        for (int i = 0; i < candles.size(); i++) {
            Candle c = candles.get(i);
            int centerX = left + (int) ((i + 0.5) * slotWidth);
            int x = centerX - (candleWidth / 2);

            int yHigh = scale(c.getHigh(), min, max, top, priceBottom);
            int yLow = scale(c.getLow(), min, max, top, priceBottom);
            int yOpen = scale(c.getOpen(), min, max, top, priceBottom);
            int yClose = scale(c.getClose(), min, max, top, priceBottom);

            boolean up = c.getClose() >= c.getOpen();
            Color candleColor = up ? new Color(0, 200, 120) : new Color(220, 70, 70);
            g2.setColor(candleColor);
            g2.drawLine(centerX, yHigh, centerX, yLow);

            int bodyTop = Math.min(yOpen, yClose);
            int bodyHeight = Math.max(2, Math.abs(yClose - yOpen));
            g2.fillRect(x, bodyTop, candleWidth, bodyHeight);

            int volHeight = (int) ((c.getVolume() / maxVolume) * (bottom - volumeTop - 4));
            g2.setColor(new Color(candleColor.getRed(), candleColor.getGreen(), candleColor.getBlue(), 140));
            g2.fillRect(x, bottom - volHeight, candleWidth, volHeight);
        }
    }

    private int scale(double value, double min, double max, int top, int bottom) {
        double normalized = (value - min) / (max - min);
        normalized = Math.max(0.0, Math.min(1.0, normalized));
        return bottom - (int) (normalized * (bottom - top));
    }
}
