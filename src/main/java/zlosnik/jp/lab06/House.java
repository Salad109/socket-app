package zlosnik.jp.lab06;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class House {

    private static final AtomicInteger sewageCounter = new AtomicInteger(0);

    private JLabel sewageCounterLabel;
    private JLabel portLabel;
    private JTextArea textArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(House::new);
    }

    public House() {
        createAndShowGUI();
        startSewageIncrementer();
        Server server = new Server(textArea, portLabel, this::processRequest);
        server.startServerInBackground();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("House");

        sewageCounterLabel = new JLabel(getSewageCounterText());
        portLabel = new JLabel("Port: ");
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        frame.setLayout(new BorderLayout());
        frame.add(portLabel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(sewageCounterLabel, BorderLayout.SOUTH);

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

    private void startSewageIncrementer() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    sewageCounter.incrementAndGet();
                    SwingUtilities.invokeLater(() -> sewageCounterLabel.setText(getSewageCounterText()));
                } catch (InterruptedException e) {
                    logError("Sewage incrementer interrupted", e);
                }
            }
        }).start();
    }

    private String getSewageCounterText() {
        return "Sewage counter: " + sewageCounter.get();
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }

    private void logError(String message, Exception e) {
        SwingUtilities.invokeLater(() -> textArea.append(message + ": " + e.getMessage() + "\n"));
        e.printStackTrace();
    }

    private void processRequest(String request, PrintWriter writer) {
        String[] parts = request.split(" ");
        if (parts.length == 3 && "DRAIN SEWAGE".equalsIgnoreCase(parts[0] + " " + parts[1])) {
            handleSewageRequest(parts[2], writer);
        } else {
            writer.println("Unknown request");
            logMessage("Sent: Unknown request");
        }
    }

    private void handleSewageRequest(String remainingCapacityStr, PrintWriter writer) {
        try {
            int remainingCapacity = Integer.parseInt(remainingCapacityStr);
            int amountToSend = Math.min(remainingCapacity, sewageCounter.get());
            sewageCounter.addAndGet(-amountToSend);

            String response = "DRAINED SEWAGE " + amountToSend;
            writer.println(response);

            SwingUtilities.invokeLater(() -> {
                sewageCounterLabel.setText(getSewageCounterText());
                textArea.append("Sent: " + response + "\n");
            });
        } catch (NumberFormatException e) {
            writer.println("Invalid request format");
            logMessage("Sent: Invalid request format");
        }
    }
}