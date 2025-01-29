import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*;
import java.io.*; 

public class ChatSession {
	public void createChatRoomUI(JFrame frame) {
        // Clear frame
        frame.getContentPane().removeAll();
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());

        // Left panel for users online
        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setPreferredSize(new Dimension(200, 600));
        usersPanel.setBorder(BorderFactory.createTitledBorder("Users Online:"));

        // Sample users
        String[] users = {"amnesic1122qs", "amnesic1ea5", "amnesic1vw42", "amnesic1aw35"};
        for (String user : users) {
            JPanel userPanel = new JPanel();
            userPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel userLabel = new JLabel(user);
            JLabel statusIcon = new JLabel(new ImageIcon("status_icon.png")); // Replace
            userPanel.add(statusIcon);
            userPanel.add(userLabel);
            usersPanel.add(userPanel);
        }

        // Middle panel for chat
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        // Chat messages
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setText("amnesic1122qs: [NO PGP KEY]\n" +
                "amnesic1ea5: Oh cool!\n" +
                "amnesic1ea5: [USER NO AUTH]\n" +
                "amnesic1vw42: [NO DEVICE ID]\n" +
                "amnesic1aw: Sounds like a plan!\n" +
                "amnesic441: Alright, so when will we meet?");
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Text input area
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // Center image
        JLabel imageLabel = new JLabel(new ImageIcon("image.png")); // Replace 
        chatPanel.add(imageLabel, BorderLayout.NORTH);

        // Right panel for options
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setPreferredSize(new Dimension(200, 600));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("P2P / {CHAT ROOM NAME}"));

        // Encryption methods
        JLabel encryptionLabel = new JLabel("Encryption Methods for Sending Messages:");
        optionsPanel.add(encryptionLabel);

        String[] methods = {"AES", "Serpent", "Twofish", "Camellia", "Kuznyechik"};
        for (String method : methods) {
            JCheckBox checkBox = new JCheckBox(method);
            optionsPanel.add(checkBox);
        }

        // Buttons
        JButton openChatButton = new JButton("Open Another Chat Session");
        JButton inviteButton = new JButton("Invite People");
        JButton disconnectButton = new JButton("Disconnect");
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacing
        optionsPanel.add(openChatButton);
        optionsPanel.add(inviteButton);
        optionsPanel.add(disconnectButton);

        // Add components to the frame
        frame.add(usersPanel, BorderLayout.WEST);
        frame.add(chatPanel, BorderLayout.CENTER);
        frame.add(optionsPanel, BorderLayout.EAST);

        frame.revalidate();
        frame.repaint();
    }
}