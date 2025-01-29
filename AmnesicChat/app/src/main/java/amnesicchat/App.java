import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Arrays;
import javax.swing.*;
import java.security.Key;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.io.IOException;
import javax.crypto.spec.IvParameterSpec;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class App {
	
	/*
	 * 
	 * The App class is the main class.
	 * 
	 * It will be the root for running functions like:
	 * - Hash
	 * - CreateAccount
	 * 
	 * The App exists to not only make the entire program
	 * modular but to also allow easy amendments where
	 * necessary.
	 * 
	 * The App functions like a fallback if anything else.
	 * If a server shuts down or a Peer-to-Peer session
	 * ends, the user will be redirected here.
	 * 
	 * */
	
	//UI Defaults
	public URL fileButtonIconURL = getClass().getResource("/images/File.png");
	public URL labelURL = getClass().getResource("/images/AmnesicLabel.png");
	public URL gearURL = getClass().getResource("/images/gear.png");
    private JFrame frame; //JFrame is private so that we can isolate the variable to prevent potential tampering.
    public JPanel appPanel = new JPanel(); // Stops creating new panels unnecessarily.
    public int baseWidth = 650;
    public int baseHeight = 350;	
    public boolean isPortForward = false;
    public String username;
    
    public App() {
        frame = new JFrame("AmnesicChat"); // Constructor for frame
    }

    public JFrame getJFrame() {
        return frame; // Get private frame only
    }
    
    
    // Access the Hash instance
    static Hash hash = CentralManager.getHash();
    
    // Access the CreateAccount instance
    static CreateAccount createAccount = CentralManager.getCreateAccount();
    
    //Access the Peer To Peer instance
    static JoinPeerToPeer peerToPeer = CentralManager.getJoinPeerToPeer();
    
    // Access Settings
    static Settings settings = CentralManager.getSettings();
    
    //Access Chat
    static ChatSession chatSession = CentralManager.getChatSession();
    
    static CipherData cipherData = CentralManager.getCipherData();
    
    static StorageDevices storageDevices = CentralManager.getStorageDevices();
    
    static {
        if (storageDevices == null) {
            System.err.println("StorageDevices is null. Creating a fallback instance.");
            storageDevices = new StorageDevices(); // Provide a fallback instance
        }
    }
    
    // Variables for account creation
    public boolean strictMode = false; // Enforce strict account protection
    public List<String> hashedSerials = new ArrayList<>(); //Device ID encryption key

    private ServerSocket serverSocket;
    private Thread pingListenerThread;

    public void loggedInMenu(JFrame frame, String username, String publicFingerprint) {
        // Stop the ping listener if it is already running
        stopPingListener();

        // Clear frame and set title
        frame.getContentPane().removeAll();
        frame.setTitle("AmnesicChat - Main Menu");

        this.username = username;

        // Set frame size to be more compact
        frame.setSize(600, 525);
        frame.setLocationRelativeTo(null); // Center the window on screen

        // Create the main panel with GridBagLayout for centering
        JPanel appPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Set spacing between components
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Ensure elements are centered

        // AmnesicChat Image Label
        JLabel imageLabel = new JLabel();
        if (labelURL != null) {
            ImageIcon originalIcon = new ImageIcon(labelURL);
            imageLabel.setIcon(originalIcon);
        } else {
            System.out.println("Image not found!");
        }
        gbc.gridy = 0;
        appPanel.add(imageLabel, gbc);

        // Username Label
        JLabel usernameLabel = new JLabel("Username: " + username + " (change)");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 1;
        appPanel.add(usernameLabel, gbc);

        // Fingerprint Label
        JLabel fingerprintLabel = new JLabel("Fingerprint: " + publicFingerprint);
        fingerprintLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 2;
        appPanel.add(fingerprintLabel, gbc);

        // Expanded Fingerprint Info Label
        JLabel fingerprintInfo = new JLabel(
            "<html><div style='text-align: center; width: 400px;'>"
            + "Your public fingerprint is a unique identifier that allows others to find and connect with you securely. "
            + "Think of it like a phone number for encrypted messaging. You can share this with trusted contacts so they can identify you. "
            + "Never share it on public platforms if you want to maintain privacy."
            + "</div></html>"
        );
        fingerprintInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        gbc.gridy = 3;
        appPanel.add(fingerprintInfo, gbc);

        // Copy Fingerprint Button
        JButton copyFingerprintButton = new JButton("Copy Fingerprint");
        copyFingerprintButton.setFont(new Font("Arial", Font.PLAIN, 14));
        copyFingerprintButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(publicFingerprint);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(frame, "Fingerprint copied to clipboard!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        gbc.gridy = 4;
        appPanel.add(copyFingerprintButton, gbc);

        // Create panel for buttons (PEER TO PEER, SETTINGS, QUIT)
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10)); // Vertical stack with spacing
        String[] buttonLabels = {"PEER TO PEER", "SETTINGS", "QUIT"};

        for (String text : buttonLabels) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 14));
            button.setPreferredSize(new Dimension(200, 40));

            switch (text) {
                case "QUIT":
                    button.addActionListener(e -> System.exit(0));
                    break;
                case "PEER TO PEER":
                    button.addActionListener(e -> peerToPeer.peerToPeerUI(frame, false, username));
                    break;
                case "SETTINGS":
                    button.addActionListener(e -> settings.settingsUI());
                    break;
            }

            buttonPanel.add(button);
        }

        gbc.gridy = 5;
        appPanel.add(buttonPanel, gbc);

        // Add panel to frame
        frame.add(appPanel);
        frame.revalidate();
        frame.repaint();

        // Start listening on port 10555
        if (!isPortForward) {
            isPortForward = true;
            startPingListener(frame);
        }
    }
    
private synchronized void startPingListener(JFrame frame) {
    // Check if the ping listener is already running
    if (isPortForward && pingListenerThread != null && pingListenerThread.isAlive()) {
        System.out.println("Ping listener is already running on port 10555.");
        return; // Exit if the listener is already active
    }

    try {
        // Initialize the ServerSocket and allow address reuse
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true); // Allow reuse of the port
        serverSocket.bind(new InetSocketAddress(10555)); // Bind to the port

        isPortForward = true; // Mark the port as open

        // Create the listener thread
        pingListenerThread = new Thread(() -> {
            System.out.println("Ping listener started on port 10555.");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    // Fetch pinger's username
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String pingerUsername = in.readLine(); // Expect first message to be username

                    // Pass to chat UI
                    SwingUtilities.invokeLater(() -> {
                        chatSession.createChatRoomUI(frame, clientSocket, username, pingerUsername);
                    });

                } catch (SocketException e) {
                    if (!serverSocket.isClosed()) e.printStackTrace();
                    System.out.println("Ping listener socket closed.");
                    break;
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) e.printStackTrace();
                }
            }
            System.out.println("Ping listener thread exiting.");
        });


        // Start the thread
        pingListenerThread.start();

    } catch (IOException e) {
        isPortForward = false; // Reset the flag if the port fails to open
        JOptionPane.showMessageDialog(frame, "Failed to open port 10555: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

public synchronized void stopPingListener() {
    try {
        // Close the server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            serverSocket = null;
            System.out.println("Ping listener server socket closed.");
        }

        // Interrupt the listener thread
        if (pingListenerThread != null && pingListenerThread.isAlive()) {
            pingListenerThread.interrupt();
            pingListenerThread.join(); // Wait for the thread to finish
            pingListenerThread = null; // Ensure it won't be reused
            System.out.println("Ping listener thread stopped.");
        }

        isPortForward = false; // Reset the flag so the port can be reopened
    } catch (IOException | InterruptedException e) {
        System.err.println("Error stopping ping listener: " + e.getMessage());
        e.printStackTrace();
    }
}
public void mainMenu(JFrame frame) {
    // Ensure this method runs on EDT (Event Dispatch Thread for stability of program)
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            // Clear the current frame content
            frame.getContentPane().removeAll();

            // Set up the new layout
            frame.setTitle("AmnesicChat - Account");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(650, 450);
            frame.setLayout(new BorderLayout());

            try {
                // Load the favicon image from the resources folder
                URL faviconURL = getClass().getResource("/images/Favicon.png");
                if (faviconURL != null) {
                    ImageIcon favicon = new ImageIcon(faviconURL);
                    frame.setIconImage(favicon.getImage());
                } else {
                    System.out.println("Favicon not found");
                }
            } catch (Exception e) {
                System.out.println("Error loading favicon: " + e.getMessage());
            }

            // Main panel setup
            JPanel appPanel = new JPanel();
            appPanel.setLayout(new BoxLayout(appPanel, BoxLayout.Y_AXIS));
            appPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
            frame.add(appPanel, BorderLayout.CENTER);

            // Banner setup
            JLabel imageLabel = new JLabel();
            URL labelURL = getClass().getResource("/images/AmnesicLabel.png");
            if (labelURL != null) {
                ImageIcon originalIcon = new ImageIcon(labelURL);
                imageLabel.setIcon(originalIcon);
            } else {
                System.out.println("Image not found!");
            }
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            appPanel.add(imageLabel);

            // Header label
            JLabel headerLabel = new JLabel("Create Account", SwingConstants.CENTER);
            headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            appPanel.add(headerLabel);

            appPanel.add(Box.createVerticalStrut(10)); // Add spacing

            // Instructions label
            JLabel instructionLabel = new JLabel(
                    "<html>If you have an existing account, use the directory selection below to open your account file. Otherwise, create a new account with the bottom button.</html>",
                    SwingConstants.CENTER);
            instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
            appPanel.add(instructionLabel);

            appPanel.add(Box.createVerticalStrut(20)); // Add spacing

            // File chooser panel
            JPanel fileChooserPanel = new JPanel();
            fileChooserPanel.setLayout(new BoxLayout(fileChooserPanel, BoxLayout.X_AXIS));

            // Create the file path field
            JTextField filePathField = new JTextField();

            // Create Account / Load Account button
            JButton createAccountButton = new JButton("Create Account");
            createAccountButton.setPreferredSize(new Dimension(200, 40));
            createAccountButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            createAccountButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String filePath = filePathField.getText();
                    if (filePath.isEmpty()) {
                        createAccount.createAccount(frame); // Call create account logic
                    } else {
                        loadAccount(filePath, frame); // Call load account logic
                    }
                }
            });

            // Create the browse button with resized image icon
            URL fileButtonIconURL = getClass().getResource("/images/File.png");
            if (fileButtonIconURL != null) {
                ImageIcon originalIcon = new ImageIcon(fileButtonIconURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH); // Resize image to 50x50
                ImageIcon resizedIcon = new ImageIcon(scaledImage);

                JButton browseButton = new JButton(resizedIcon);
                browseButton.setPreferredSize(new Dimension(50, 50)); // Set fixed dimensions for the button

                // Set the height of the file path field to match the browse button
                filePathField.setMaximumSize(new Dimension(Integer.MAX_VALUE, browseButton.getPreferredSize().height));

                // Add components to the panel
                fileChooserPanel.add(filePathField);
                fileChooserPanel.add(Box.createHorizontalStrut(10)); // Add spacing
                fileChooserPanel.add(browseButton);

                // Add action listener for browse button
                browseButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fileChooser = new JFileChooser();
                        int result = fileChooser.showOpenDialog(frame);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                            // Change the button text to "Load Account" when a file is selected
                            createAccountButton.setText("Load Account");
                        }
                    }
                });

                // Add the components of file choosing to the main panel
                appPanel.add(fileChooserPanel);
            } else {
                System.out.println("File button icon not found");
            }

            appPanel.add(Box.createVerticalStrut(20)); // Add spacing

            appPanel.add(createAccountButton); 

            frame.revalidate();  // Revalidate to ensure everything is laid out properly
            frame.repaint();  // Repaint to show the changes

            // Center the frame on the screen
            frame.setLocationRelativeTo(null);
        }
    });
}

    private byte[] getEncryptedData(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));  // Read encrypted file content as byte array
    }
    
    private void loadAccount(String filePath, JFrame frame) {
        try {
            // Load file content
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

            // Check if the content is plain text (ASCII)
            if (isASCII(fileContent)) {
                // Handle plain-text loading
                String text = new String(fileContent, StandardCharsets.UTF_8);
                String[] parts = text.split(":");

                if (parts.length >= 3) {
                    username = parts[0];
                    String communicationKey = parts[2];
                    String pubKey = hash.hashSHA512(hash.hashSHA512(communicationKey));

                    loggedInMenu(frame, username, "GPG Key Needed");
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid format in the file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            // Proceed to decryption UI
            frame.getContentPane().removeAll();
            frame.setSize(600, 700);
            frame.setLayout(new BorderLayout());

            // Header Panel
            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel headerLabel = new JLabel("Decrypt Account", SwingConstants.CENTER);
            headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            headerPanel.add(headerLabel);

            JLabel descriptionLabel = new JLabel(
                    "<html>\n"
                    + "Use the password that you have created when making the account<br>"
                    + "to unlock the account file. Also input the encryption method you<br>"
                    + "have used when creating your account file along with the password</html>",
                    SwingConstants.CENTER);
            descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            headerPanel.add(Box.createVerticalStrut(10));
            headerPanel.add(descriptionLabel);

            frame.add(headerPanel, BorderLayout.NORTH);

            // Center Panel
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(15, 15, 15, 15);

            // Password Field
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            centerPanel.add(passwordLabel, gbc);

            gbc.gridx = 2;
            gbc.gridy = 4;
            gbc.gridwidth = 3;
            JPasswordField passwordField = new JPasswordField(20);
            passwordField.setPreferredSize(new Dimension(250, 30));
            centerPanel.add(passwordField, gbc);

            // Device Selection
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            JLabel deviceLabel = new JLabel("Select Devices:");
            deviceLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            centerPanel.add(deviceLabel, gbc);

            JPanel devicePanel = new JPanel();
            devicePanel.setLayout(new GridLayout(0, 1, 10, 10));

            List<String> deviceNames = storageDevices.getStorageDeviceNames();
            Map<String, String> diskToSerialMap = new HashMap<>();
            for (String device : deviceNames) {
                diskToSerialMap.put(device, "Serial-" + device.hashCode());
            }

            List<String> selectedSerials = new ArrayList<>();
            for (String deviceName : deviceNames) {
                JToggleButton deviceToggleButton = new JToggleButton(deviceName);

                deviceToggleButton.addActionListener(e -> {
                    if (deviceToggleButton.isSelected()) {
                        selectedSerials.add(diskToSerialMap.get(deviceName));
                    } else {
                        selectedSerials.remove(diskToSerialMap.get(deviceName));
                    }
                });

                devicePanel.add(deviceToggleButton);
            }

            gbc.gridx = 2;
            gbc.gridy = 5;
            gbc.gridwidth = 3;
            centerPanel.add(devicePanel, gbc);

            // Encryption Methods
            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.gridwidth = 2;
            JLabel encryptionLabel = new JLabel("Encryption Methods:");
            encryptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            centerPanel.add(encryptionLabel, gbc);

            JPanel encryptionMethodsPanel = new JPanel();
            encryptionMethodsPanel.setLayout(new GridLayout(3, 2, 10, 10));
            JCheckBox aesCheckbox = new JCheckBox("AES");
            JCheckBox serpentCheckbox = new JCheckBox("Serpent");
            JCheckBox twofishCheckbox = new JCheckBox("Twofish");
            JCheckBox camelliaCheckbox = new JCheckBox("Camellia");
            JCheckBox kuzCheckbox = new JCheckBox("Kuznyechik");

            encryptionMethodsPanel.add(aesCheckbox);
            encryptionMethodsPanel.add(serpentCheckbox);
            encryptionMethodsPanel.add(twofishCheckbox);
            encryptionMethodsPanel.add(camelliaCheckbox);
            encryptionMethodsPanel.add(kuzCheckbox);

            gbc.gridx = 2;
            gbc.gridy = 6;
            gbc.gridwidth = 3;
            centerPanel.add(encryptionMethodsPanel, gbc);

            // Decryption Order
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.gridwidth = 2;
            JLabel orderLabel = new JLabel("Decryption Order:");
            orderLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            centerPanel.add(orderLabel, gbc);

            DefaultListModel<String> orderListModel = new DefaultListModel<>();
            JList<String> orderList = new JList<>(orderListModel);
            JScrollPane scrollPane = new JScrollPane(orderList);

            gbc.gridx = 2;
            gbc.gridy = 7;
            gbc.gridwidth = 3;
            centerPanel.add(scrollPane, gbc);

            ActionListener addToOrder = e -> {
                JCheckBox source = (JCheckBox) e.getSource();
                if (source.isSelected()) {
                    orderListModel.addElement(source.getText());
                } else {
                    orderListModel.removeElement(source.getText());
                }
            };

            aesCheckbox.addActionListener(addToOrder);
            serpentCheckbox.addActionListener(addToOrder);
            twofishCheckbox.addActionListener(addToOrder);
            camelliaCheckbox.addActionListener(addToOrder);
            kuzCheckbox.addActionListener(addToOrder);

            frame.add(centerPanel, BorderLayout.CENTER);

            // Footer Panel
            JPanel footerPanel = new JPanel();
            footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

            JButton backButton = new JButton("Back");
            backButton.addActionListener(e -> mainMenu(frame));

            JButton continueButton = new JButton("Continue");
            continueButton.addActionListener(e -> {
                try {
                    char[] pass = passwordField.getPassword();
                    if (pass.length == 0) {
                        JOptionPane.showMessageDialog(frame, "Password is required.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Hash the serial numbers, sort them, and pass them to the next function
                    hashedSerials = selectedSerials.stream()
                        .map(hash::hashSHA512)
                        .sorted()
                        .collect(Collectors.toList());
                    
                    // Hash the password
                    String passString = new String(pass);
                    byte[] passwordHash = hash.hashSHA256(passString);

                    // Create the list of keys (hashedSerials and passwordHash)
                    List<byte[]> keys = new ArrayList<>();
                    
                    keys.add(passwordHash);  // Add the password hash to the keys
                    if (hashedSerials != null) {
                        String serials = String.join(",", hashedSerials); // Combine serials into a single string
                        keys.add(hash.hashSHA256(serials));              // Hash the serials and add to the list
                    }

                    
                    // Reverse the selected encryption order for decryption
                    ArrayList<String> selectedOrder = new ArrayList<>();
                    for (int i = 0; i < orderListModel.size(); i++) {
                        selectedOrder.add(orderListModel.getElementAt(i));
                    }

                    // Decrypt the file using the keys and selected order
                    byte[] decryptedData = cipherData.decryptFileWithOrder(fileContent, keys, selectedOrder);

                    // Parse the decrypted data
                    String decryptedText = new String(decryptedData, StandardCharsets.UTF_8);
                    String[] parts = decryptedText.split(":");
                    if (parts.length >= 3) {
                        username = parts[0];
                        String communicationKey = parts[2];
                        String pubKey = hash.hashSHA512(hash.hashSHA512(communicationKey));

                        loggedInMenu(frame, username, "GPG Key Needed");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid decrypted file format.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });

            footerPanel.add(backButton);
            footerPanel.add(continueButton);
            frame.add(footerPanel, BorderLayout.SOUTH);

            frame.revalidate();
            frame.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper function to determine if file content is ASCII
    private boolean isASCII(byte[] data) {
        for (byte b : data) {
            if (b < 0 || b > 127) return false; // Outside ASCII range
        }
        return true;
    }

    public static void main(String[] args) {
    	//We run the program through EventQueue with EDT (Event Dispatch Thread) to make program stable.
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    App window = new App();
                    window.mainMenu(window.getJFrame());
                    window.getJFrame().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
