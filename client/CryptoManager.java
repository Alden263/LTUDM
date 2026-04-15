package client;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class CryptoManager {
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int T_LEN = 128;
    private static final int IV_LENGTH = 12;
    public static String encryptClientRequest(String plainText, String publicKeyB64, SecretKey sessionKey) throws Exception {
        // 1. Tạo IV ngẫu nhiên
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // 2. Mã hóa dữ liệu bằng AES
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey, new GCMParameterSpec(T_LEN, iv));
        byte[] encryptedData = aesCipher.doFinal(plainText.getBytes());

        // 3. Mã hóa Session Key bằng RSA Public Key
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyB64);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKey = rsaCipher.doFinal(sessionKey.getEncoded());

        // 4. Đóng gói
        return Base64.getEncoder().encodeToString(encryptedKey) + ":" +
               Base64.getEncoder().encodeToString(iv) + ":" +
               Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Giải mã phản hồi từ Server (Chỉ có IV : AES_Data vì Client đã có khóa AES)
     */
    public static String decryptServerResponse(String packet, SecretKey sessionKey) throws Exception {
        String[] parts = packet.split(":");
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encryptedData = Base64.getDecoder().decode(parts[1]);

        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.DECRYPT_MODE, sessionKey, new GCMParameterSpec(T_LEN, iv));
        return new String(aesCipher.doFinal(encryptedData));
    }
    public static SecretKey generateSessionKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }
}
 