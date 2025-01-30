package zlosnik.jp.lab06;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Tanker {

    private static final int TOTAL_CAPACITY = 20;

    private String houseHostname = "localhost";
    private int housePort = 12345;
    private String sewagePlantHostname = "localhost";
    private int sewagePlantPort = 12345;
    private int currentSewageAmount = 0;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private JTextArea textArea;
    private JLabel sewageLabel;
    private JTextField houseHostnameField;
    private JTextField housePortField;
    private JTextField sewagePlantHostnameField;
    private JTextField sewagePlantPortField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Tanker::new);
    }

    public Tanker() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Tanker");

        houseHostnameField = new JTextField(houseHostname, 10);
        housePortField = new JTextField(String.valueOf(housePort), 10);
        sewagePlantHostnameField = new JTextField(sewagePlantHostname, 10);
        sewagePlantPortField = new JTextField(String.valueOf(sewagePlantPort), 10);

        sewageLabel = new JLabel(getSewageLabelText());

        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton sewageGetButton = new JButton("Get Sewage");
        sewageGetButton.addActionListener(e -> fetchSewage());
        JButton sewageDumpButton = new JButton("Dump Sewage");
        sewageDumpButton.addActionListener(e -> dumpSewage());

        JPanel inputPanel = new JPanel(new GridLayout(0, 2));
        inputPanel.add(new JLabel("House hostname:"));
        inputPanel.add(houseHostnameField);
        inputPanel.add(new JLabel("House port:"));
        inputPanel.add(housePortField);
        inputPanel.add(new JLabel());
        inputPanel.add(sewageGetButton);

        inputPanel.add(new JLabel("Sewage plant hostname:"));
        inputPanel.add(sewagePlantHostnameField);
        inputPanel.add(new JLabel("Sewage plant port:"));
        inputPanel.add(sewagePlantPortField);
        inputPanel.add(new JLabel());
        inputPanel.add(sewageDumpButton);

        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(sewageLabel, BorderLayout.SOUTH);

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

    private void fetchSewage() {
        try {
            houseHostname = houseHostnameField.getText();
            housePort = Integer.parseInt(housePortField.getText());
            connectToServer(houseHostname, housePort);

            int remainingCapacity = TOTAL_CAPACITY - currentSewageAmount;
            String request = "DRAIN SEWAGE " + remainingCapacity;
            sendRequest(request);
        } catch (NumberFormatException ex) {
            logMessage("Invalid housePort number: " + ex.getMessage());
        }
    }

    private void dumpSewage() {
        try {
            sewagePlantHostname = sewagePlantHostnameField.getText();
            sewagePlantPort = Integer.parseInt(sewagePlantPortField.getText());
            connectToServer(sewagePlantHostname, sewagePlantPort);

            String request = "DUMP SEWAGE " + currentSewageAmount;
            sendRequest(request);
        } catch (NumberFormatException ex) {
            logMessage("Invalid sewagePlantPort number: " + ex.getMessage());
        }
    }

    private void connectToServer(String hostname, int port) {
        try {
            socket = new Socket(hostname, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logMessage("Connected to server on port " + housePort);
        } catch (UnknownHostException ex) {
            logMessage("Unknown host: " + ex.getMessage());
        } catch (IOException ex) {
            logMessage("I/O error: " + ex.getMessage());
        }
    }

    private void sendRequest(String request) {
        if (socket == null || socket.isClosed()) {
            logMessage("Error: Not connected to the server.");
            return;
        }

        try {
            writer.println(request);
            logMessage("Sent: " + request);

            String response = reader.readLine();
            processResponse(response);
        } catch (IOException ex) {
            logMessage("I/O error while sending request: " + ex.getMessage());
        }
    }

    private void processResponse(String response) {
        if (response == null || response.isEmpty()) {
            logMessage("No response from server.");
            return;
        }

        logMessage("Received: " + response);
        String[] parts = response.split(" ");
        if (parts.length == 3 && "DRAINED SEWAGE".equals(parts[0] + " " + parts[1])) {
            try {
                int receivedSewage = Integer.parseInt(parts[2]);
                currentSewageAmount += receivedSewage;
                sewageLabel.setText(getSewageLabelText());
            } catch (NumberFormatException ex) {
                logMessage("Invalid sewage amount in response: " + parts[2]);
            }
        } else if (parts.length == 3 && "DUMPED SEWAGE".equals(parts[0] + " " + parts[1])) {
            try {
                int dumpedSewage = Integer.parseInt(parts[2]);
                currentSewageAmount -= dumpedSewage;
                sewageLabel.setText(getSewageLabelText());
            } catch (NumberFormatException ex) {
                logMessage("Invalid sewage amount in response: " + parts[2]);
            }
        } else {
            logMessage("Unknown response: " + response);
        }
    }

    private String getSewageLabelText() {
        return "Current sewage amount: " + currentSewageAmount + "/" + TOTAL_CAPACITY;
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }
}
