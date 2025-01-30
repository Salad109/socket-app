package zlosnik.jp.lab06;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class SewagePlant {
    private JLabel portLabel;
    private JTextArea textArea;
    private JLabel sewageLabel;
    private int accumulatedSewage = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SewagePlant::new);
    }

    public SewagePlant() {
        createAndShowGUI();
        Server server = new Server(textArea, portLabel, this::processRequest);
        server.startServerInBackground();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Sewage Plant");

        portLabel = new JLabel("Port: ");
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        sewageLabel = new JLabel("Accumulated sewage: 0");

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(portLabel);
        bottomPanel.add(sewageLabel);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }

    private void processRequest(String request, PrintWriter writer) {
        String[] parts = request.split(" ");
        if (parts.length == 3 && "DUMP SEWAGE".equals(parts[0] + " " + parts[1])) {
            handleSewageDump(parts[2], writer);
        } else if ("REQUEST BILL".equals(parts[0] + " " + parts[1])) {
            handleBillRequest(writer);
        } else {
            writer.println("Unknown request");
            logMessage("Sent: Unknown request");
        }
    }

    private void handleSewageDump(String dumpedSewage, PrintWriter writer) {
        try {
            String response = "DUMPED SEWAGE " + dumpedSewage;
            writer.println(response);
            logMessage("Sent: " + response);
            accumulatedSewage += Integer.parseInt(dumpedSewage);
            sewageLabel.setText("Accumulated sewage: " + accumulatedSewage);
        } catch (NumberFormatException e) {
            writer.println("Invalid request format");
            logMessage("Sent: Invalid request format");
        }
    }

    private void handleBillRequest(PrintWriter writer) {
        int bill = accumulatedSewage * 100;
        String response = "BILL " + bill;
        writer.println(response);
        logMessage("Sent: " + response);
        accumulatedSewage = 0;
        sewageLabel.setText("Accumulated sewage: 0");
    }
}