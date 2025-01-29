import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ChatSession {

    static App app = CentralManager.getApp();
	
    public void createChatRoomUI(JFrame frame, Socket clientSocket, String username, String pingUsername) {
        System.out.println(app.username);

        frame.getContentPane().removeAll();
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());

        // Left panel for users online
        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setPreferredSize(new Dimension(200, 600));
        usersPanel.setBorder(BorderFactory.createTitledBorder("Users Online:"));

        String[] users = {username, pingUsername};
        for (String user : users) {
            JPanel userPanel = new JPanel();
            userPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel userLabel;

            if (user.equals(username)) {
                userLabel = new JLabel(user + " (You)");
                userLabel.setForeground(Color.RED); // Your username in red
            } else {
                userLabel = new JLabel(user);
            }

            JLabel statusIcon = new JLabel(new ImageIcon("status_icon.png"));
            userPanel.add(statusIcon);
            userPanel.add(userLabel);
            usersPanel.add(userPanel);
        }

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(usersPanel, BorderLayout.WEST);
        frame.add(chatPanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();

        sendButton.addActionListener(e -> {
            String message = inputField.getText().trim();
            if (!message.isEmpty()) {
                try {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println(message);
                    chatArea.append(username +": "+ message + "\n");
                    inputField.setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Failed to send message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String receivedMessage;
                while ((receivedMessage = in.readLine()) != null) {
                    if ("###DISCONNECT###".equals(receivedMessage)) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame, "Chat Closed. User disconnected.", "Disconnected", JOptionPane.INFORMATION_MESSAGE);
                            app.loggedInMenu(frame, null, null);
                        });
                        break;
                    }

                    String finalMessage = receivedMessage;
                    SwingUtilities.invokeLater(() -> chatArea.append("Peer: " + finalMessage + "\n"));
                }
            } catch (IOException ex) {
                System.out.println("Connection closed: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Chat Closed. User disconnected.", "Disconnected", JOptionPane.INFORMATION_MESSAGE);
                    app.loggedInMenu(frame, null, null);
                });
            }
        }).start();

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    out.println("###DISCONNECT###");
                    out.println("###DISCONNECT###");

                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.exit(0);
                }
            }
        });

        // Add a shutdown hook for Ctrl+C or program termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (clientSocket != null && !clientSocket.isClosed()) {
                try {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    out.println("###DISCONNECT###");
                    out.println("###DISCONNECT###");

                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

}
