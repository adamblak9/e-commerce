package com.ecommerce.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PasswordUtil.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * Le hachage et la vérification de mots de passe protègent les comptes
 * utilisateurs. Si hash() ne produit pas un hash BCrypt valide, ou si
 * verify() accepte de mauvais mots de passe, la sécurité est compromise.
 */
class PasswordUtilTest {

    // ── hash ─────────────────────────────────────────────────────────────────

    @Test
    void hash_returnsNonNullNonEmpty() {
        String hash = PasswordUtil.hash("password123");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hash_returnsBcryptFormatted() {
        String hash = PasswordUtil.hash("mySecret!");
        // BCrypt hashes commencent toujours par $2a$ ou $2b$
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"),
                "Le hash doit être au format BCrypt");
    }

    @Test
    void hash_sameInput_producesDifferentHashes() {
        // BCrypt utilise un salt aléatoire → deux appels donnent des hashes différents
        String hash1 = PasswordUtil.hash("same");
        String hash2 = PasswordUtil.hash("same");
        assertNotEquals(hash1, hash2,
                "Deux hachages du même mot de passe doivent être différents (salt)");
    }

    // ── verify ────────────────────────────────────────────────────────────────

    @Test
    void verify_correctPassword_returnsTrue() {
        String hash = PasswordUtil.hash("correctPassword");
        assertTrue(PasswordUtil.verify("correctPassword", hash));
    }

    @Test
    void verify_wrongPassword_returnsFalse() {
        String hash = PasswordUtil.hash("correctPassword");
        assertFalse(PasswordUtil.verify("wrongPassword", hash));
    }

    @Test
    void verify_emptyPassword_doesNotMatchNonEmptyHash() {
        String hash = PasswordUtil.hash("secret");
        assertFalse(PasswordUtil.verify("", hash));
    }

    @Test
    void verify_casesSensitive_returnsFalse() {
        String hash = PasswordUtil.hash("Password123");
        // Le mot de passe est sensible à la casse
        assertFalse(PasswordUtil.verify("password123", hash));
    }

    @Test
    void verify_longPassword_works() {
        String longPwd = "a".repeat(72); // BCrypt tronque à 72 caractères
        String hash = PasswordUtil.hash(longPwd);
        assertTrue(PasswordUtil.verify(longPwd, hash));
    }
}
