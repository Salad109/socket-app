package zlosnik.jp.lab06;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SewagePlant {
    private JLabel portLabel;
    private JTextArea textArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SewagePlant::new);
    }

    public SewagePlant() {
        createAndShowGUI();
        startServerInBackground();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Sewage Plant");

        // Initialize components
        portLabel = new JLabel("Port: ");
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Layout
        frame.setLayout(new BorderLayout());
        frame.add(portLabel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }


    private void startServerInBackground() {
        new Thread(this::startServer).start(); // Run server logic in a background thread
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();
            SwingUtilities.invokeLater(() -> {
                portLabel.setText("Port: " + port);
                logMessage("Sewage Plant is listening on port " + port);
            });

            while (true) {
                Socket socket = serverSocket.accept();
                logMessage("New client connected");
                new ServerThread(socket).start();
            }
        } catch (IOException e) {
            logError("Server error", e);
        }
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }

    private void logError(String message, Exception e) {
        SwingUtilities.invokeLater(() -> textArea.append(message + ": " + e.getMessage() + "\n"));
        e.printStackTrace();
    }

    private class ServerThread extends Thread {
        private final Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    InputStream input = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true)
            ) {
                handleClientCommunication(reader, writer);
            } catch (IOException e) {
                logError("Connection error", e);
            }
        }

        private void handleClientCommunication(BufferedReader reader, PrintWriter writer) throws IOException {
            String request;
            while ((request = reader.readLine()) != null) {
                logMessage("Received: " + request);
                processRequest(request, writer);
            }
        }

        private void processRequest(String request, PrintWriter writer) {
            String[] parts = request.split(" ");
            if (parts.length == 3 && "DUMP SEWAGE".equalsIgnoreCase(parts[0] + " " + parts[1])) {
                handleSewageDump(parts[2], writer);
            } else {
                writer.println("Unknown request");
                logMessage("Sent: Unknown request");
            }
        }

        private void handleSewageDump(String dumpedSewage, PrintWriter writer) {
            try {
                String response = "DUMPED SEWAGE " + dumpedSewage;
                writer.println(response);
            } catch (NumberFormatException e) {
                writer.println("Invalid request format");
                logMessage("Sent: Invalid request format");
            }
        }
    }
}
