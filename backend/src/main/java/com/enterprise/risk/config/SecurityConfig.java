package com.enterprise.risk.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String securityMode;
    private final List<String> allowedOrigins;
    private final String issuerUri;
    private final String expectedAudience;

    public SecurityConfig(
            @Value("${app.security.mode:open}") String securityMode,
            @Value("${app.cors.allowed-origins:http://localhost:5173}") List<String> allowedOrigins,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:https://dev-1shkkgz44qulrkdg.us.auth0.com/}") String issuerUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.audiences:https://enterprise-risk-api}") String expectedAudience) {
        this.securityMode = securityMode;
        this.allowedOrigins = allowedOrigins;
        this.issuerUri = issuerUri;
        this.expectedAudience = expectedAudience;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults());

        if ("jwt".equalsIgnoreCase(securityMode)) {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health").permitAll()
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().denyAll())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));
        } else {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health").permitAll()
                            .requestMatchers("/api/**").permitAll()
                            .anyRequest().denyAll());
        }

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            List<String> audiences = jwt.getAudience();
            if (audiences != null && audiences.contains(expectedAudience)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "The required audience is missing", null));
        };
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
