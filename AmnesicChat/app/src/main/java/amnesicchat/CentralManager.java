public class CentralManager {
    // Declare instances of the required classes
	private static App app = new App();
	private static ChatSession chatSession = new ChatSession();
	private static CipherData cipherData = new CipherData();
	private static CreateAccount createAccount = new CreateAccount();
    private static final Hash hash = new Hash();
    private static JoinPeerToPeer joinPeerToPeer = new JoinPeerToPeer();
    private static Settings settings = new Settings();
    private static StorageDevices storageDevices = new StorageDevices();
    
    // Getter methods to access instances (stops duplication and promotes optimisation)
    public static App getApp() {return app;}
    public static ChatSession getChatSession() {return chatSession;}
    public static CipherData getCipherData() {return cipherData;}
    public static CreateAccount getCreateAccount() {return createAccount;}
    public static Hash getHash() {
        if (hash != null) {
            return hash;
        } else {
            return new Hash(); // Stops program crashing
        }
    }
    public static JoinPeerToPeer getJoinPeerToPeer() {return joinPeerToPeer;}
    public static Settings getSettings() {return settings;}
    public static StorageDevices getStorageDevices() {return storageDevices;}
}
