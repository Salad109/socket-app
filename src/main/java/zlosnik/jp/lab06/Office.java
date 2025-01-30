package zlosnik.jp.lab06;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Office {
    private JLabel portLabel;
    private JTextArea textArea;
    private JTextField sewagePlantHostnameField;
    private JTextField sewagePlantPortField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Office::new);
    }

    public Office() {
        createAndShowGUI();
        Server server = new Server(textArea, portLabel, this::processRequest);
        server.startServerInBackground();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Office");

        portLabel = new JLabel("Port: ");
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        sewagePlantHostnameField = new JTextField("localhost", 10);
        sewagePlantPortField = new JTextField("12345", 10);

        JButton sendRequestButton = new JButton("Send Request");
        sendRequestButton.addActionListener(e -> sendRequestToSewagePlant());

        JPanel inputPanel = new JPanel(new GridLayout(0, 2));
        inputPanel.add(new JLabel("Sewage Plant Hostname:"));
        inputPanel.add(sewagePlantHostnameField);
        inputPanel.add(new JLabel("Sewage Plant Port:"));
        inputPanel.add(sewagePlantPortField);
        inputPanel.add(new JLabel());
        inputPanel.add(sendRequestButton);

        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(portLabel, BorderLayout.SOUTH);

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
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

        if (parts.length == 2 && "REGISTER TANKER".equals(parts[0])) {
            // todo
        } else if (parts.length == 2 && "ORDER SERVICE".equals(parts[0])) {
            // todo
        } else {
            writer.println("Unknown request");
            logMessage("Sent: Unknown request");
        }
    }

    private void sendRequestToSewagePlant() {
        String hostname = sewagePlantHostnameField.getText();
        int port = Integer.parseInt(sewagePlantPortField.getText());

        try (Socket socket = new Socket(hostname, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String request = "REQUEST BILL";
            writer.println(request);
            logMessage("Sent: " + request);

            String response = reader.readLine();
            logMessage("Received: " + response);

        } catch (IOException e) {
            logError("Error connecting to SewagePlant", e);
        }
    }
}