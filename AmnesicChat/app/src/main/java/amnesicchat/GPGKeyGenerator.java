import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;

import java.io.*;
import java.security.*;
import java.time.LocalDate; // ✅ FIXED: Import LocalDate
import java.util.Date;

public class GPGKeyGenerator {
    public static void generateGPGKey(String userId, String email, String passphrase, LocalDate expiry, String algorithm, int keySize, String comments) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Key Pair Generator based on selected algorithm
        KeyPairGenerator keyPairGenerator;
        if ("RSA".equalsIgnoreCase(algorithm)) {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(keySize);
        } else if ("ECC".equalsIgnoreCase(algorithm)) {
            keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGenerator.initialize(keySize);
        } else if ("DSA".equalsIgnoreCase(algorithm)) {
            keyPairGenerator = KeyPairGenerator.getInstance("DSA", "BC");
            keyPairGenerator.initialize(keySize);
        } else {
            throw new IllegalArgumentException("Unsupported Algorithm");
        }

        // Generate key pair
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        // Save key pair to user's home directory
        String userHome = System.getProperty("user.home");
        File privateKeyFile = new File(userHome, "privateKey.asc");

        try (FileOutputStream privateKeyOut = new FileOutputStream(privateKeyFile);
             ArmoredOutputStream armoredOut = new ArmoredOutputStream(privateKeyOut)) {
            
            PGPKeyRingGenerator keyRingGenerator = generateKeyRingGenerator(userId, password.toCharArray(), keyPair);
            keyRingGenerator.generateSecretKeyRing().encode(armoredOut);
            
            System.out.println("GPG Key successfully saved in: " + privateKeyFile.getAbsolutePath());
        }
    }

    private static PGPKeyRingGenerator generateKeyRingGenerator(String userId, char[] passphrase, KeyPair keyPair)
            throws PGPException {
        PGPKeyPair pgpKeyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, keyPair, new Date());

        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
        PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION,
                pgpKeyPair,
                userId,
                sha1Calc,
                null, null,
                new JcaPGPContentSignerBuilder(pgpKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256),
                new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha1Calc).setProvider("BC").build(passphrase)
        );

        return keyRingGen;
    }
}
