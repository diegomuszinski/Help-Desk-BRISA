package br.com.brisabr.helpdesk_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Auth endpoints - públicos (sem versão)
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()

                        // API v1 - Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/v1/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/api/test/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/api/test/**").permitAll()

                        // Health checks e actuator endpoints - públicos para monitoramento
                        .requestMatchers("/actuator/**").permitAll()

                        // Swagger/OpenAPI - público em desenvolvimento
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // API v1 - Endpoints autenticados
                        .requestMatchers(HttpMethod.GET, "/v1/api/categorias").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/api/prioridades").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/api/anexos/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/api/tickets/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
