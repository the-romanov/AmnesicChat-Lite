import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;


import java.io.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GPGKeyGenerator {

    public static File publicKeyFile;
    public static File privateKeyFile;

 public static byte[] encryptWithPublicKey(byte[] data, PGPPublicKey publicKey) throws IOException, PGPException {
        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        
        // Use BouncyCastle API to encrypt data with the public key
        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                        .setWithIntegrityPacket(true)
                        .setSecureRandom(new SecureRandom())
                        .setProvider("BC")
        );

        encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey).setProvider("BC"));

        OutputStream cOut = encGen.open(encryptedOut, data.length);
        cOut.write(data);
        cOut.close();
        encGen.close();

        return encryptedOut.toByteArray();
    }

    public static String generateGPGKey(String userId, String email, String passphrase, LocalDate expiry,
                                        String algorithm, String keySizeOrCurve, String comments) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // Generate the KeyPair (now supports ECC curves properly)
        KeyPair keyPair = generateKeyPair(algorithm, keySizeOrCurve);

        // Save the private and public keys
        String userHome = System.getProperty("user.home");
        privateKeyFile = new File(userHome, "privateKey.asc");
        publicKeyFile = new File(userHome, "publicKey.asc");

        try (FileOutputStream privateKeyOut = new FileOutputStream(privateKeyFile);
             ArmoredOutputStream armoredPrivateOut = new ArmoredOutputStream(privateKeyOut);
             FileOutputStream publicKeyOut = new FileOutputStream(publicKeyFile);
             ArmoredOutputStream armoredPublicOut = new ArmoredOutputStream(publicKeyOut)) {

            PGPKeyRingGenerator keyRingGenerator = generateKeyRingGenerator(
                    userId + " <" + email + ">", keyPair, algorithm, passphrase, expiry);

            keyRingGenerator.generateSecretKeyRing().encode(armoredPrivateOut);
            keyRingGenerator.generatePublicKeyRing().encode(armoredPublicOut);

            System.out.println("GPG Private Key successfully saved in: " + privateKeyFile.getAbsolutePath());
            System.out.println("GPG Public Key successfully saved in: " + publicKeyFile.getAbsolutePath());

            PGPPublicKeyRing publicKeyRing = keyRingGenerator.generatePublicKeyRing();
            PGPPublicKey publicKey = publicKeyRing.getPublicKey();
            byte[] fingerprint = publicKey.getFingerprint();
            return bytesToHex(fingerprint);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0'); // Append leading zero if necessary
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase(); // Convert to uppercase for standard GPG fingerprint format
    }

    private static KeyPair generateKeyPair(String algorithm, String keySizeOrCurve) 
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator;

        switch (algorithm.toUpperCase()) {
            case "RSA", "DSA" -> {
                int keySize = Integer.parseInt(keySizeOrCurve); // Convert String to int
                keyPairGenerator = KeyPairGenerator.getInstance(algorithm, "BC");
                keyPairGenerator.initialize(keySize);
            }
            case "ECC" -> {
                keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
                keyPairGenerator.initialize(new ECGenParameterSpec(getECCCurve(keySizeOrCurve)));
            }
            default -> throw new IllegalArgumentException("Unsupported Algorithm: " + algorithm);
        }
        return keyPairGenerator.generateKeyPair();
    }

    private static String getECCCurve(String curveName) {
        return switch (curveName) {
            case "Curve25519" -> "curve25519";
            case "NIST P-256" -> "secp256r1";
            case "NIST P-384" -> "secp384r1";
            case "NIST P-521" -> "secp521r1";
            case "BrainpoolP256r1" -> "brainpoolP256r1";
            case "BrainpoolP384r1" -> "brainpoolP384r1";
            case "BrainpoolP512r1" -> "brainpoolP512r1";
            default -> throw new IllegalArgumentException("Unsupported ECC curve: " + curveName);
        };
    }

    private static PGPKeyRingGenerator generateKeyRingGenerator(String userId, KeyPair keyPair, String algorithm, String pass, LocalDate expiry)
            throws PGPException {
        int algorithmTag;
        switch (algorithm.toUpperCase()) {
            case "RSA" -> algorithmTag = PGPPublicKey.RSA_GENERAL;
            case "ECC" -> algorithmTag = PGPPublicKey.ECDSA;
            case "DSA" -> algorithmTag = PGPPublicKey.DSA;
            default -> throw new IllegalArgumentException("Unsupported Algorithm: " + algorithm);
        }

        PGPKeyPair pgpKeyPair = new JcaPGPKeyPair(algorithmTag, keyPair, new Date());
        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);

        // Convert LocalDate expiry to seconds since key creation
        long creationTime = pgpKeyPair.getPublicKey().getCreationTime().getTime() / 1000;
        long expirySeconds = expiry.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - creationTime;

        if (expirySeconds < 0) {
            throw new IllegalArgumentException("Expiry date cannot be before the key's creation time.");
        }

        // Set key expiration time
        PGPSignatureSubpacketGenerator subpacketGen = new PGPSignatureSubpacketGenerator();
        subpacketGen.setKeyExpirationTime(false, expirySeconds);

        char[] passphrase = pass.toCharArray();

        return new PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION,
                pgpKeyPair,
                userId,
                sha1Calc,
                subpacketGen.generate(), // Attach expiry information
                null,
                new JcaPGPContentSignerBuilder(pgpKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256),
                new JcePBESecretKeyEncryptorBuilder(PGPPublicKey.RSA_ENCRYPT, HashAlgorithmTags.SHA256)
                        .setSecureRandom(new SecureRandom())
                        .build(passphrase) // Passphrase for protecting the key
        );
    }
}
