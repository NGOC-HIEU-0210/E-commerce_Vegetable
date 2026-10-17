package com.hieu.config;

public final class JWTConstant {

    public static final String SECRET_KEY = System.getProperty(
            "jwt.secret",
            System.getenv().getOrDefault("JWT_SECRET",
                    "aGlldS13ZWItaW5kLW1hcnQtand0LXNlY3JldC1rZXktZm9yLXNwcmluZy1ib290LTMuNS1qd3Qtc2lnbmluZy1rZXktMjU2LWJpdHMtbGVuZ3Ro"));

    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    private JWTConstant() {
    }
}
