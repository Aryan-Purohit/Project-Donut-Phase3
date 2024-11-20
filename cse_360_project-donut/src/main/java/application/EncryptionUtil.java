package application;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for encryption and decryption using AES algorithm.
 * It includes methods for key generation and management.
 */
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String PASSWORD_KEY_FILE = "passwordKey.key";
    private static final String ARTICLE_KEY_FILE = "articleKey.key";

    /**
     * Encrypts a plain text using the provided key.
     *
     * @param valueToEnc The plain text to encrypt.
     * @param key        The encryption key.
     * @return The encrypted byte array.
     * @throws Exception If encryption fails.
     */
    public static byte[] encrypt(String valueToEnc, byte[] key) throws Exception {
        Key keySpec = generateKey(key);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encValue = c.doFinal(valueToEnc.getBytes("UTF-8"));
        return encValue;
    }

    /**
     * Decrypts an encrypted byte array using the provided key.
     *
     * @param encryptedValue The encrypted byte array.
     * @param key            The decryption key.
     * @return The decrypted plain text.
     * @throws Exception If decryption fails.
     */
    public static String decrypt(byte[] encryptedValue, byte[] key) throws Exception {
        Key keySpec = generateKey(key);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decValue = c.doFinal(encryptedValue);
        String decryptedValue = new String(decValue, "UTF-8");
        return decryptedValue;
    }

    /**
     * Generates a secret key specification based on the provided key bytes.
     *
     * @param keyBytes The key bytes.
     * @return The secret key specification.
     * @throws Exception If key generation fails.
     */
    private static Key generateKey(byte[] keyBytes) throws Exception {
        // Use only first 16 bytes for AES-128
        byte[] keyBytes16 = new byte[16];
        System.arraycopy(keyBytes, 0, keyBytes16, 0, Math.min(keyBytes.length, 16));
        Key key = new SecretKeySpec(keyBytes16, ALGORITHM);
        return key;
    }

    /**
     * Generates a random encryption key and saves it to a file.
     *
     * @param keyFile The file to save the key to.
     * @throws IOException If an I/O error occurs.
     */
    public static byte[] generateAndStoreKey(String keyFile) throws IOException {
        byte[] key = new byte[16]; // 16 bytes for AES-128
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        try (FileOutputStream fos = new FileOutputStream(keyFile)) {
            fos.write(Base64.getEncoder().encode(key));
        }
        return key;
    }

    /**
     * Loads an encryption key from a file. If the file doesn't exist, generates a new key.
     *
     * @param keyFile The file to load the key from.
     * @return The encryption key bytes.
     * @throws IOException If an I/O error occurs.
     */
    public static byte[] loadKey(String keyFile) throws IOException {
        File file = new File(keyFile);
        if (!file.exists()) {
            // Generate and store a new key
            return generateAndStoreKey(keyFile);
        } else {
            // Load existing key
            try (FileInputStream fis = new FileInputStream(keyFile)) {
                byte[] encodedKey = fis.readAllBytes();
                return Base64.getDecoder().decode(encodedKey);
            }
        }
    }

    /**
     * Loads the password encryption key.
     *
     * @return The encryption key bytes.
     * @throws IOException If an I/O error occurs.
     */
    public static byte[] getPasswordKey() throws IOException {
        return loadKey(PASSWORD_KEY_FILE);
    }

    /**
     * Loads the article encryption key.
     *
     * @return The encryption key bytes.
     * @throws IOException If an I/O error occurs.
     */
    public static byte[] getArticleKey() throws IOException {
        return loadKey(ARTICLE_KEY_FILE);
    }
}
