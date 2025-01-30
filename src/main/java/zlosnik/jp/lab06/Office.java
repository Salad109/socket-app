package zlosnik.jp.lab06;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class Office {
    private JLabel portLabel;
    private JTextArea textArea;

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

        frame.setLayout(new BorderLayout());
        frame.add(portLabel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

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

        if (parts.length == 2 && "REGISTER".equalsIgnoreCase(parts[0])) {
            // todo
        } else if (parts.length == 2 && "ORDER".equalsIgnoreCase(parts[0])) {
            // todo
        } else {
            writer.println("Unknown request");
            logMessage("Sent: Unknown request");
        }
    }
}