import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class JoinPeerToPeer {

    private ServerSocket serverSocket;
    private Socket clientSocket;

    static ChatSession chatSession = CentralManager.getChatSession();
    static App app = CentralManager.getApp();
    
    public String user;
    
    public void peerToPeerUI(JFrame frame, boolean inP2P, String username) {
        // Clear frame
    	user = username;
        frame.getContentPane().removeAll();

        if (inP2P) {
            setupP2PUI(frame);
        } else {
            setupConnectionUI(frame);
        }
    }

    private void setupP2PUI(JFrame frame) {
        // Code for inP2P == true
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel message = new JLabel("Connected to Peer!", JLabel.CENTER);
        message.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(message, BorderLayout.CENTER);

        // Add Back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> app.loggedInMenu(frame, null, null)); // Navigate back to menu
        panel.add(backButton, BorderLayout.SOUTH);

        frame.add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private void setupConnectionUI(JFrame frame) {
        // Set up the frame properties
        frame.setTitle("AmnesicChat - Connect to Peer");
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 300);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("PEER TO PEER (NOT CONNECTED TO A PEER)", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // IP Address/Domain field
        JLabel ipLabel = new JLabel("IP Address/Domain:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(ipLabel, gbc);

        JTextField ipField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(ipField, gbc);

        // Port field
        JLabel portLabel = new JLabel("Peer To Peer Port:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(portLabel, gbc);

        JTextField portField = new JTextField("50000"); // Suggested default port
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(portField, gbc);

        // Attempt connection button
        JButton connectButton = new JButton("Connect to Peer");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(connectButton, gbc);

        // Action listener for connection
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = ipField.getText().trim();
                String portStr = portField.getText().trim();

                if (ip.isEmpty() || portStr.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter both IP address and port.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int port = Integer.parseInt(portStr);
                    connectToPeer(ip, port);
                    chatSession.createChatRoomUI(frame, clientSocket, user, "someValue");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid port number.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to connect: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add Back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> app.loggedInMenu(frame, null, null)); // Navigate back to menu
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(backButton, gbc);

        // Add main panel to the frame
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private void connectToPeer(String ip, int port) throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }

        clientSocket = new Socket(ip, port);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        // Send username immediately after connecting
        out.println(user);  

        // Start listening for incoming messages
        new Thread(() -> listenForMessages(clientSocket)).start();
    }


    private void listenForMessages(Socket socket) {
        try {
            // Handle incoming messages from the peer
            var input = socket.getInputStream();
            var buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                String message = new String(buffer, 0, bytesRead);
                System.out.println("Received: " + message); // Debug: Log the received message
            }
        } catch (IOException e) {
            System.out.println("Connection closed: " + e.getMessage());
        }
    }

    // Optional: Stop listening on the port
    public void closePort() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
    }
}
