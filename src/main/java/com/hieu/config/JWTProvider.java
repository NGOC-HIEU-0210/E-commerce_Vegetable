package com.hieu.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JWTProvider {

    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            JWTConstant.SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    private static final long EXPIRATION_MS = Long.parseLong(
            System.getProperty("jwt.expiration-ms", "86400000"));

    public String generateToken(String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claims(extraClaims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public boolean isValid(String token, String username) {
        try {
            Claims claims = parse(token);
            return username.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(KEY).build()
                .parseSignedClaims(token).getPayload();
    }
}
