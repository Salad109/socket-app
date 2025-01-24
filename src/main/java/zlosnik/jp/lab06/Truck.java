package zlosnik.jp.lab06;

                import javax.swing.*;
                import java.io.*;
                import java.net.Socket;
                import java.net.UnknownHostException;

                public class Truck {
                    private static final String HOSTNAME = "localhost";
                    private static int port;
                    private static PrintWriter writer;
                    private static BufferedReader reader;
                    private static JTextArea textArea;
                    private static final int TOTAL_CAPACITY = 20;
                    private static int currentSewageAmount = 0;
                    private static JLabel sewageLabel;

                    public static void main(String[] args) {
                        JFrame frame = new JFrame("Truck");
                        JButton button = new JButton("Get Sewage");
                        textArea = new JTextArea(10, 30);
                        textArea.setEditable(false);
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        JTextField portField = new JTextField("12345", 10);
                        sewageLabel = new JLabel("Current sewage amount: 0");

                        button.addActionListener(e -> {
                            port = Integer.parseInt(portField.getText());
                            connectToServer();
                            sendRequest();
                        });

                        JPanel panel = new JPanel();
                        panel.add(new JLabel("House port:"));
                        panel.add(portField);
                        panel.add(button);

                        frame.getContentPane().add(panel, "North");
                        frame.getContentPane().add(scrollPane, "Center");
                        frame.getContentPane().add(sewageLabel, "South");
                        frame.setSize(400, 300);
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setAlwaysOnTop(true);
                        frame.setVisible(true);
                    }

                    private static void connectToServer() {
                        Socket socket;
                        try {
                            socket = new Socket(HOSTNAME, port);
                            OutputStream output = socket.getOutputStream();
                            writer = new PrintWriter(output, true);
                            InputStream input = socket.getInputStream();
                            reader = new BufferedReader(new InputStreamReader(input));
                            SwingUtilities.invokeLater(() -> textArea.append("Connected to server on port " + port + "\n"));
                        } catch (UnknownHostException ex) {
                            SwingUtilities.invokeLater(() -> textArea.append("House not found: " + ex.getMessage() + "\n"));
                        } catch (IOException ex) {
                            SwingUtilities.invokeLater(() -> textArea.append("I/O error: " + ex.getMessage() + "\n"));
                        }
                    }

                    private static void sendRequest() {
                        try {
                            int remainingCapacity = TOTAL_CAPACITY - currentSewageAmount;
                            writer.println("GET " + remainingCapacity);
                            SwingUtilities.invokeLater(() -> textArea.append("Sent request: GET " + remainingCapacity + "\n"));
                            String response = reader.readLine();
                            int receivedSewage = Integer.parseInt(response.split(" ")[1]);
                            currentSewageAmount += receivedSewage;
                            SwingUtilities.invokeLater(() -> textArea.append("House response: Received " + receivedSewage + " units of sewage\n"));
                            SwingUtilities.invokeLater(() -> sewageLabel.setText("Current sewage amount: " + currentSewageAmount));
                        } catch (IOException ex) {
                            SwingUtilities.invokeLater(() -> textArea.append("I/O error: " + ex.getMessage() + "\n"));
                        }
                    }
                }