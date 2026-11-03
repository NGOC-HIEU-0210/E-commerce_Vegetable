package com.hieu.config;

import com.hieu.model.User;
import com.hieu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        syncLocalUser(jwt);
        return new JwtAuthenticationToken(jwt, extractAuthorities(jwt),
                jwt.getClaimAsString("preferred_username"));
    }

    private void syncLocalUser(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || userRepository.existsByUsername(username)) {
            return;
        }

        String email = jwt.getClaimAsString("email");
        // email trong tbl_user là unique: nếu đã bị user khác dùng thì bỏ qua email
        if (email != null && userRepository.existsByEmail(email)) {
            email = null;
        }

        try {
            userRepository.save(User.builder()
                    .username(username)
                    .password("{keycloak}")   // mật khẩu do Keycloak quản lý, không dùng ở đây
                    .email(email)
                    .firstName(jwt.getClaimAsString("given_name"))
                    .lastName(jwt.getClaimAsString("family_name"))
                    .status(true)
                    .build());
        } catch (DataIntegrityViolationException ignored) {
            // hai request đầu tiên chạy song song: bên kia đã tạo xong, bỏ qua
        }
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .<GrantedAuthority>map(r -> new SimpleGrantedAuthority(String.valueOf(r)))
                .toList();
    }
}
