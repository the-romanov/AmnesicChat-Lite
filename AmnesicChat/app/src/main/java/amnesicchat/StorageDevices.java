import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;

public class StorageDevices {
	public List<String> getAvailableStorageDevices() {
	    List<String> devices = new ArrayList<>();

	        // Use hardware information (SystemInfo) to get devices
	        SystemInfo systemInfo = new SystemInfo();
	        List<HWDiskStore> diskStores = systemInfo.getHardware().getDiskStores();

	        for (HWDiskStore diskStore : diskStores) {
	            devices.add(diskStore.getName());
	        } 	   
	    return devices;
	}
    

    // Fetch storage device names using OSHI
    public List<String> getStorageDeviceNames() {
        List<String> deviceNames = new ArrayList<>();
        SystemInfo systemInfo = new SystemInfo();
        oshi.hardware.HardwareAbstractionLayer hardware = systemInfo.getHardware();
        List<HWDiskStore> diskStores = hardware.getDiskStores();

        // Iterate through each disk and add its name to the list, filtering out logical volumes
        for (HWDiskStore disk : diskStores) {
            String diskName = disk.getModel();  // Get the model of the disk (e.g., "Samsung 970 Evo")
            
            // Filter out logical volumes or devices with names that suggest they are not physical
            if (!isLogicalVolume(diskName)) {
                deviceNames.add(diskName);
            }
        }

        if (deviceNames.isEmpty()) {
            deviceNames.add("No devices found.");
        }

        return deviceNames;
    }
    
 // Function to check if the device name suggests it is a logical volume
    private boolean isLogicalVolume(String deviceName) {
        // Check if the device name contains typical logical volume keywords
        String[] logicalKeywords = {"logical", "volume", "raid", "virtual", "part", "mapper", "md"};
        for (String keyword : logicalKeywords) {
            if (deviceName.toLowerCase().contains(keyword)) {
                return true; // It's likely a logical volume
            }
        }
        return false; // Otherwise, assume it's a physical device
    }
    private void updateDeviceList(JPanel panel) {
    	// Get the available storage devices based on the mode selected
    	List<String> availableDevices = getAvailableStorageDevices();

    	// Find the devicePanel in the panel and update it
    	JPanel devicePanel = null;
    	Component[] components = panel.getComponents();

    	// Look for the existing device panel
    	for (Component component : components) {
    	    if (component instanceof JPanel && ((JPanel) component).getLayout() instanceof GridLayout) {
    	        devicePanel = (JPanel) component;
    	        break; // Found the existing device panel
    	    }
    	}

    	// If no devicePanel found, create a new one
    	if (devicePanel == null) {
    	    devicePanel = new JPanel();
    	    devicePanel.setLayout(new GridLayout(0, 1, 10, 10)); // Use a grid layout
    	    panel.add(devicePanel);
    	} else {
    	    devicePanel.removeAll();  // Clear previous buttons
    	}

    	// Create toggle buttons for the available devices
    	for (String device : availableDevices) {
    	    JToggleButton deviceToggleButton = new JToggleButton(device);
    	    deviceToggleButton.setToolTipText("Click to select " + device);
    	    devicePanel.add(deviceToggleButton);  // Add the toggle button
    	}

    	// Refresh the devicePanel
    	panel.revalidate();
    	panel.repaint();
    	}
}