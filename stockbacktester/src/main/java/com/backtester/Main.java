package com.backtester;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Main {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final ManualTradingService tradingService = new ManualTradingService(10_000);
    private MarketDataFeed marketDataFeed = new MarketDataFeed("AAPL_test.csv");

    private JLabel symbolLabel;
    private JLabel markLabel;
    private JLabel cashLabel;
    private JLabel equityLabel;
    private JTextArea toastArea;
    private JComboBox<String> symbolDropdown;
    private JComboBox<OrderType> orderTypeDropdown;
    private JSpinner quantitySpinner;
    private JTextField priceField;
    private JButton buyButton;
    private JButton sellButton;
    private DefaultTableModel positionModel;
    private DefaultTableModel historyModel;
    private PriceChartPanel chartPanel;
    private Timer marketTimer;

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new Main().showUI());
    }

    private void showUI() {
        ensureSyntheticDataFiles();
        JFrame frame = new JFrame("Manual Trading Terminal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 760);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout(12, 12));
        frame.add(buildTopBar(), BorderLayout.NORTH);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildCenterPanel(), buildBottomPanel());
        verticalSplit.setResizeWeight(0.45);
        verticalSplit.setDividerLocation(300);
        verticalSplit.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        frame.add(verticalSplit, BorderLayout.CENTER);

        startMarketStreaming();
        refreshUi();
        frame.setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));

        symbolLabel = new JLabel("Symbol: AAPL");
        markLabel = new JLabel("Price: $0.00");
        cashLabel = new JLabel("Cash: $10,000.00");
        equityLabel = new JLabel("Equity: $10,000.00");

        topBar.add(symbolLabel);
        topBar.add(markLabel);
        topBar.add(cashLabel);
        topBar.add(equityLabel);
        return topBar;
    }

    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        chartPanel = new PriceChartPanel();
        center.add(chartPanel, BorderLayout.CENTER);
        center.add(buildOrderTicket(), BorderLayout.EAST);
        return center;
    }

    private JPanel buildOrderTicket() {
        JPanel ticket = new JPanel(new GridBagLayout());
        ticket.setBorder(BorderFactory.createTitledBorder("Order Ticket"));
        ticket.setPreferredSize(new Dimension(340, 300));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        symbolDropdown = new JComboBox<>(new String[] {"AAPL", "MSFT", "GOOGL"});
        symbolDropdown.addActionListener(e -> onSymbolChanged());
        orderTypeDropdown = new JComboBox<>(OrderType.values());
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        priceField = new JTextField();
        priceField.setEnabled(false);

        orderTypeDropdown.addActionListener(e -> {
            OrderType type = (OrderType) orderTypeDropdown.getSelectedItem();
            priceField.setEnabled(type == OrderType.LIMIT || type == OrderType.STOP);
        });

        buyButton = new JButton("BUY");
        buyButton.setForeground(new Color(0, 200, 120));
        sellButton = new JButton("SELL");
        sellButton.setForeground(new Color(220, 70, 70));

        buyButton.addActionListener(e -> submitOrder(OrderSide.BUY));
        sellButton.addActionListener(e -> submitOrder(OrderSide.SELL));

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        ticket.add(new JLabel("Symbol"), gbc);
        gbc.gridx = 1;
        ticket.add(symbolDropdown, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        ticket.add(new JLabel("Order Type"), gbc);
        gbc.gridx = 1;
        ticket.add(orderTypeDropdown, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        ticket.add(new JLabel("Quantity"), gbc);
        gbc.gridx = 1;
        ticket.add(quantitySpinner, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        ticket.add(new JLabel("Price (Limit/Stop)"), gbc);
        gbc.gridx = 1;
        ticket.add(priceField, gbc);

        JPanel actionRow = new JPanel(new GridLayout(1, 2, 8, 0));
        actionRow.add(buyButton);
        actionRow.add(sellButton);
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        ticket.add(actionRow, gbc);
        return ticket;
    }

    private JPanel buildBottomPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(12, 12));
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        wrapper.setMinimumSize(new Dimension(600, 260));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Positions", buildPositionsPanel());
        tabs.addTab("Order History", buildHistoryPanel());

        toastArea = new JTextArea(3, 40);
        toastArea.setEditable(false);
        toastArea.setLineWrap(true);
        toastArea.setWrapStyleWord(true);
        toastArea.setBorder(BorderFactory.createTitledBorder("Execution Toasts"));

        wrapper.add(tabs, BorderLayout.CENTER);
        wrapper.add(toastArea, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildPositionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        positionModel = new DefaultTableModel(new Object[][] {}, new String[] {
            "Symbol", "Qty", "Avg Entry", "Mark", "PnL"
        });
        JTable table = new JTable(positionModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton closeSelected = new JButton("Close Position");
        JButton closeAll = new JButton("Close All");
        actions.add(closeSelected);
        actions.add(closeAll);
        panel.add(actions, BorderLayout.SOUTH);

        closeSelected.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                pushToast("Select a position row first.");
                return;
            }
            String symbol = String.valueOf(positionModel.getValueAt(row, 0));
            executeClose(symbol);
        });
        closeAll.addActionListener(e -> {
            for (int i = 0; i < positionModel.getRowCount(); i++) {
                executeClose(String.valueOf(positionModel.getValueAt(i, 0)));
            }
        });
        return panel;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        historyModel = new DefaultTableModel(new Object[][] {}, new String[] {
            "Time", "Symbol", "Side", "Type", "Qty", "Req Price", "Fill", "Status", "Message"
        });
        panel.add(new JScrollPane(new JTable(historyModel)), BorderLayout.CENTER);
        return panel;
    }

    private void onSymbolChanged() {
        String symbol = (String) symbolDropdown.getSelectedItem();
        SyntheticDataGenerator.ensureDataFile(symbol);
        marketDataFeed = new MarketDataFeed(symbol + "_test.csv");
        symbolLabel.setText("Symbol: " + symbol);
        refreshUi();
    }

    private void startMarketStreaming() {
        marketTimer = new Timer(900, e -> {
            marketDataFeed.advance();
            tradingService.refreshMark((String) symbolDropdown.getSelectedItem(), marketDataFeed.getCurrentPrice());
            refreshUi();
        });
        marketTimer.start();
    }

    private void setOrderButtonsLoading(boolean loading) {
        buyButton.setEnabled(!loading);
        sellButton.setEnabled(!loading);
    }

    private void submitOrder(OrderSide side) {
        OrderType type = (OrderType) orderTypeDropdown.getSelectedItem();
        String symbol = (String) symbolDropdown.getSelectedItem();
        int quantity = (Integer) quantitySpinner.getValue();
        Double triggerPrice = null;
        if (type == OrderType.LIMIT || type == OrderType.STOP) {
            try {
                triggerPrice = Double.parseDouble(priceField.getText());
            } catch (Exception ignored) {
                pushToast("Enter a valid trigger price for limit/stop orders.");
                return;
            }
        }

        ManualOrder order = new ManualOrder(symbol, type, side, quantity, triggerPrice);
        setOrderButtonsLoading(true);
        SwingWorker<OrderExecutionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OrderExecutionResult doInBackground() {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ignored) {
                }
                return tradingService.submitOrder(order, marketDataFeed.getCurrentPrice());
            }

            @Override
            protected void done() {
                setOrderButtonsLoading(false);
                try {
                    OrderExecutionResult result = get();
                    if (result.getStatus() == OrderStatus.FILLED) {
                        pushToast(side + " " + quantity + " " + symbol + " filled at $" + String.format("%.2f", result.getFillPrice()));
                    } else {
                        pushToast("Order failed: " + result.getReason());
                    }
                } catch (Exception ex) {
                    pushToast("Order failed: " + ex.getMessage());
                }
                refreshUi();
            }
        };
        worker.execute();
    }

    private void executeClose(String symbol) {
        OrderExecutionResult result = tradingService.closePosition(symbol, marketDataFeed.getCurrentPrice());
        if (result.getStatus() == OrderStatus.FILLED) {
            pushToast("Position for " + symbol + " closed.");
        } else {
            pushToast("Close failed: " + result.getReason());
        }
        refreshUi();
    }

    private void refreshUi() {
        double mark = marketDataFeed.getCurrentPrice();
        markLabel.setText(String.format("Price: $%.2f", mark));
        chartPanel.setCandles("Candles + Volume (" + symbolDropdown.getSelectedItem() + ")", marketDataFeed.getVisibleCandles(80));

        PortfolioState state = tradingService.getPortfolioState();
        double holdings = 0.0;
        positionModel.setRowCount(0);
        for (Map.Entry<String, PositionSnapshot> entry : state.getPositions().entrySet()) {
            PositionSnapshot p = entry.getValue();
            holdings += p.getMarkPrice() * p.getQuantity();
            positionModel.addRow(new Object[] {
                p.getSymbol(),
                p.getQuantity(),
                String.format("$%.2f", p.getAverageEntryPrice()),
                String.format("$%.2f", p.getMarkPrice()),
                String.format("$%.2f", p.getPnl())
            });
        }

        cashLabel.setText(String.format("Cash: $%,.2f", state.getCashBalance()));
        equityLabel.setText(String.format("Equity: $%,.2f", state.getCashBalance() + holdings));

        historyModel.setRowCount(0);
        for (OrderRecord record : state.getOrderHistory()) {
            historyModel.addRow(new Object[] {
                TIME_FMT.format(record.getTimestamp()),
                record.getSymbol(),
                record.getSide().name(),
                record.getOrderType().name(),
                record.getQuantity(),
                String.format("$%.2f", record.getRequestedPrice()),
                String.format("$%.2f", record.getFillPrice()),
                record.getStatus().name(),
                record.getReason()
            });
        }
    }

    private void pushToast(String message) {
        toastArea.append("[" + TIME_FMT.format(java.time.Instant.now()) + "] " + message + "\n");
    }

    private void ensureSyntheticDataFiles() {
        SyntheticDataGenerator.ensureDataFile("AAPL");
        SyntheticDataGenerator.ensureDataFile("MSFT");
        SyntheticDataGenerator.ensureDataFile("GOOGL");
    }
}