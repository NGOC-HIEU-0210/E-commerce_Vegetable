package com.hieu.config;

import lombok.RequiredArgsConstructor;
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
public class SecurityConfig {

    private final KeycloakJwtConverter keycloakJwtConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html", "/error", "/UploadFiles/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()


                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/coupons", "/api/coupons/paged").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/paged", "/api/users/search").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders", "/api/orders/paged", "/api/orders/search").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories/**", "/api/coupons").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/coupons/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/coupons/**").hasAuthority("ADMIN")


                        .requestMatchers("/api/users/me").authenticated()


                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/coupons/active").permitAll()

                        .anyRequest().authenticated())

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter)));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
