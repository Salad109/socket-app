package zlosnik.jp.lab06;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Tanker {

    private static final int TOTAL_CAPACITY = 20;

    private String hostname = "localhost";
    private int port = 12345;
    private int currentSewageAmount = 0;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private JTextArea textArea;
    private JLabel sewageLabel;
    private JTextField hostnameField;
    private JTextField portField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Tanker::new);
    }

    public Tanker() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Tanker");

        // Input fields
        hostnameField = new JTextField(hostname, 10);
        portField = new JTextField(String.valueOf(port), 10);

        // Sewage label
        sewageLabel = new JLabel(getSewageLabelText());

        // Text area for logs
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Button
        JButton button = new JButton("Get Sewage");
        button.addActionListener(e -> fetchSewage());

        // Layout
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Hostname:"));
        inputPanel.add(hostnameField);
        inputPanel.add(new JLabel("Port:"));
        inputPanel.add(portField);
        inputPanel.add(button);

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
        hostname = hostnameField.getText();
        try {
            port = Integer.parseInt(portField.getText());
            connectToServer();
            sendRequest();
        } catch (NumberFormatException ex) {
            logMessage("Invalid port number: " + ex.getMessage());
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket(hostname, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logMessage("Connected to server on port " + port);
        } catch (UnknownHostException ex) {
            logMessage("Unknown host: " + ex.getMessage());
        } catch (IOException ex) {
            logMessage("I/O error: " + ex.getMessage());
        }
    }

    private void sendRequest() {
        if (socket == null || socket.isClosed()) {
            logMessage("Error: Not connected to the server.");
            return;
        }

        try {
            int remainingCapacity = TOTAL_CAPACITY - currentSewageAmount;
            String request = "DRAIN SEWAGE " + remainingCapacity;

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
        if (parts.length == 3 && "DRAINED SEWAGE".equalsIgnoreCase(parts[0] + " " + parts[1])) {
            try {
                int receivedSewage = Integer.parseInt(parts[2]);
                currentSewageAmount += receivedSewage;
                sewageLabel.setText(getSewageLabelText());
            } catch (NumberFormatException ex) {
                logMessage("Invalid sewage amount in response: " + parts[2]);
            }
        } else {
            logMessage("Unexpected response: " + response);
        }
    }

    private String getSewageLabelText() {
        return "Current sewage amount: " + currentSewageAmount + "/" + TOTAL_CAPACITY;
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }
}
