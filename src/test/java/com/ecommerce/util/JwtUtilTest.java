package com.ecommerce.util;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour JwtUtil.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * JWT est la seule barrière de sécurité entre le client et l'API.
 * Un token mal signé ou mal vérifié permettrait à n'importe qui d'usurper
 * une identité. Ces tests confirment que generate() produit un token valide
 * et que verify() rejette les tokens altérés.
 */
class JwtUtilTest {

    // ── generate + verify (happy path) ───────────────────────────────────────

    @Test
    void generateAndVerify_validToken_succeeds() {
        String token = JwtUtil.generate(42L, "alice@example.com", "CUSTOMER");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        DecodedJWT decoded = JwtUtil.verify(token);
        assertNotNull(decoded);
    }

    @Test
    void verify_extractsUserId() {
        String token = JwtUtil.generate(7L, "bob@example.com", "SELLER");
        DecodedJWT decoded = JwtUtil.verify(token);
        assertEquals(7L, decoded.getClaim("userId").asLong());
    }

    @Test
    void verify_extractsEmail() {
        String token = JwtUtil.generate(1L, "carol@example.com", "ADMIN");
        DecodedJWT decoded = JwtUtil.verify(token);
        assertEquals("carol@example.com", decoded.getSubject());
    }

    @Test
    void verify_extractsRole() {
        String token = JwtUtil.generate(3L, "user@example.com", "SELLER");
        DecodedJWT decoded = JwtUtil.verify(token);
        assertEquals("SELLER", decoded.getClaim("role").asString());
    }

    // ── Tokens invalides ──────────────────────────────────────────────────────

    @Test
    void verify_tamperedToken_throwsException() {
        String token = JwtUtil.generate(1L, "user@example.com", "CUSTOMER");
        // Altérer le token en changeant un caractère dans la signature
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThrows(JWTVerificationException.class, () -> JwtUtil.verify(tampered));
    }

    @Test
    void verify_randomString_throwsException() {
        assertThrows(JWTVerificationException.class,
                () -> JwtUtil.verify("this.is.not.a.jwt"));
    }

    @Test
    void verify_emptyString_throwsException() {
        assertThrows(Exception.class, () -> JwtUtil.verify(""));
    }

    // ── getExpirationSeconds ──────────────────────────────────────────────────

    @Test
    void getExpirationSeconds_isPositive() {
        assertTrue(JwtUtil.getExpirationSeconds() > 0);
    }

    @Test
    void getExpirationSeconds_is24Hours() {
        // 24h = 86400 secondes
        assertEquals(86400L, JwtUtil.getExpirationSeconds());
    }
}
