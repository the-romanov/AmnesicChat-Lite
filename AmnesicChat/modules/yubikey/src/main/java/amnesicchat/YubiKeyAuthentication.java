import java.security.MessageDigest;

public class YubiKeyAuthentication implements AuthenticationModule {
    private String challengeResponseKey;

    public YubiKeyAuthentication() {
        challengeResponseKey = generateChallengeResponseKey("test"); 
    }

    private String generateChallengeResponseKey(String userId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((userId + "secret_key").getBytes()); // "secret_key" is the shared secret
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getName() {
        return "YubiKey Authentication Module";
    }

    @Override
    public String getAuthor() {
        return "the-romanov (GitHub)";
    }

    @Override
    public void run() {
        System.out.println("Running YubiKey authentication...");
        // Simulate YubiKey interaction
        try {
            Thread.sleep(2000); // Simulate delay
            System.out.println("YubiKey authentication successful!");
            System.out.println("Generated Challenge-Response Key: " + challengeResponseKey);
        } catch (InterruptedException e) {
            System.err.println("Authentication interrupted.");
        }
    }
}
