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
                        // ---- Công khai ----
                        .requestMatchers("/", "/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html", "/error", "/UploadFiles/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ---- Chỉ admin: dashboard, vai trò, trạng thái đơn ----
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/roles", "/api/roles/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/order-statuses", "/api/order-statuses/**").hasAuthority("ADMIN")

                        // ---- Người dùng: tự quản lý hồ sơ, còn lại chỉ admin ----
                        .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                        .requestMatchers("/api/users", "/api/users/**").hasAuthority("ADMIN")

                        // ---- Danh mục ----
                        .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/paged",
                                "/api/categories/search").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/categories/active", "/api/categories/{id}").permitAll()
                        .requestMatchers("/api/categories", "/api/categories/**").hasAuthority("ADMIN")

                        // ---- Sản phẩm ----
                        .requestMatchers(HttpMethod.GET, "/api/products").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/products/category/{categoryId}", "/api/products/search",
                                "/api/products/latest", "/api/products/{id}").permitAll()
                        .requestMatchers("/api/products", "/api/products/**").hasAuthority("ADMIN")

                        // ---- Mã giảm giá ----
                        .requestMatchers(HttpMethod.GET, "/api/coupons/active").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/coupons/apply").authenticated()
                        .requestMatchers("/api/coupons", "/api/coupons/**").hasAuthority("ADMIN")

                        // ---- Yêu thích: xem số lượt thích công khai, còn lại cần đăng nhập ----
                        .requestMatchers(HttpMethod.GET, "/api/favourites/count/{productId}").permitAll()

                        // ---- Đơn hàng: phần của admin; còn lại user (chủ đơn) xử lý trong controller ----
                        .requestMatchers(HttpMethod.GET, "/api/orders", "/api/orders/paged",
                                "/api/orders/search", "/api/orders/user/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status",
                                "/api/orders/*/payment-status").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/*").hasAuthority("ADMIN")

                        // Còn lại (giỏ hàng, yêu thích, đặt hàng, đơn của mình...) cần đăng nhập
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
