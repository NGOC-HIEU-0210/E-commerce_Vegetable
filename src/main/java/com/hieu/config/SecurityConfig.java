
package com.hieu.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig{

    private final KeycloakJwtConverter keycloakJwtConverter;

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        if (rateLimitEnabled) {
            http.addFilterBefore(
                    new RateLimitFilter(),
                    UsernamePasswordAuthenticationFilter.class
            );
        }

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Công khai
                        .requestMatchers(
                                "/",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error",
                                "/UploadFiles/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Admin
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/roles", "/api/roles/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/order-statuses", "/api/order-statuses/**").hasAuthority("ADMIN")

                        // User
                        .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                        .requestMatchers("/api/users", "/api/users/**").hasAuthority("ADMIN")

                        // Categories
                        .requestMatchers(HttpMethod.GET,
                                "/api/categories/active",
                                "/api/categories/{id}"
                        ).permitAll()

                        .requestMatchers("/api/categories", "/api/categories/**")
                        .hasAuthority("ADMIN")

                        // Products
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/category/{categoryId}",
                                "/api/products/search",
                                "/api/products/latest",
                                "/api/products/rescue",
                                "/api/products/{id}"
                        ).permitAll()

                        .requestMatchers("/api/products", "/api/products/**")
                        .hasAuthority("ADMIN")

                        // Coupons
                        .requestMatchers(HttpMethod.GET, "/api/coupons/active")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/coupons/apply")
                        .authenticated()

                        .requestMatchers("/api/coupons", "/api/coupons/**")
                        .hasAuthority("ADMIN")

                        // Favourites
                        .requestMatchers(HttpMethod.GET, "/api/favourites/count/{productId}")
                        .permitAll()

                        // Orders - Admin
                        .requestMatchers(HttpMethod.GET,
                                "/api/orders",
                                "/api/orders/paged",
                                "/api/orders/search"
                        ).hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/orders/*/status",
                                "/api/orders/*/payment-status"
                        ).hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/orders/*")
                        .hasAuthority("ADMIN")

                        // Các API còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter))
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
