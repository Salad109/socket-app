package zlosnik.jp.lab06;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class House {
    private static final AtomicInteger SEWAGE_COUNTER = new AtomicInteger(0);
    private static JLabel sewageCounterLabel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("House");
        sewageCounterLabel = new JLabel("Sewage counter: 0");
        JLabel portLabel = new JLabel("Port: ");
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        frame.getContentPane().add(portLabel, "North");
        frame.getContentPane().add(sewageCounterLabel, "South");
        frame.getContentPane().add(scrollPane, "Center");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);

        // Start the SEWAGE_COUNTER increment thread
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    int newValue = SEWAGE_COUNTER.incrementAndGet();
                    SwingUtilities.invokeLater(() -> sewageCounterLabel.setText("Sewage counter: " + newValue));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            final int port = serverSocket.getLocalPort();
            SwingUtilities.invokeLater(() -> portLabel.setText("Port: " + port));
            SwingUtilities.invokeLater(() -> textArea.append("House is listening on port " + port + "\n"));
            while (true) {
                Socket socket = serverSocket.accept();
                SwingUtilities.invokeLater(() -> textArea.append("New client connected\n"));
                new ServerThread(socket, SEWAGE_COUNTER, textArea).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static class ServerThread extends Thread {
        private final Socket socket;
        private final AtomicInteger sewageCounter;
        private final JTextArea textArea;

        public ServerThread(Socket socket, AtomicInteger sewageCounter, JTextArea textArea) {
            this.socket = socket;
            this.sewageCounter = sewageCounter;
            this.textArea = textArea;
        }

        @Override
        public void run() {
            try (InputStream input = socket.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                 OutputStream output = socket.getOutputStream();
                 PrintWriter writer = new PrintWriter(output, true)) {

                String text;
                while ((text = reader.readLine()) != null) {
                    String finalText = text;
                    SwingUtilities.invokeLater(() -> textArea.append("Received: " + finalText + "\n"));
                    String[] parts = text.split(" ");
                    String command = parts[0];
                    int remainingCapacity = Integer.parseInt(parts[1]);

                    if (command.equalsIgnoreCase("GET")) {
                        int amountToSend = Math.min(remainingCapacity, sewageCounter.get());
                        int newSewageCounterValue = sewageCounter.addAndGet(-amountToSend);
                        writer.println("SENT " + amountToSend);
                        SwingUtilities.invokeLater(() -> textArea.append("Sent: SENT " + amountToSend + "\n"));
                        SwingUtilities.invokeLater(() -> sewageCounterLabel.setText("Sewage counter: " + newSewageCounterValue));
                    } else {
                        writer.println("Unknown request");
                        SwingUtilities.invokeLater(() -> textArea.append("Sent: Unknown request\n"));
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}