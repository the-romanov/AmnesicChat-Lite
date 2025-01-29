import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*; 
import java.awt.event.ActionListener;

public class Settings {
public void settingsUI() {
    	    JFrame frame = new JFrame("Settings UI");
    	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only the frame, not the program
    	    frame.setSize(800, 600);
    	    frame.setLayout(new GridLayout(1, 2));

    	    // Left panel for appearance settings
    	    JPanel appearancePanel = new JPanel();
    	    appearancePanel.setLayout(new BoxLayout(appearancePanel, BoxLayout.Y_AXIS));
    	    appearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance:"));

    	    JCheckBox verboseMode = new JCheckBox("Verbose Mode");
    	    JCheckBox darkMode = new JCheckBox("Dark Mode");
    	    JSlider zoomLevel = new JSlider(25, 200, 100);
    	    zoomLevel.setPaintLabels(true);
    	    zoomLevel.setPaintTicks(true);
    	    zoomLevel.setMajorTickSpacing(25);
    	    zoomLevel.setBorder(BorderFactory.createTitledBorder("Zoom Level"));

    	    JSlider fontSize = new JSlider(6, 28, 14);
    	    fontSize.setPaintLabels(true);
    	    fontSize.setPaintTicks(true);
    	    fontSize.setMajorTickSpacing(6);
    	    fontSize.setBorder(BorderFactory.createTitledBorder("Font Size"));

    	    JComboBox<String> fontStyle = new JComboBox<>(new String[]{"DejaVu Sans", "Arial", "Courier New", "Verdana"});
    	    fontStyle.setBorder(BorderFactory.createTitledBorder("Font Style"));

    	    JCheckBox launchAtStartup = new JCheckBox("Launch At Startup?");
    	    JCheckBox startupMinimised = new JCheckBox("Startup Minimised");

    	    appearancePanel.add(verboseMode);
    	    appearancePanel.add(darkMode);
    	    appearancePanel.add(zoomLevel);
    	    appearancePanel.add(fontSize);
    	    appearancePanel.add(fontStyle);
    	    appearancePanel.add(launchAtStartup);
    	    appearancePanel.add(startupMinimised);

    	    // Right panel for notifications and other settings
    	    JPanel notificationsPanel = new JPanel();
    	    notificationsPanel.setLayout(new BoxLayout(notificationsPanel, BoxLayout.Y_AXIS));
    	    notificationsPanel.setBorder(BorderFactory.createTitledBorder("Notifications:"));

    	    JTextField mediaDownloadLimit = new JTextField("9999");
    	    mediaDownloadLimit.setBorder(BorderFactory.createTitledBorder("Media Download Limit (MB/s)"));

    	    JTextField openModulesFolder = new JTextField();
    	    openModulesFolder.setBorder(BorderFactory.createTitledBorder("Open Modules Folder"));

    	    JButton saveButton = new JButton("Save Settings");
    	    JButton loadButton = new JButton("Load Settings");

    	    notificationsPanel.add(mediaDownloadLimit);
    	    notificationsPanel.add(openModulesFolder);
    	    notificationsPanel.add(saveButton);
    	    notificationsPanel.add(loadButton);

    	    // Add listeners for save and load buttons
    	    saveButton.addActionListener(e -> saveSettings(
    	        verboseMode.isSelected(), 
    	        darkMode.isSelected(), 
    	        zoomLevel.getValue(), 
    	        fontSize.getValue(), 
    	        (String) fontStyle.getSelectedItem(), 
    	        launchAtStartup.isSelected(), 
    	        startupMinimised.isSelected(), 
    	        mediaDownloadLimit.getText(), 
    	        openModulesFolder.getText()
    	    ));

    	    loadButton.addActionListener(e -> loadSettings(
    	        verboseMode, 
    	        darkMode, 
    	        zoomLevel, 
    	        fontSize, 
    	        fontStyle, 
    	        launchAtStartup, 
    	        startupMinimised, 
    	        mediaDownloadLimit, 
    	        openModulesFolder
    	    ));

    	    // Add panels to frame
    	    frame.add(appearancePanel);
    	    frame.add(notificationsPanel);
    	    frame.setVisible(true);
    	}

    	// Example saveSettings and loadSettings methods
    	private void saveSettings(
    	    boolean verboseMode, 
    	    boolean darkMode, 
    	    int zoomLevel, 
    	    int fontSize, 
    	    String fontStyle, 
    	    boolean launchAtStartup, 
    	    boolean startupMinimised, 
    	    String mediaDownloadLimit, 
    	    String openModulesFolder
    	) {
    	    // TO do
    	}

    	private void loadSettings(
    	    JCheckBox verboseMode, 
    	    JCheckBox darkMode, 
    	    JSlider zoomLevel, 
    	    JSlider fontSize, 
    	    JComboBox<String> fontStyle, 
    	    JCheckBox launchAtStartup, 
    	    JCheckBox startupMinimised, 
    	    JTextField mediaDownloadLimit, 
    	    JTextField openModulesFolder
    	) {
    	    // TO do
    	}
}