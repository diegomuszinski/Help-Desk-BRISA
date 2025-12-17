import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";

        System.out.println("=== Gerando Hash BCrypt ===");
        System.out.println("Senha: " + password);
        System.out.println("");

        // Gerar 3 hashes diferentes (BCrypt sempre gera hashes diferentes)
        for (int i = 1; i <= 3; i++) {
            String hash = encoder.encode(password);
            System.out.println("Hash " + i + ": " + hash);
            System.out.println("Verifica: " + encoder.matches(password, hash));
            System.out.println("");
        }

        // Testar os hashes antigos
        System.out.println("=== Testando Hashes Existentes ===");
        String[] oldHashes = {
            "$2a$10$N9qo8uLOickgx2ZMRZoMye6J4Qf8mKvjeyELCtU3xbGQEUzLe6T7e",
            "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi"
        };

        for (String hash : oldHashes) {
            System.out.println("Hash: " + hash.substring(0, 30) + "...");
            System.out.println("Matches '123456': " + encoder.matches(password, hash));
            System.out.println("");
        }
    }
}
