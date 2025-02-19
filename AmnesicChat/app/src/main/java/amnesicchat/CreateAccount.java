import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.Key;
import java.security.Security;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.util.stream.Collectors;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import org.bouncycastle.bcpg.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.*;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.text.SimpleDateFormat;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.util.Iterator;
import java.io.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.time.LocalDate;
import java.util.Date;


public class CreateAccount {
	
	static {
        Security.addProvider(new BouncyCastleProvider());
    }
	
	//Variables for GPG key
	public String name;
    public String email;
    public String passphrase;
    public String algorithm;
    public int keySize;
    public LocalDate expiry;
    public String exportKeys;
    public String comments;
    private String fingerprint;

    public boolean importKey = false;

	//Variables for Account Creation
    private static PGPPublicKey publicKey;  // Store the public key
    private boolean strictMode = false;
    private List<String> hashedSerials = new ArrayList<>();
    
    public List<String> getHashedSerials(){
    	return hashedSerials;
    }
    
    public String username = "";
    public String hashedCommunicationKey = null;
    
    //Get image
    public URL fileButtonIconURL = getClass().getResource("/images/File.png");
     
    //Access the Hash instance
    static Hash hash = CentralManager.getHash();
    
    //Access the App instance
    static App app = CentralManager.getApp();
    
    static StorageDevices storageDevices = CentralManager.getStorageDevices();

    static {
        if (storageDevices == null) {
            System.err.println("CentralManager.getStorageDevices() returned null. Initializing StorageDevices manually.");
            storageDevices = new StorageDevices();
        }
    }
    static CipherData cipherData = CentralManager.getCipherData();
    
    public void setupSuccess(JFrame frame, List<String> selected) {
		// Clear frame
		frame.getContentPane().removeAll();
        // Create the main panel to hold all components
	    frame.setSize(600, 400);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add title label
        JLabel titleLabel = new JLabel("Account Setup Successful!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        // Add a gap
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Add success message
        JLabel successMessage = new JLabel("Your account has now been fully set up!");
        successMessage.setFont(new Font("Arial", Font.PLAIN, 14));
        successMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(successMessage);

        // Add another gap
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Add summary label
        JLabel summaryLabel = new JLabel("Summary of Encryption Process to unlock your account fully:");
        summaryLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        summaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(summaryLabel);

        // Add another gap
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Add Continue button
        JButton continueButton = new JButton("Continue");
        continueButton.setFont(new Font("Arial", Font.PLAIN, 14));
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueButton.addActionListener(e -> {
        	app.loggedInMenu(frame, username, fingerprint);
        });
        panel.add(continueButton);

        // Add panel to frame
        frame.add(panel);
        frame.revalidate();
        frame.repaint();
    }

public void createPassword(JFrame frame) {
    // Clear frame
    frame.getContentPane().removeAll();
    frame.setSize(600, 800);
    frame.setLayout(new BorderLayout());

    // Header Panel
    JPanel headerPanel = new JPanel();
    headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
    headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // More space around the header

    JLabel headerLabel = new JLabel("Create Password", SwingConstants.CENTER);
    headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
    headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    headerPanel.add(headerLabel);

    JLabel descriptionLabel = new JLabel("<html>If you lose or forget the password, you will lose access to your account.<br>"
            + "Keep the password extremely safe! Nobody will help you to recover your account once you lose the password.</html>",
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
    gbc.insets = new Insets(15, 15, 15, 15);  // More spacing between elements

    // Username Label and Field
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    JLabel usernameLabel = new JLabel("Username:");
    usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    centerPanel.add(usernameLabel, gbc);

    JTextField usernameField = new JTextField(20);
    usernameField.setMaximumSize(new Dimension(300, 25));
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    centerPanel.add(usernameField, gbc);

    // Add a gap
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 5;
    centerPanel.add(Box.createVerticalStrut(10), gbc);

    // Description Label and TextArea
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    JLabel descriptionLabel2 = new JLabel("Description (Optional):");
    descriptionLabel2.setFont(new Font("SansSerif", Font.PLAIN, 14));
    centerPanel.add(descriptionLabel2, gbc);

    JTextArea descriptionArea = new JTextArea(5, 20);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    gbc.gridx = 2;
    gbc.gridy = 2;
    gbc.gridwidth = 3;
    centerPanel.add(descriptionArea, gbc);

    // Password Minimum Length Notice
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 5; // Span the full width
    JLabel minCharLabel = new JLabel("Password must not be empty.");
    minCharLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    minCharLabel.setForeground(Color.RED); // Optional: Make the notice more prominent
    centerPanel.add(minCharLabel, gbc);

    // Password Label
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = 2;
    JLabel passwordLabel = new JLabel("Password:");
    passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    centerPanel.add(passwordLabel, gbc);

    // Password Field
    gbc.gridx = 2;
    gbc.gridy = 4;
    gbc.gridwidth = 3;
    JPasswordField passwordField = new JPasswordField(20);
    passwordField.setPreferredSize(new Dimension(250, 30));  // Increase the width of the password field
    centerPanel.add(passwordField, gbc);

    // Encryption Methods
    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    JLabel encryptionLabel = new JLabel("Encryption Method(s):");
    encryptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    centerPanel.add(encryptionLabel, gbc);

    // Encryption Methods Panel with more spacing
    JPanel encryptionMethodsPanel = new JPanel();
    encryptionMethodsPanel.setLayout(new GridLayout(3, 2, 10, 10));  // Adjusted for better spacing
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
    gbc.gridy = 5;
    gbc.gridwidth = 3;
    centerPanel.add(encryptionMethodsPanel, gbc);

    // Encryption Order
    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.gridwidth = 2;
    JLabel orderLabel = new JLabel("Encryption Order:");
    orderLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    centerPanel.add(orderLabel, gbc);

    DefaultListModel<String> orderListModel = new DefaultListModel<>();
    JList<String> orderList = new JList<>(orderListModel);
    orderList.setVisibleRowCount(5);
    orderList.setFixedCellHeight(20);
    orderList.setFixedCellWidth(100);
    JScrollPane scrollPane = new JScrollPane(orderList);

    gbc.gridx = 2;
    gbc.gridy = 6;
    gbc.gridwidth = 3;
    centerPanel.add(scrollPane, gbc);

    // Add encryption methods to order list
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
    footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));  // Use FlowLayout.CENTER for horizontal centering
    footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Increased padding

    // Back Button
    JButton backButton = new JButton("Back");
    backButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
    backButton.addActionListener(e -> {
        selectSecurityModules(frame);  // Call selectSecurityModules() when Back button is clicked
    });

    // Continue Button
    JButton continueButton = new JButton("Continue");
    continueButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
    continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);  // Ensuring button is centered
continueButton.addActionListener(e -> {
    char[] pass = passwordField.getPassword();
    String passString = new String(pass);

    byte[] passwordHash = hash.hashSHA256(passString);
    username = usernameField.getText();
    String communicationKey = cipherData.generateRandomKey();
    String description = descriptionArea.getText();

    if (username.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Username cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
    } else if (pass.length == 0) {
        JOptionPane.showMessageDialog(frame, "Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
    } else if (orderListModel.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Select at least one encryption method!", "Error", JOptionPane.ERROR_MESSAGE);
    } else if (publicKey == null) { // Check if publicKey is already set
        JOptionPane.showMessageDialog(frame, "Error: Public key is not loaded!", "Encryption Error", JOptionPane.ERROR_MESSAGE);
    } else {
        try {
            System.out.println("Public Key: " + (publicKey != null ? "Loaded" : "NULL"));

            String content = username + ":" + description + ":" + communicationKey + ":" + fingerprint;

            File keyFile = new File(System.getProperty("user.home") + File.separator + "communication_key.txt");
            try (FileWriter writer = new FileWriter(keyFile)) {
                writer.write(content);
            }

            List<byte[]> keys = new ArrayList<>();
            if (hashedSerials != null) {
                String serials = String.join(",", hashedSerials);
                keys.add(hash.hashSHA256(serials));
            }
            keys.add(passwordHash);

            ArrayList<String> encryptionOrder = new ArrayList<>();
            for (int i = 0; i < orderListModel.size(); i++) {
                encryptionOrder.add(orderListModel.getElementAt(i));
            }

            byte[] finalEncryptedData;
            if (!strictMode) {
                byte[] encryptedData = cipherData.encryptFileWithOrder(keyFile, keys, encryptionOrder);
                finalEncryptedData = GPGKeyGenerator.encryptWithPublicKey(encryptedData, publicKey);
            } else {
                byte[] gpgEncryptedData = GPGKeyGenerator.encryptWithPublicKey(Files.readAllBytes(keyFile.toPath()), publicKey);
                File tempFile = File.createTempFile("gpg_encrypted", ".tmp");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(gpgEncryptedData);
                }
                finalEncryptedData = cipherData.encryptFileWithOrder(tempFile, keys, encryptionOrder);
                tempFile.deleteOnExit();
            }

            File encryptedFile = new File(System.getProperty("user.home") + File.separator + "communication_key_encrypted.txt");
            try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
                fos.write(finalEncryptedData);
            }

            JOptionPane.showMessageDialog(frame, "File created and encrypted successfully:\n" + encryptedFile.getAbsolutePath(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            setupSuccess(frame, null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
});

    // Add buttons to footer panel (Back and Continue)
    footerPanel.add(backButton);
    footerPanel.add(Box.createHorizontalStrut(10)); // Spacer between buttons
    footerPanel.add(continueButton);

    frame.add(footerPanel, BorderLayout.SOUTH);

    frame.revalidate();
    frame.repaint();
}

public static byte[] encryptWithPublicKey(byte[] data, PGPPublicKey publicKey) throws Exception {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ArmoredOutputStream armoredOut = new ArmoredOutputStream(byteOut);
    
    BcPGPDataEncryptorBuilder encryptor = new BcPGPDataEncryptorBuilder(PGPEncryptedData.AES_256);
    encryptor.setWithIntegrityPacket(true);
    encryptor.setSecureRandom(new SecureRandom());

    PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(encryptor);
    encryptedDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicKey));

    OutputStream encryptedOut = encryptedDataGenerator.open(armoredOut, new byte[4096]);
    encryptedOut.write(data);
    encryptedOut.close();
    armoredOut.close();

    return byteOut.toByteArray();
}

public JPanel modulePanel; // Holds each individual module
public JPanel moduleListPanel; // Holds the list of module panels
public int MODULES_PER_PAGE = 3; // Number of modules to display per page
public int currentPage = 1; // Tracks the current page
public List<String> selectedSecurityMethods = new ArrayList<>(); // Shows the path of how to decrypt account

public void selectSecurityModules(JFrame frame) {
frame.getContentPane().removeAll();
frame.setSize(600, 550);
frame.setLayout(new BorderLayout());

// Header Panel
JPanel headerPanel = new JPanel();
headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Add margin around the header panel

JLabel headerLabel = new JLabel("Account Protection Modules", SwingConstants.CENTER);
headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
headerPanel.add(headerLabel);

headerPanel.add(Box.createVerticalStrut(10));

// Description panel
JLabel descriptionLabel = new JLabel(
        "<html>If you have modules imported in the module folder, you may load them to further secure your account. If not, you may continue.</html>",
        SwingConstants.CENTER);
descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
headerPanel.add(descriptionLabel);

frame.add(headerPanel, BorderLayout.NORTH);

// Module List Panel
moduleListPanel = new JPanel();
moduleListPanel.setLayout(new GridLayout(MODULES_PER_PAGE, 1, 10, 10));
moduleListPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Add margin around the module panel
JScrollPane moduleScrollPane = new JScrollPane(moduleListPanel);
moduleScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove scrollpane border for cleaner look
frame.add(moduleScrollPane, BorderLayout.CENTER);

// Footer Panel with Navigation Buttons
JPanel footerPanel = new JPanel();
footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Add margin around the footer panel

JPanel navigationPanel = new JPanel();
JButton previousButton = new JButton("PREVIOUS");
JButton nextButton = new JButton("NEXT");
JLabel pageLabel = new JLabel("PAGE 1/2", SwingConstants.CENTER);
pageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

previousButton.addActionListener(e -> navigateModules(-1, pageLabel));
nextButton.addActionListener(e -> navigateModules(1, pageLabel));

navigationPanel.add(previousButton);
navigationPanel.add(pageLabel);
navigationPanel.add(nextButton);

JButton continueButton = new JButton("Continue");
continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
continueButton.addActionListener(e -> createPassword(frame));

JButton backButton = new JButton("Back");
backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
backButton.addActionListener(e -> {
    if (importKey) {
        insertGPGIdentity(frame);
    } else {
        secondGPGIdentity(frame);
    }
});

footerPanel.add(navigationPanel);
footerPanel.add(Box.createVerticalStrut(10));
footerPanel.add(continueButton); // Add Continue button first
footerPanel.add(Box.createVerticalStrut(5));
footerPanel.add(backButton); // Add Back button below Continue
frame.add(footerPanel, BorderLayout.SOUTH);

// Load Initial Modules
loadModules();
}

private void navigateModules(int direction, JLabel pageLabel) {
int totalPages = 2; // Example total pages
currentPage += direction;

if (currentPage < 1) {
    currentPage = 1;
} else if (currentPage > totalPages) {
    currentPage = totalPages;
}

pageLabel.setText("PAGE " + currentPage + "/" + totalPages);
loadModules();
}

private void loadModules() {
moduleListPanel.removeAll();

// Folder containing module files
File moduleFolder = new File("src/main/resources/modules");
if (!moduleFolder.exists() || !moduleFolder.isDirectory()) {
    JLabel errorLabel = new JLabel("No modules folder found.");
    moduleListPanel.add(errorLabel);
    moduleListPanel.revalidate();
    moduleListPanel.repaint();
    return;
}

moduleListPanel.revalidate();
moduleListPanel.repaint();
}

public void secondGPGIdentity(JFrame frame) {
    SwingUtilities.invokeLater(() -> {
        frame.setTitle("AmnesicChat - Create GPG Identity");
        frame.setSize(700, 450);
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        frame.add(mainPanel, BorderLayout.CENTER);

        JLabel headerLabel = new JLabel("Create GPG Identity", SwingConstants.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(headerLabel);

        mainPanel.add(Box.createVerticalStrut(10));

        JLabel subHeaderLabel = new JLabel(
                "<html>It is recommended to use a pseudo identity. Do not use your real identity unless necessary.<br>Hover over the text boxes and tooltip for more.</html>",
                SwingConstants.CENTER);
        subHeaderLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subHeaderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subHeaderLabel);

        mainPanel.add(Box.createVerticalStrut(20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Algorithm Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Algorithm:"), gbc);
        gbc.gridx = 1;
        String[] algorithms = {"RSA", "ECC", "DSA"};
        JComboBox<String> algorithmComboBox = new JComboBox<>(algorithms);
        formPanel.add(algorithmComboBox, gbc);

        // Key Size Selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Key Size:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> keySizeComboBox = new JComboBox<>();
        formPanel.add(keySizeComboBox, gbc);

        algorithmComboBox.addActionListener(e -> {
    keySizeComboBox.removeAllItems();
    switch ((String) algorithmComboBox.getSelectedItem()) {
        case "RSA" -> {
            keySizeComboBox.addItem("2048");
            keySizeComboBox.addItem("3072");
            keySizeComboBox.addItem("4096");
        }
        case "ECC" -> { // Use named curves instead of bit sizes
            keySizeComboBox.addItem("Curve25519"); 
            keySizeComboBox.addItem("NIST P-256"); 
            keySizeComboBox.addItem("NIST P-384"); 
            keySizeComboBox.addItem("NIST P-521"); 
            keySizeComboBox.addItem("BrainpoolP256r1");
            keySizeComboBox.addItem("BrainpoolP384r1");
            keySizeComboBox.addItem("BrainpoolP512r1");
        }
        case "DSA" -> {
            keySizeComboBox.addItem("1024");
            keySizeComboBox.addItem("2048");
            keySizeComboBox.addItem("3072");
        }
    }
});
        // Export Keys Selection
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Export Keys?"), gbc);
        gbc.gridx = 1;
        JToggleButton bothButton = new JToggleButton("BOTH");
        JToggleButton secretOnlyButton = new JToggleButton("SECRET ONLY");
        JToggleButton publicOnlyButton = new JToggleButton("PUBLIC ONLY");
        JToggleButton noneButton = new JToggleButton("NONE");

        ButtonGroup exportButtonGroup = new ButtonGroup();
        exportButtonGroup.add(bothButton);
        exportButtonGroup.add(secretOnlyButton);
        exportButtonGroup.add(publicOnlyButton);
        exportButtonGroup.add(noneButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 10, 0));
        buttonPanel.add(bothButton);
        buttonPanel.add(secretOnlyButton);
        buttonPanel.add(publicOnlyButton);
        buttonPanel.add(noneButton);
        formPanel.add(buttonPanel, gbc);

        mainPanel.add(formPanel);

        // Continue Button
        JButton continueButton = new JButton("Generate GPG Key");
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
continueButton.addActionListener(e -> {
    String algorithm = (String) algorithmComboBox.getSelectedItem();
    String keySizeStr = (String) keySizeComboBox.getSelectedItem();

    String exportOption = bothButton.isSelected() ? "BOTH"
            : secretOnlyButton.isSelected() ? "SECRET ONLY"
            : publicOnlyButton.isSelected() ? "PUBLIC ONLY"
            : noneButton.isSelected() ? "NONE" : null;

    if (algorithm == null || keySizeStr == null || exportOption == null) {
        JOptionPane.showMessageDialog(frame, "Please select algorithm, key size, and export option.", "Error",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        String keyParameter = keySizeStr; // Use named curves for ECC adaptation

        System.out.println("Generating GPG Key...");

        // Generate GPG Key
        fingerprint = GPGKeyGenerator.generateGPGKey(name, email, passphrase, expiry, algorithm, keyParameter, "");
        File generatedPublicKeyFile = GPGKeyGenerator.publicKeyFile;
        File generatedPrivateKeyFile = GPGKeyGenerator.privateKeyFile;

        if (generatedPublicKeyFile == null || !generatedPublicKeyFile.exists() || generatedPublicKeyFile.length() == 0) {
            throw new IOException("Public key file was not generated correctly.");
        }

        publicKey = loadPGPPublicKey(generatedPublicKeyFile);
        System.out.println("Public key successfully generated!");

        // Now ask for a save location
        File selectedDir = null;
        if (!"NONE".equals(exportOption)) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Directory to Save Keys");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                selectedDir = fileChooser.getSelectedFile();
                File savedPublicKeyFile = new File(selectedDir, "publicKey.asc");
                File savedPrivateKeyFile = new File(selectedDir, "privateKey.asc");

                // Copy generated keys to selected directory
                Files.copy(generatedPublicKeyFile.toPath(), savedPublicKeyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if ("BOTH".equals(exportOption) || "SECRET ONLY".equals(exportOption)) {
                    Files.copy(generatedPrivateKeyFile.toPath(), savedPrivateKeyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                System.out.println("Keys saved to: " + selectedDir.getAbsolutePath());
            } else {
                System.out.println("Key export canceled by user.");
            }
        }

        JOptionPane.showMessageDialog(frame, "GPG Key successfully created!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
        importKey = false;
        selectSecurityModules(frame);

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(frame, "Error while generating GPG key: " + ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
});

        // Back Button
        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> createGPGIdentity(frame));

        mainPanel.add(continueButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(backButton);

        frame.add(mainPanel);
        frame.revalidate();
        frame.repaint();
    });
}

private PGPPublicKey loadPGPPublicKey(File publicKeyFile) throws IOException, PGPException {
    Security.addProvider(new BouncyCastleProvider());

    // Validate existence and non-empty file
    if (!publicKeyFile.exists()) {
        throw new FileNotFoundException("Public key file not found at: " + publicKeyFile.getAbsolutePath());
    }
    if (publicKeyFile.length() == 0) {
        throw new IOException("Public key file is empty: " + publicKeyFile.getAbsolutePath());
    }

    System.out.println("Loading public key from: " + publicKeyFile.getAbsolutePath());

    try (InputStream keyIn = new BufferedInputStream(new FileInputStream(publicKeyFile))) {
        PGPObjectFactory pgpFactory = new PGPObjectFactory(PGPUtil.getDecoderStream(keyIn), new BcKeyFingerprintCalculator());
        Object obj;

        while ((obj = pgpFactory.nextObject()) != null) {
            System.out.println("PGP Object Found: " + obj.getClass().getSimpleName());

            if (obj instanceof PGPPublicKeyRing) {
                PGPPublicKeyRing keyRing = (PGPPublicKeyRing) obj;
                for (PGPPublicKey key : keyRing) {
                    System.out.println("Found Key ID: " + Long.toHexString(key.getKeyID()));
                    if (key.isEncryptionKey()) {
                        System.out.println("Successfully loaded public key!");
                        return key;
                    }
                }
            }
        }
    }

    throw new PGPException("No valid public key found in the file.");
}

// Helper Method to Copy Files
private void copyFile(File source, File destination) throws IOException {
    if (source.exists() && source.isFile()) {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    } else {
        throw new IOException("Source file not found: " + source.getAbsolutePath());
    }
}

public void createGPGIdentity(JFrame frame) {
    SwingUtilities.invokeLater(() -> {
        frame.setTitle("AmnesicChat - Create GPG Identity");
        frame.setSize(700, 450);
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        frame.add(mainPanel, BorderLayout.CENTER);

        JLabel headerLabel = new JLabel("Create GPG Identity", SwingConstants.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        JLabel subHeaderLabel = new JLabel(
                "<html>It is recommended to use a pseudo identity. Do not use your real identity unless necessary.<br>" +
                        "Hover over the text boxes and tooltip for more.</html>",
                SwingConstants.CENTER);
        subHeaderLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subHeaderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subHeaderLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setToolTipText("Enter your preferred name. Must be at least 4 characters.");
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("E-mail:");
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        formPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Passphrase:");
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        JPasswordField passphraseField = new JPasswordField(20);
        formPanel.add(passphraseField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel expiryLabel = new JLabel("Expiry:");
        formPanel.add(expiryLabel, gbc);
        gbc.gridx = 1;
        JTextField expiryField = new JTextField(20);
        expiryField.setText("DD-MM-YYYY");
        formPanel.add(expiryField, gbc);

        gbc.gridx = 2;
        JButton infoButton = new JButton("i");
        formPanel.add(infoButton, gbc);

        mainPanel.add(formPanel);

        JPanel warningPanel = new JPanel();
        warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.Y_AXIS));
        mainPanel.add(warningPanel);

        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        mainPanel.add(buttonPanel);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> insertGPGIdentity(frame));
        buttonPanel.add(backButton);

        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(e -> {
            warningPanel.removeAll();
            boolean hasErrors = false;
            boolean hasWarnings = false;
            StringBuilder warnings = new StringBuilder();

            String enteredName = nameField.getText().trim();
            String enteredEmail = emailField.getText().trim();
            char[] passwordChars = passphraseField.getPassword();
            String enteredPassphrase = new String(passwordChars);
            String enteredExpiry = expiryField.getText().trim();

            if (enteredName.length() < 4) {
                JLabel errorLabel = new JLabel("Name must have at least 4 characters.");
                errorLabel.setForeground(Color.RED);
                warningPanel.add(errorLabel);
                hasErrors = true;
            }

            if (!enteredEmail.matches(".+@.+")) {
                JLabel errorLabel = new JLabel("Email must be valid (e.g., name@example.com).");
                errorLabel.setForeground(Color.RED);
                warningPanel.add(errorLabel);
                hasErrors = true;
            }

            if (passwordChars.length == 0) {
                warnings.append("- No passphrase set. This is not recommended for security.\n");
                hasWarnings = true;
            } else if (passwordChars.length < 8) {
                warnings.append("- Passphrase is weak (less than 8 characters).\n");
                hasWarnings = true;
            }

            LocalDate expiryDate = null;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                expiryDate = LocalDate.parse(enteredExpiry, formatter);
                LocalDate today = LocalDate.now();
                long daysBetween = ChronoUnit.DAYS.between(today, expiryDate);

                if (!expiryDate.isAfter(today)) {
                    JLabel errorLabel = new JLabel("Expiry date must be in the future.");
                    errorLabel.setForeground(Color.RED);
                    warningPanel.add(errorLabel);
                    hasErrors = true;
                } else if (daysBetween < 90) {
                    warnings.append("- Expiry date is less than 90 days from now.\n");
                    hasWarnings = true;
                }
            } catch (DateTimeParseException ex) {
                JLabel errorLabel = new JLabel("Expiry date must be valid and in the format DD-MM-YYYY.");
                errorLabel.setForeground(Color.RED);
                warningPanel.add(errorLabel);
                hasErrors = true;
            }

            warningPanel.revalidate();
            warningPanel.repaint();

            if (hasErrors) {
                JOptionPane.showMessageDialog(frame, "Please correct the highlighted errors.", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
            } else if (hasWarnings) {
                int choice = JOptionPane.showOptionDialog(frame,
                        "Warnings:\n" + warnings.toString(),
                        "Warnings",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        new String[]{"Back", "Continue"},
                        "Back");
                if (choice == JOptionPane.NO_OPTION) {
                    name = enteredName;
                    email = enteredEmail;
                    passphrase = enteredPassphrase;
                    expiry = expiryDate;
                    secondGPGIdentity(frame);
                }
            } else {
            	name = enteredName;
                email = enteredEmail;
                passphrase = enteredPassphrase;
                expiry = expiryDate;
                secondGPGIdentity(frame);
            }
        });
        buttonPanel.add(continueButton);

        frame.add(mainPanel);
        frame.revalidate();
        frame.repaint();
    });
}

public void loadGPGKey(JFrame frame, File gpgKeyFile) {
    // Prepare the frame
    frame.getContentPane().removeAll();
    frame.setTitle("GPG Identity Loaded");
    frame.setLayout(new BorderLayout());

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JLabel headerLabel = new JLabel("GPG Identity Loaded", SwingConstants.CENTER);
    headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
    headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    contentPanel.add(headerLabel);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

    // Button Panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

    JButton returnButton = new JButton("Return");
    JButton continueButton = new JButton("Continue");

    returnButton.addActionListener(e -> insertGPGIdentity(frame));
    continueButton.addActionListener(e -> {
        if (publicKey == null) {
            showError(frame, contentPanel, buttonPanel, "Public key could not be extracted from the imported private key.");
            return;
        }
        JOptionPane.showMessageDialog(frame, "Public key successfully extracted!", "Success", JOptionPane.INFORMATION_MESSAGE);
        importKey = true;
        selectSecurityModules(frame);
    });

    buttonPanel.add(returnButton);
    buttonPanel.add(continueButton);

    boolean success = false; // Track if key loading succeeds

    if (gpgKeyFile != null && gpgKeyFile.exists()) {
        try (InputStream keyIn = new FileInputStream(gpgKeyFile)) {
            String keyContent = new String(keyIn.readAllBytes(), StandardCharsets.UTF_8);

            if (!keyContent.contains("-----BEGIN PGP PRIVATE KEY BLOCK-----")) {
                showError(frame, contentPanel, buttonPanel, "The file does not contain a valid PGP private key block.");
                return;
            }

            // Reopen file for proper reading
            try (InputStream decodedStream = PGPUtil.getDecoderStream(new FileInputStream(gpgKeyFile))) {
                List<PGPSecretKeyRing> keyRingList = new ArrayList<>();
                PGPObjectFactory pgpFact = new PGPObjectFactory(decodedStream, new JcaKeyFingerprintCalculator());
                Object obj;

                while ((obj = pgpFact.nextObject()) != null) {
                    if (obj instanceof PGPSecretKeyRing) {
                        keyRingList.add((PGPSecretKeyRing) obj);
                    }
                }

                if (keyRingList.isEmpty()) {
                    showError(frame, contentPanel, buttonPanel, "No valid PGP secret key found in the file.");
                    return;
                }

                PGPSecretKeyRingCollection keyRings = new PGPSecretKeyRingCollection(keyRingList);
                Iterator<PGPSecretKeyRing> ringIterator = keyRings.getKeyRings();

                while (ringIterator.hasNext()) {
                    PGPSecretKeyRing keyRing = ringIterator.next();
                    PGPSecretKey secretKey = keyRing.getSecretKey();
                    if (secretKey == null) continue;

                    // Extract and store the public key
                    PGPPublicKey extractedPublicKey = secretKey.getPublicKey();
                    if (extractedPublicKey != null) {
                        publicKey = extractedPublicKey;
                    } else {
                        showError(frame, contentPanel, buttonPanel, "Failed to extract the public key.");
                        return;
                    }

                    boolean isEncrypted = true;
                    try {
                        secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().build(new char[0]));
                        isEncrypted = false;
                    } catch (PGPException ignored) {}

                    if (isEncrypted) {
                        String passphrase = requestPassphrase(frame);
                        if (passphrase == null) {
                            showError(frame, contentPanel, buttonPanel, "Passphrase required to decrypt the private key.");
                            return;
                        }

                        PGPPrivateKey privateKey = decryptPrivateKey(secretKey, passphrase.toCharArray());
                        if (privateKey == null) {
                            showError(frame, contentPanel, buttonPanel, "Failed to decrypt the private key.");
                            return;
                        }
                    }

                    // **Extract and store the fingerprint**
                    fingerprint = getKeyFingerprint(secretKey);

                    // Display key info
                    contentPanel.add(createInfoLabel("Name: " + getKeyUserID(secretKey)));
                    contentPanel.add(createInfoLabel("Email: " + getKeyUserEmail(secretKey)));
                    contentPanel.add(createInfoLabel("Expiry: " + getKeyExpiryDate(secretKey)));
                    contentPanel.add(createInfoLabel("Encryption: " + getEncryptionAlgorithm(secretKey)));
                    contentPanel.add(createInfoLabel("Fingerprint: " + fingerprint));

                    success = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError(frame, contentPanel, buttonPanel, "Error reading GPG key file: " + e.getMessage());
            return;
        }
    } else {
        showError(frame, contentPanel, buttonPanel, "GPG key file not found.");
        return;
    }

    // If successful, add buttons below key info
    if (success) {
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(buttonPanel);
    }

    // Refresh UI
    frame.add(contentPanel, BorderLayout.CENTER);
    frame.revalidate();
    frame.repaint();
}

private String getKeyFingerprint(PGPSecretKey secretKey) {
    byte[] fingerprintBytes = secretKey.getPublicKey().getFingerprint();
    StringBuilder fingerprintBuilder = new StringBuilder();
    for (byte b : fingerprintBytes) {
        fingerprintBuilder.append(String.format("%02X", b)); // Convert to hex format
    }
    return fingerprintBuilder.toString();
}

/**
 * Show error message and reset UI.
 */
private void showError(JFrame frame, JPanel contentPanel, JPanel buttonPanel, String message) {
    JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    contentPanel.removeAll();
    contentPanel.add(new JLabel(message, SwingConstants.CENTER));
    contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    contentPanel.add(buttonPanel);  // Ensure buttons are still accessible

    frame.add(contentPanel, BorderLayout.CENTER);
    frame.revalidate();
    frame.repaint();
}


private static String requestPassphrase(JFrame frame) {
    // Prompt the user to enter a passphrase
    JPasswordField passwordField = new JPasswordField(20);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JLabel("Enter passphrase for the private key:"), BorderLayout.NORTH);
    panel.add(passwordField, BorderLayout.CENTER);

    int option = JOptionPane.showConfirmDialog(frame, panel, "Enter Passphrase", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (option == JOptionPane.OK_OPTION) {
        return new String(passwordField.getPassword());
    }
    return null; // Return null if the user cancels the input
}

private static PGPPrivateKey decryptPrivateKey(PGPSecretKey secretKey, char[] passphrase) throws Exception {
    PGPPrivateKey privateKey = null;
    try {
        // Attempt to decrypt the private key using the passphrase
        privateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().build(passphrase));
    } catch (PGPException e) {
        System.out.println("Error decrypting private key: " + e.getMessage());
        // Print more detailed exception information to help diagnose the problem
        e.printStackTrace();
    }

    // If the private key is null after decryption, it means the decryption failed
    if (privateKey == null) {
        System.out.println("Decryption failed, the passphrase may be incorrect or the key is corrupted.");
    }

    return privateKey;
}

private static JLabel createInfoLabel(String text) {
    JLabel label = new JLabel(text, SwingConstants.CENTER);
    label.setFont(new Font("SansSerif", Font.PLAIN, 14));
    label.setAlignmentX(Component.CENTER_ALIGNMENT);
    return label;
}

private static String getKeyUserID(PGPSecretKey secretKey) {
    Iterator<String> userIDs = secretKey.getUserIDs();
    return userIDs.hasNext() ? userIDs.next() : "Unknown";
}

private static String getKeyUserEmail(PGPSecretKey secretKey) {
    Iterator<String> userIDs = secretKey.getUserIDs();
    while (userIDs.hasNext()) {
        String userID = userIDs.next();
        if (userID.contains("<")) {
            int startIndex = userID.indexOf("<") + 1;
            int endIndex = userID.indexOf(">");
            return userID.substring(startIndex, endIndex);
        }
    }
    return "Unknown";
}

private static String getKeyExpiryDate(PGPSecretKey secretKey) {
    PGPPublicKey publicKey = secretKey.getPublicKey();
    long validSeconds = publicKey.getValidSeconds();

    // If validSeconds is 0, it means the key does not expire
    if (validSeconds == 0) {
        return "No Expiry";  
    }

    // Calculate expiry time
    long expiryTimeMillis = publicKey.getCreationTime().getTime() + (validSeconds * 1000L);
    Date expiryDate = new Date(expiryTimeMillis);

    // Format expiry date properly
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return dateFormat.format(expiryDate);
}

private static String getEncryptionAlgorithm(PGPSecretKey secretKey) {
    PGPPublicKey publicKey = secretKey.getPublicKey();
    int algorithm = publicKey.getAlgorithm();

    switch (algorithm) {
        case PGPPublicKey.RSA_GENERAL:
            return "RSA (General)";
        case PGPPublicKey.RSA_ENCRYPT:
            return "RSA (Encryption)";
        case PGPPublicKey.RSA_SIGN:
            return "RSA (Signing)";
        case PGPPublicKey.DSA:
            return "DSA";
        case PGPPublicKey.ELGAMAL_ENCRYPT:
            return "ElGamal (Encryption)";
        case PGPPublicKey.ELGAMAL_GENERAL:
            return "ElGamal (General)";
        case PGPPublicKey.ECDH:
            return "ECDH (" + getECCurveName(publicKey) + ")";
        case PGPPublicKey.ECDSA:
            return "ECDSA (" + getECCurveName(publicKey) + ")";
        default:
            return "Unknown Algorithm (ID: " + algorithm + ")";
    }
}

private static String getECCurveName(PGPPublicKey publicKey) {
    int algorithm = publicKey.getAlgorithm();

    // Common OpenPGP ECC curves
    switch (algorithm) {
        case PGPPublicKey.ECDH:
        case PGPPublicKey.ECDSA:
            int keySize = publicKey.getBitStrength();
            if (keySize == 256) return "Curve25519 / NIST P-256";
            if (keySize == 384) return "NIST P-384";
            if (keySize == 521) return "NIST P-521";
            return "Unknown ECC Curve (Key size: " + keySize + " bits)";
        default:
            return "Not an ECC Key";
    }
}

    public void insertGPGIdentity(JFrame frame) {
	    SwingUtilities.invokeLater(() -> {
	        frame.getContentPane().removeAll();
	        frame.setTitle("AmnesicChat - GPG Identity");
	        frame.setSize(650, 350);

	        // Main panel setup
	        JPanel mainPanel = new JPanel();
	        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

	        // Header label
	        JLabel headerLabel = new JLabel("Create GPG Identity", SwingConstants.CENTER);
	        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
	        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	        mainPanel.add(headerLabel);

	        mainPanel.add(Box.createVerticalStrut(20)); // Add spacing

	        // Instruction label
	        JLabel instructionLabel = new JLabel("Would you like to import your own GPG key?");
	        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
	        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	        mainPanel.add(instructionLabel);

	        JLabel instructionSubLabel = new JLabel(
	                "If yes, please locate the private key using the directory finder below.",
	                SwingConstants.CENTER);
	        instructionSubLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
	        instructionSubLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	        mainPanel.add(instructionSubLabel);

	        mainPanel.add(Box.createVerticalStrut(20)); // Add spacing

	        // File chooser panel
	        JPanel fileChooserPanel = new JPanel();
	        fileChooserPanel.setLayout(new BoxLayout(fileChooserPanel, BoxLayout.X_AXIS));

	        JTextField filePathField = new JTextField();
	        JButton loadKeyButton = new JButton("..."); // Initially "..." for browsing
	        loadKeyButton.setPreferredSize(new Dimension(80, 30));

	        // Load the icon for the "..." button
	        if (fileButtonIconURL != null) {
	        	 ImageIcon originalIcon = new ImageIcon(fileButtonIconURL);
                 Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH); // Resize image to 50x50
                 ImageIcon resizedIcon = new ImageIcon(scaledImage);
	        	
	            loadKeyButton.setIcon(resizedIcon);
	            loadKeyButton.setText(""); // Clear text if icon is set
	        }

	        filePathField.setMaximumSize(new Dimension(Integer.MAX_VALUE, loadKeyButton.getPreferredSize().height));

	        fileChooserPanel.add(filePathField);
	        fileChooserPanel.add(Box.createHorizontalStrut(10)); // Add spacing
	        fileChooserPanel.add(loadKeyButton);

	     // Create button
	        JButton createButton = new JButton("Create my own key");
	        createButton.setPreferredSize(new Dimension(200, 40));
	        createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	        createButton.addActionListener(e -> {
	            String filePath = filePathField.getText();
	            if (filePath.isEmpty()) {
	                createGPGIdentity(frame); // Create a new GPG identity
	            } else {
	                if (validateGPGKey(filePath)) {
	                    loadGPGKey(frame, new File(filePath)); // Pass the file path as a File object
	                } else {
	                    JOptionPane.showMessageDialog(frame, "Invalid GPG key! Please select a valid secret key.",
	                            "Error", JOptionPane.ERROR_MESSAGE);
	                }
	            }
	        });
	        
	        // Change button text to "Load Key" when typing in the text field
	        filePathField.getDocument().addDocumentListener(new DocumentListener() {
	            @Override
	            public void insertUpdate(DocumentEvent e) {
	                updateButtonText();
	            }

	            @Override
	            public void removeUpdate(DocumentEvent e) {
	                updateButtonText();
	            }

	            @Override
	            public void changedUpdate(DocumentEvent e) {
	                updateButtonText();
	            }

	            private void updateButtonText() {
	                createButton.setText(filePathField.getText().isEmpty() ? "Create my own key" : "Load Key");
	            }
	        });

	        // File chooser action listener
	        loadKeyButton.addActionListener(e -> {
	            String filePath = filePathField.getText();
	            if (filePath.isEmpty()) {
	                JFileChooser fileChooser = new JFileChooser();
	                fileChooser.setFileFilter(new FileNameExtensionFilter("PGP/GPG Files", "asc")); // Restrict to .asc files
	                int result = fileChooser.showOpenDialog(frame);
	                if (result == JFileChooser.APPROVE_OPTION) {
	                    filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
	                }
	            } else {
	                if (validateGPGKey(filePath)) {
	                    loadGPGKey(frame, new File(filePath)); // Pass the file path as a File object
	                } else {
	                    JOptionPane.showMessageDialog(frame, "Invalid GPG key! Please select a valid secret key.",
	                            "Error", JOptionPane.ERROR_MESSAGE);
	                }
	            }
	        });

	        mainPanel.add(fileChooserPanel);

	        mainPanel.add(Box.createVerticalStrut(20)); // Add spacing
	        
	        mainPanel.add(createButton);

	        // Back button
	        JButton backButton = new JButton("Back");
	        backButton.setPreferredSize(new Dimension(200, 40));
	        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	        backButton.addActionListener(e -> createAccount(frame));
	        mainPanel.add(Box.createVerticalStrut(10)); // Add spacing between buttons
	        mainPanel.add(backButton);

	        frame.add(mainPanel);
	        frame.revalidate();
	        frame.repaint();
	    });
	}

	public boolean validateGPGKey(String filePath) {
	    return true;
	}

	
    public void createAccount(JFrame frame) {
        SwingUtilities.invokeLater(() -> {
            frame.setTitle("AmnesicChat - Create Account");
            frame.setSize(650, 450);

            // Fetch available storage devices using OSHI
            if (storageDevices == null) {
                System.err.println("Error: storageDevices is null. Cannot fetch storage device names.");
                JOptionPane.showMessageDialog(frame, "No storage devices found. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<String> deviceNames = storageDevices.getStorageDeviceNames();

            // Mock serial numbers for each device
            List<String> serialNumbers = deviceNames.stream()
                    .map(device -> "Serial-" + device.hashCode())
                    .collect(Collectors.toList());

            // A map to associate disk names with their serial numbers
            Map<String, String> diskToSerialMap = new HashMap<>();
            for (int i = 0; i < deviceNames.size(); i++) {
                diskToSerialMap.put(deviceNames.get(i), serialNumbers.get(i));
            }

            frame.getContentPane().removeAll();

            // Main panel
            JPanel createAccountPanel = new JPanel();
            createAccountPanel.setLayout(new BoxLayout(createAccountPanel, BoxLayout.Y_AXIS));
            createAccountPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Header and instruction labels
            JLabel headerLabel = new JLabel("Create Device Lock", SwingConstants.CENTER);
            headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
            headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            createAccountPanel.add(headerLabel);

            createAccountPanel.add(Box.createVerticalStrut(10)); // Spacing
            
            // Instruction label
            JLabel instructionLabel = new JLabel(
            		"<html>Choose which storage devices you want to use as verification.<br>"
                            + "The storage devices selected are required to unlock your account.<br>"
                            + "It is not recommended to use your USB as the only device lock.</html>",
                    SwingConstants.CENTER);
            instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            createAccountPanel.add(instructionLabel);
            createAccountPanel.add(Box.createVerticalStrut(10));
            
            // Warning message
            JLabel warningLabel = new JLabel(
        "<html><i>CHOOSE YOUR ENCRYPTION MODE CAREFULLY.<br>"
                + "BOTH OPTIONS PROVIDE SECURITY AGAINST HACKERS,<br>"
                + "BUT THE ORDER OF ENCRYPTION DIFFERS.<br><br>"
                + "• <b>GPG → Encryption:</b> Ensures long-term integrity and authenticity first.<br>"
                + "• <b>Encryption → GPG:</b> Prioritizes faster encryption before signing.<br></i></html>",
        SwingConstants.CENTER);
warningLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
warningLabel.setForeground(Color.RED);
warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
createAccountPanel.add(Box.createVerticalStrut(10)); // Add spacing
createAccountPanel.add(warningLabel);
createAccountPanel.add(Box.createVerticalStrut(20)); // Add spacing

// Encryption Mode panel
JPanel encryptionModePanel = new JPanel();
encryptionModePanel.setLayout(new BoxLayout(encryptionModePanel, BoxLayout.X_AXIS));
encryptionModePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
JLabel encryptionModeLabel = new JLabel("Encryption Mode: ");
encryptionModeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
encryptionModePanel.add(encryptionModeLabel);

// Radio buttons
JRadioButton gpgFirstButton = new JRadioButton("GPG → Encryption");
JRadioButton encryptionFirstButton = new JRadioButton("Encryption → GPG", true); // Default to Encryption -> GPG
ButtonGroup encryptionModeGroup = new ButtonGroup();
encryptionModeGroup.add(gpgFirstButton);
encryptionModeGroup.add(encryptionFirstButton);

// Add tooltips on hover to explain encryption order
gpgFirstButton.setToolTipText("GPG is applied first, ensuring authenticity and integrity before encryption.");
encryptionFirstButton.setToolTipText("Encryption is applied first, ensuring speed before signing with GPG.");

// Add action listeners to update the encryptionMode variable
gpgFirstButton.addItemListener(e -> {
    if (e.getStateChange() == ItemEvent.SELECTED) {
        strictMode = true; // GPG → Encryption
    }
});

encryptionFirstButton.addItemListener(e -> {
    if (e.getStateChange() == ItemEvent.SELECTED) {
        strictMode = false; // Encryption → GPG
    }
});

encryptionModePanel.add(gpgFirstButton);
encryptionModePanel.add(Box.createHorizontalStrut(10)); // Add spacing
encryptionModePanel.add(encryptionFirstButton);
createAccountPanel.add(encryptionModePanel);
            
            // Panel for device toggles
            JPanel devicePanel = new JPanel();
            devicePanel.setLayout(new GridLayout(0, 1, 10, 10));  // Grid layout for device toggles

            // List to keep track of selected serial numbers
            List<String> selectedSerials = new ArrayList<>();

            // Displaying each storage device on the UI
            for (String deviceName : deviceNames) {
                JToggleButton deviceToggleButton = new JToggleButton(deviceName);

                // Add action listener to toggle button
                deviceToggleButton.addActionListener(e -> {
                    if (deviceToggleButton.isSelected()) {
                        // Add the corresponding serial number to the list
                        selectedSerials.add(diskToSerialMap.get(deviceName));
                    } else {
                        // Remove the serial number from the list if deselected
                        selectedSerials.remove(diskToSerialMap.get(deviceName));
                    }
                });

                devicePanel.add(deviceToggleButton);
            }

            createAccountPanel.add(devicePanel);
            createAccountPanel.add(Box.createVerticalStrut(20)); // Spacing

            // Continue button
            JButton continueButton = new JButton("Continue");
            continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            continueButton.addActionListener(e -> {
                // Hash the serial numbers, sort them, and pass them to the next function
                hashedSerials = selectedSerials.stream()
                    .map(hash::hashSHA512)
                    .sorted()
                    .collect(Collectors.toList());

                // Pass the sorted hashed serials to the next function
                insertGPGIdentity(frame);
            });

            createAccountPanel.add(continueButton);
            createAccountPanel.add(Box.createVerticalStrut(10)); // Spacing

            // Back button to return to the main menu
            JButton backButton = new JButton("Back");
            backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            backButton.addActionListener(e -> app.mainMenu(frame));
            createAccountPanel.add(backButton);

            frame.getContentPane().add(createAccountPanel, BorderLayout.CENTER);

            frame.revalidate();
            frame.repaint();
        });
    }
}
