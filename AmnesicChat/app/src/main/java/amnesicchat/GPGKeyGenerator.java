import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.time.LocalDate;
import java.util.Date;

public class GPGKeyGenerator {

    public static void generateGPGKey(String userId, String email, String passphrase, LocalDate expiry,
                                      String algorithm, int keySize, String comments) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // Generate the KeyPair
        KeyPair keyPair = generateKeyPair(algorithm, keySize);

        // Save the private and public keys
        String userHome = System.getProperty("user.home");
        File privateKeyFile = new File(userHome, "privateKey.asc");
        File publicKeyFile = new File(userHome, "publicKey.asc");

        try (FileOutputStream privateKeyOut = new FileOutputStream(privateKeyFile);
             ArmoredOutputStream armoredPrivateOut = new ArmoredOutputStream(privateKeyOut);
             FileOutputStream publicKeyOut = new FileOutputStream(publicKeyFile);
             ArmoredOutputStream armoredPublicOut = new ArmoredOutputStream(publicKeyOut)) {

            PGPKeyRingGenerator keyRingGenerator = generateKeyRingGenerator(userId + " <" + email + ">", keyPair, algorithm);
            keyRingGenerator.generateSecretKeyRing().encode(armoredPrivateOut);
            keyRingGenerator.generatePublicKeyRing().encode(armoredPublicOut);

            System.out.println("GPG Private Key successfully saved in: " + privateKeyFile.getAbsolutePath());
            System.out.println("GPG Public Key successfully saved in: " + publicKeyFile.getAbsolutePath());
        }
    }

    private static KeyPair generateKeyPair(String algorithm, int keySize) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator;
        switch (algorithm.toUpperCase()) {
            case "RSA" -> {
                keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
                keyPairGenerator.initialize(keySize);
            }
            case "ECC" -> {
                keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
                switch (keySize) {
                    case 256 -> keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));
                    case 384 -> keyPairGenerator.initialize(new ECGenParameterSpec("secp384r1"));
                    case 521 -> keyPairGenerator.initialize(new ECGenParameterSpec("secp521r1"));
                    default -> throw new IllegalArgumentException("Unsupported key size for ECC: " + keySize);
                }
            }
            case "DSA" -> {
                keyPairGenerator = KeyPairGenerator.getInstance("DSA", "BC");
                keyPairGenerator.initialize(keySize);
            }
            default -> throw new IllegalArgumentException("Unsupported Algorithm: " + algorithm);
        }
        return keyPairGenerator.generateKeyPair();
    }

    private static PGPKeyRingGenerator generateKeyRingGenerator(String userId, KeyPair keyPair, String algorithm)
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

        return new PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION,
                pgpKeyPair,
                userId,
                sha1Calc,
                null,
                null,
                new JcaPGPContentSignerBuilder(pgpKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256),
                null // No passphrase, no encryption
        );
    }
}
