import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.file.Files;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.security.SecureRandom;
import java.util.Base64;


public class CipherData {
		
    static Hash hash = CentralManager.getHash();
	
	//Generate Random Key and Hash
	public String generateRandomKey() {
	 SecureRandom random = new SecureRandom();
	 byte[] keyBytes = new byte[512]; // 4096 bits = 512 bytes
	 random.nextBytes(keyBytes);
	 String key = Base64.getEncoder().encodeToString(keyBytes); // UTF-8 compatible string
	 return hash.hashSHA512(key); // Hash the key using SHA-512
	}
	
	//ENCRYPTION
	public byte[] encryptFileWithOrder(File file, List<byte[]> keys, ArrayList<String> encryptionOrder) throws Exception {
	    byte[] fileContent = Files.readAllBytes(file.toPath());
	    byte[] encryptedData = fileContent;

	    for (String algorithm : encryptionOrder) {
	        switch (algorithm) {
	            case "AES":
	                encryptedData = encryptWithAES(encryptedData, keys);
	                break;
	            case "Serpent":
	                encryptedData = encryptWithSerpent(encryptedData, keys);
	                break;
	            case "Twofish":
	                encryptedData = encryptWithTwofish(encryptedData, keys);
	                break;
	            case "Camellia":
	                encryptedData = encryptWithCamellia(encryptedData, keys);
	                break;
	            case "Kuznyechik":
	                encryptedData = encryptWithKuznyechik(encryptedData, keys);
	                break;
	            default:
	                throw new IllegalArgumentException("Unknown encryption algorithm: " + algorithm);
	        }
	    }
	    return encryptedData;
	}

	public byte[] encryptWithAES(byte[] deviceID, List<byte[]> keys) throws Exception {
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    byte[] encryptedData = deviceID;

	    // Encrypt using the keys in reverse order
	    for (int i = keys.size() - 1; i >= 0; i--) {
	        SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "AES");
	        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]); // Use a zero IV for simplicity

	        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
	        encryptedData = cipher.doFinal(encryptedData);  // Encrypt the data using the current key
	    }

	    return encryptedData;
	}

	// Serpent Encryption with Multiple Keys and PKCS7Padding
	//.addProvider(new BouncyCastleProvider());
	public byte[] encryptWithSerpent(byte[] deviceID, List<byte[]> keys) throws Exception {
	    Cipher cipher = Cipher.getInstance("Serpent/CBC/PKCS7Padding");
	    byte[] encryptedData = deviceID;

	    // Encrypt using the keys in reverse order
	    for (int i = keys.size() - 1; i >= 0; i--) {
	        SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "Serpent");
	        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]); // Use a zero IV for simplicity

	        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
	        encryptedData = cipher.doFinal(encryptedData);  // Encrypt the data using the current key
	    }

	    return encryptedData;
	}

	// Twofish Encryption with Multiple Keys and PKCS7Padding
	public byte[] encryptWithTwofish(byte[] deviceID, List<byte[]> keys) throws Exception {
	    Cipher cipher = Cipher.getInstance("Twofish/CBC/PKCS7Padding");
	    byte[] encryptedData = deviceID;

	    // Encrypt using the keys in reverse order
	    for (int i = keys.size() - 1; i >= 0; i--) {
	        SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "Twofish");
	        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]); // Use a zero IV for simplicity

	        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
	        encryptedData = cipher.doFinal(encryptedData);  // Encrypt the data using the current key
	    }

	    return encryptedData;
	}

	// Camellia Encryption with Multiple Keys and PKCS7Padding
	public byte[] encryptWithCamellia(byte[] deviceID, List<byte[]> keys) throws Exception {
	    Cipher cipher = Cipher.getInstance("Camellia/CBC/PKCS7Padding");
	    byte[] encryptedData = deviceID;

	    // Encrypt using the keys in reverse order
	    for (int i = keys.size() - 1; i >= 0; i--) {
	        SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "Camellia");
	        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]); // Use a zero IV for simplicity

	        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
	        encryptedData = cipher.doFinal(encryptedData);  // Encrypt the data using the current key
	    }

	    return encryptedData;
	}

	// Kuznyechik (GOST) Encryption with Multiple Keys and PKCS7Padding
	public byte[] encryptWithKuznyechik(byte[] deviceID, List<byte[]> keys) throws Exception {
	    Cipher cipher = Cipher.getInstance("GOST3412-2015/CBC/PKCS7Padding");
	    byte[] encryptedData = deviceID;

	    // Encrypt using the keys in reverse order
	    for (int i = keys.size() - 1; i >= 0; i--) {
	        SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "GOST3412-2015");
	        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]); // Use a zero IV for simplicity

	        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
	        encryptedData = cipher.doFinal(encryptedData);  // Encrypt the data using the current key
	    }

	    return encryptedData;
	}
	
	// DECRYPTION
    private ArrayList<String> getDecryptionOrder() {
        ArrayList<String> decryptionOrder = new ArrayList<>();
        // Add algorithms in reverse of encryption order
        decryptionOrder.add("Kuznyechik");
        decryptionOrder.add("Camellia");
        decryptionOrder.add("Twofish");
        decryptionOrder.add("Serpent");
        decryptionOrder.add("AES");
        return decryptionOrder;
    }

 // Decrypt Function with dynamic decryption order
    public byte[] decryptFileWithOrder(byte[] encryptedData, List<byte[]> keys, ArrayList<String> encryptionOrder) throws Exception {
        byte[] decryptedData = encryptedData;

        // Reverse the order for decryption
        Collections.reverse(encryptionOrder);

        for (String algorithm : encryptionOrder) {
            switch (algorithm) {
                case "AES":
                    decryptedData = decryptWithAES(decryptedData, keys);
                    break;
                case "Serpent":
                    decryptedData = decryptWithSerpent(decryptedData, keys);
                    break;
                case "Twofish":
                    decryptedData = decryptWithTwofish(decryptedData, keys);
                    break;
                case "Camellia":
                    decryptedData = decryptWithCamellia(decryptedData, keys);
                    break;
                case "Kuznyechik":
                    decryptedData = decryptWithKuznyechik(decryptedData, keys);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown decryption algorithm: " + algorithm);
            }
        }
        return decryptedData;
    }
    
    private void handleDecryptedData(byte[] decryptedData) {
        // Convert decrypted data to a string assuming it was originally a text string
        try {
            String decryptedString = new String(decryptedData, StandardCharsets.UTF_8);
            System.out.println("Decrypted Data: " + decryptedString);  // Prints decrypted text
        } catch (Exception e) {
            System.err.println("Error converting decrypted data: " + e.getMessage());
        }
    }
	
    public byte[] decryptWithAES(byte[] encryptedData, List<byte[]> keys) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] decryptedData = encryptedData;

        // Decrypt using the keys in reverse order
        for (int i = keys.size() - 1; i >= 0; i--) {
            SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            decryptedData = cipher.doFinal(decryptedData);
        }

        return decryptedData;
    }

    public byte[] decryptWithSerpent(byte[] encryptedData, List<byte[]> keys) throws Exception {
        Cipher cipher = Cipher.getInstance("Serpent/CBC/PKCS7Padding");
        byte[] decryptedData = encryptedData;

        // Decrypt using the keys in reverse order
        for (int i = keys.size() - 1; i >= 0; i--) {
            SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "Serpent");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            decryptedData = cipher.doFinal(decryptedData);
        }

        return decryptedData;
    }

    public byte[] decryptWithTwofish(byte[] encryptedData, List<byte[]> keys) throws Exception {
        Cipher cipher = Cipher.getInstance("Twofish/CBC/PKCS7Padding");
        byte[] decryptedData = encryptedData;

        // Decrypt using the keys in reverse order
        for (int i = keys.size() - 1; i >= 0; i--) {
            SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "Twofish");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            decryptedData = cipher.doFinal(decryptedData);
        }

        return decryptedData;
    }

    public byte[] decryptWithCamellia(byte[] encryptedData, List<byte[]> keys) throws Exception {
        Cipher cipher = Cipher.getInstance("Camellia/CBC/PKCS7Padding");
        byte[] decryptedData = encryptedData;

        // Decrypt using the keys in reverse order
        for (int i = keys.size() - 1; i >= 0; i--) {
            SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "Camellia");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            decryptedData = cipher.doFinal(decryptedData);
        }

        return decryptedData;
    }

    public byte[] decryptWithKuznyechik(byte[] encryptedData, List<byte[]> keys) throws Exception {
        Cipher cipher = Cipher.getInstance("GOST3412-2015/CBC/PKCS7Padding");
        byte[] decryptedData = encryptedData;

        // Decrypt using the keys in reverse order
        for (int i = keys.size() - 1; i >= 0; i--) {
            SecretKeySpec keySpec = new SecretKeySpec(keys.get(i), "GOST3412-2015");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            decryptedData = cipher.doFinal(decryptedData); 
        }

        return decryptedData;
    }	
	
}