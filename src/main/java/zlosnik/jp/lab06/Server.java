package zlosnik.jp.lab06;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final JTextArea textArea;
    private final JLabel portLabel;
    private final RequestHandler requestHandler;

    public Server(JTextArea textArea, JLabel portLabel, RequestHandler requestHandler) {
        this.textArea = textArea;
        this.portLabel = portLabel;
        this.requestHandler = requestHandler;
    }

    public void startServerInBackground() {
        new Thread(this::startServer).start();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();
            SwingUtilities.invokeLater(() -> {
                portLabel.setText("Port: " + port);
                logMessage("Server is listening on port " + port);
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
                requestHandler.processRequest(request, writer);
            }
        }
    }

    public interface RequestHandler {
        void processRequest(String request, PrintWriter writer);
    }
}