import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateHash {
    private static final Logger logger = LoggerFactory.getLogger(GenerateHash.class);

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";

        logger.info("=== Gerando Hash BCrypt ===");
        logger.info("Senha: {}", password);
        logger.info("");

        // Gerar 3 hashes diferentes (BCrypt sempre gera hashes diferentes)
        for (int i = 1; i <= 3; i++) {
            String hash = encoder.encode(password);
            logger.info("Hash {}: {}", i, hash);
            logger.info("Verifica: {}", encoder.matches(password, hash));
            logger.info("");
        }

        // Testar os hashes antigos
        logger.info("=== Testando Hashes Existentes ===");
        String[] oldHashes = {
            "$2a$10$N9qo8uLOickgx2ZMRZoMye6J4Qf8mKvjeyELCtU3xbGQEUzLe6T7e",
            "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi"
        };

        for (String hash : oldHashes) {
            logger.info("Hash: {}...", hash.substring(0, 30));
            logger.info("Matches '123456': {}", encoder.matches(password, hash));
            logger.info("");
        }
    }
}
