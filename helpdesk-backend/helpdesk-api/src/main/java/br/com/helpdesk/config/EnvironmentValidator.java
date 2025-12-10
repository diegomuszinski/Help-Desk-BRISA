package br.com.helpdesk.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Validates critical environment variables on application startup.
 * Prevents application from starting with invalid or missing configuration.
 */
@Slf4j
@Component
public class EnvironmentValidator {

    @Value("${api.security.token.secret:#{null}}")
    private String jwtSecret;

    @Value("${spring.datasource.url:#{null}}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:#{null}}")
    private String datasourcePassword;

    @Value("${file.storage.location:./uploads}")
    private String fileStorageLocation;

    @EventListener(ApplicationReadyEvent.class)
    public void validateEnvironment() {
        log.info("🔍 Validating environment configuration...");

        boolean hasErrors = false;

        // Validate JWT Secret
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            log.error("❌ JWT_SECRET is not configured! Please set the JWT_SECRET environment variable.");
            hasErrors = true;
        } else if (jwtSecret.length() < 32) {
            log.error("❌ JWT_SECRET is too short! Minimum length is 32 characters. Current length: {}", jwtSecret.length());
            hasErrors = true;
        } else {
            log.info("✅ JWT_SECRET is configured (length: {} characters)", jwtSecret.length());
        }

        // Validate Database URL
        if (datasourceUrl == null || datasourceUrl.trim().isEmpty()) {
            log.error("❌ DATABASE_URL is not configured! Please set the DATABASE_URL environment variable.");
            hasErrors = true;
        } else {
            log.info("✅ Database URL is configured: {}", maskDatabaseUrl(datasourceUrl));
        }

        // Validate Database Username
        if (datasourceUsername == null || datasourceUsername.trim().isEmpty()) {
            log.warn("⚠️  DATABASE_USERNAME is not configured! Using default: postgres");
        } else {
            log.info("✅ Database username is configured: {}", datasourceUsername);
        }

        // Validate Database Password
        if (datasourcePassword == null || datasourcePassword.trim().isEmpty()) {
            log.error("❌ DATABASE_PASSWORD is not configured! Please set the DATABASE_PASSWORD environment variable.");
            hasErrors = true;
        } else {
            log.info("✅ Database password is configured (length: {} characters)", datasourcePassword.length());
        }

        // Validate File Storage Location
        java.io.File uploadDir = new java.io.File(fileStorageLocation);
        if (!uploadDir.exists()) {
            log.info("📁 Creating upload directory: {}", fileStorageLocation);
            if (uploadDir.mkdirs()) {
                log.info("✅ Upload directory created successfully");
            } else {
                log.error("❌ Failed to create upload directory: {}", fileStorageLocation);
                hasErrors = true;
            }
        } else if (!uploadDir.canWrite()) {
            log.error("❌ Upload directory is not writable: {}", fileStorageLocation);
            hasErrors = true;
        } else {
            log.info("✅ Upload directory is configured: {}", fileStorageLocation);
        }

        // Check optional environment variables
        checkOptionalVariable("SPRING_PROFILES_ACTIVE", System.getenv("SPRING_PROFILES_ACTIVE"), "dev");
        checkOptionalVariable("SERVER_PORT", System.getenv("SERVER_PORT"), "8080");

        if (hasErrors) {
            log.error("❌ Application startup failed due to invalid environment configuration!");
            log.error("Please check the following:");
            log.error("  1. Set JWT_SECRET environment variable (minimum 32 characters)");
            log.error("  2. Set DATABASE_URL environment variable");
            log.error("  3. Set DATABASE_PASSWORD environment variable");
            log.error("  4. Ensure upload directory is writable");

            throw new IllegalStateException(
                "Application startup failed: Invalid environment configuration. " +
                "Please check the logs above for details."
            );
        }

        log.info("✅ All environment variables validated successfully!");
    }

    /**
     * Masks sensitive parts of database URL for logging
     */
    private String maskDatabaseUrl(String url) {
        if (url == null) return null;

        // Mask password in URL if present
        return url.replaceAll(":[^:@]+@", ":****@");
    }

    /**
     * Checks and logs optional environment variables with defaults
     */
    private void checkOptionalVariable(String name, String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            log.info("ℹ️  {} not set, using default: {}", name, defaultValue);
        } else {
            log.info("✅ {} is configured: {}", name, value);
        }
    }
}
