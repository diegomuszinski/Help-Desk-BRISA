package br.com.brisabr.helpdesk_api.auth;

import br.com.brisabr.helpdesk_api.user.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para TokenService.
 */
@DisplayName("TokenService - Testes Unitários")
class TokenServiceTest {

    private TokenService tokenService;
    private final String testSecret = "test-secret-key-for-testing-purposes-only";

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", testSecret);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido")
    void shouldGenerateValidToken() {
        // Given
        User user = createTestUser();

        // When
        String token = tokenService.generateToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT tem 3 partes
    }

    @Test
    @DisplayName("Token gerado deve conter subject correto")
    void tokenShouldContainCorrectSubject() {
        // Given
        User user = createTestUser();

        // When
        String token = tokenService.generateToken(user);

        // Then
        String subject = JWT.decode(token).getSubject();
        assertThat(subject).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Token gerado deve conter claims personalizados")
    void tokenShouldContainCustomClaims() {
        // Given
        User user = createTestUser();

        // When
        String token = tokenService.generateToken(user);

        // Then
        var decodedToken = JWT.decode(token);
        assertThat(decodedToken.getClaim("name").asString()).isEqualTo(user.getNome());
        assertThat(decodedToken.getClaim("role").asString()).isEqualTo(user.getPerfil());
    }

    @Test
    @DisplayName("Token gerado deve ter issuer correto")
    void tokenShouldHaveCorrectIssuer() {
        // Given
        User user = createTestUser();

        // When
        String token = tokenService.generateToken(user);

        // Then
        String issuer = JWT.decode(token).getIssuer();
        assertThat(issuer).isEqualTo("helpdesk-api");
    }

    @Test
    @DisplayName("Deve validar token válido com sucesso")
    void shouldValidateValidToken() {
        // Given
        User user = createTestUser();
        String token = tokenService.generateToken(user);

        // When
        String email = tokenService.validateToken(token);

        // Then
        assertThat(email).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Deve retornar string vazia para token inválido")
    void shouldReturnEmptyStringForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        String result = tokenService.validateToken(invalidToken);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar string vazia para token expirado")
    void shouldReturnEmptyStringForExpiredToken() {
        // Given - Criar token com secret diferente
        Algorithm algorithm = Algorithm.HMAC256("wrong-secret");
        String tokenWithWrongSecret = JWT.create()
                .withIssuer("helpdesk-api")
                .withSubject("test@test.com")
                .sign(algorithm);

        // When
        String result = tokenService.validateToken(tokenWithWrongSecret);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve gerar token mesmo com campos nulos em claims")
    void shouldGenerateTokenEvenWithNullClaims() {
        // Given
        User user = new User();
        user.setEmail("test@test.com");
        user.setNome(null); // Nome nulo não deve impedir geração
        user.setPerfil(null); // Perfil nulo não deve impedir geração

        // When
        String token = tokenService.generateToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setNome("Test User");
        user.setEmail("test@test.com");
        user.setPerfil("admin");
        user.setSenha("encoded-password");
        return user;
    }
}
