package com.ecommerce.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = System.getenv().getOrDefault(
            "JWT_SECRET", "change-me-in-production");

    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 24 h

    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public static String generate(Long userId, String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .sign(ALGORITHM);
    }

    public static DecodedJWT verify(String token) {
        return JWT.require(ALGORITHM).build().verify(token);
    }

    public static long getExpirationSeconds() {
        return EXPIRATION_MS / 1000;
    }
}