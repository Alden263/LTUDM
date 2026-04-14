import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSAKeyGeneratorTool {
    public static void main(String[] args) {
        try {
            // 1. Khởi tạo đối tượng tạo khóa RSA
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            
            // Kích thước khóa 2048 bit là chuẩn an toàn và phổ biến hiện nay
            keyGen.initialize(2048); 

            // 2. Tạo cặp khóa (Public Key & Private Key)
            KeyPair pair = keyGen.generateKeyPair();
            PublicKey publicKey = pair.getPublic();
            PrivateKey privateKey = pair.getPrivate();

            // 3. Lấy mảng byte của khóa (Java tự động dùng chuẩn X.509 cho Public và PKCS#8 cho Private)
            byte[] publicKeyBytes = publicKey.getEncoded();
            byte[] privateKeyBytes = privateKey.getEncoded();

            // 4. Mã hóa mảng byte thành chuỗi Base64 để in ra màn hình dạng text
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);
            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes);

            // 5. In kết quả
            System.out.println("=== PUBLIC KEY (Dùng để gán cứng vào code Sender) ===");
            System.out.println(publicKeyBase64);
            
            System.out.println("\n=== PRIVATE KEY (Giữ bí mật, dùng cho code Receiver để giải mã sau này) ===");
            System.out.println(privateKeyBase64);

        } catch (Exception e) {
            System.err.println("Đã xảy ra lỗi khi tạo khóa: " + e.getMessage());
            e.printStackTrace();
        }
    }
}