package server;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoManager {
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int T_LEN = 128;
    private static final int IV_LENGTH = 12;
    protected static Object[] decryptClientRequest(String packet, String privateKeyB64) throws Exception {
        String[] parts = packet.split(":");
        byte[] encryptedKey = Base64.getDecoder().decode(parts[0]);
        byte[] iv = Base64.getDecoder().decode(parts[1]);
        byte[] encryptedData = Base64.getDecoder().decode(parts[2]);

        // 1. Giải mã lấy Session Key (RSA)
        byte[] privKeyBytes = Base64.getDecoder().decode(privateKeyB64);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] sessionKeyBytes = rsaCipher.doFinal(encryptedKey);
        SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, "AES");

        // 2. Giải mã lệnh (AES)
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.DECRYPT_MODE, sessionKey, new GCMParameterSpec(T_LEN, iv));
        String plainText = new String(aesCipher.doFinal(encryptedData));

        return new Object[]{plainText, sessionKey};
    }


    //Mã hóa dữ liệu Server phản hồi: IV : encrypted_Data
    protected static String encryptServerResponse(String plainText, SecretKey sessionKey) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey, new GCMParameterSpec(T_LEN, iv));
        byte[] encryptedData = aesCipher.doFinal(plainText.getBytes());

        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Hàm tạo Session Key mới cho mỗi Request
     */
    protected static SecretKey generateSessionKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }
}
