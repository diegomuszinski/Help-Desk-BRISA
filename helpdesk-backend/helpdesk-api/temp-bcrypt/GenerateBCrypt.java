import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hash = encoder.encode(password);
        System.out.println("Hash BCrypt para '123456':");
        System.out.println(hash);
        System.out.println("");
        System.out.println("Testando match:");
        System.out.println(encoder.matches(password, hash));
    }
}
