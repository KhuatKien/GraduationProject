package com.phenikaa.api_gateway.config;

import com.phenikaa.api_gateway.security.JwtAuthenticationManager;
import com.phenikaa.api_gateway.security.ServerHttpBearerAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
            JwtAuthenticationManager jwtAuthManager,
            ServerHttpBearerAuthenticationConverter authConverter) {

        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtAuthManager);
        authenticationWebFilter.setServerAuthenticationConverter(authConverter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> {
                })
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // cho phép tất cả các options
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/api/internal/users/**").permitAll()
                        .pathMatchers("/api/category/user/**").permitAll()
                        .pathMatchers("/api/tour/internal/**").permitAll()
                        .pathMatchers("/api/tour/chat/**").permitAll()
                        .pathMatchers("/ws/notifications/**").permitAll()
                        .pathMatchers("/api/booking/webhook/**").permitAll() // Direct booking webhook
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/users/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .pathMatchers("/api/category/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/tour/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/tour/user/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .pathMatchers("/api/booking/admin/**").hasAnyRole("ADMIN")
                        .pathMatchers("/api/booking/user/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .pathMatchers("/api/booking/webhook/**").permitAll()
                        .pathMatchers("/api/notifications/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .pathMatchers("/api/promotions/user/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .pathMatchers("/api/promotions/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/reviews/user/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .pathMatchers("/api/reviews/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/campaigns/**").hasRole("ADMIN")
                        .pathMatchers("/api/campaigns/calculate-discount/**").hasAnyRole("CUSTOMER", "ADMIN")

                        .anyExchange().authenticated())
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public ServerHttpBearerAuthenticationConverter bearerAuthenticationConverter() {
        return new ServerHttpBearerAuthenticationConverter();
    }

    @Bean
    @Order(-1)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "https://*.ngrok-free.dev",
                "https://*.ngrok.io",
                "*" // Allow all origins for webhook
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false); // Disable for webhook compatibility
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

}
