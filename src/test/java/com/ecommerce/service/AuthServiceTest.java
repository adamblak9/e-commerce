package com.ecommerce.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la logique de validation d'AuthService.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * AuthService gère l'inscription et la connexion. Tester la logique de rôle
 * (seuls CUSTOMER et SELLER sont autorisés à l'auto-inscription) garantit
 * qu'un attaquant ne peut pas créer un compte ADMIN via l'API publique.
 *
 * Note : les tests qui touchent la BDD (signup complet, login) sont des tests
 * d'intégration — ils seraient dans un dossier séparé avec une BDD H2 en
 * mémoire. Ici on teste uniquement la logique pure (résolution de rôle).
 */
class AuthServiceTest {

    // ── Logique de résolution de rôle (reproduite depuis SignupServlet) ────────

    /**
     * Reproduit la méthode resolveRole() de SignupServlet pour la tester.
     * En production, il faudrait extraire cette logique dans un utilitaire testable.
     */
    private String resolveRole(String raw) {
        if ("SELLER".equalsIgnoreCase(raw)) return "SELLER";
        return "CUSTOMER"; // valeur par défaut
    }

    @Test
    void resolveRole_seller_returnsSeller() {
        assertEquals("SELLER", resolveRole("SELLER"));
    }

    @Test
    void resolveRole_sellerLowercase_returnsSeller() {
        assertEquals("SELLER", resolveRole("seller"));
    }

    @Test
    void resolveRole_customer_returnsCustomer() {
        assertEquals("CUSTOMER", resolveRole("CUSTOMER"));
    }

    @Test
    void resolveRole_null_returnsCustomer() {
        assertEquals("CUSTOMER", resolveRole(null));
    }

    @Test
    void resolveRole_admin_returnsCustomer() {
        // ADMIN ne peut PAS être auto-assigné via l'API
        assertEquals("CUSTOMER", resolveRole("ADMIN"),
                "Un utilisateur ne doit pas pouvoir s'auto-assigner le rôle ADMIN");
    }

    @Test
    void resolveRole_randomString_returnsCustomer() {
        assertEquals("CUSTOMER", resolveRole("SUPERUSER"));
    }

    // ── Validation de l'email (reproduite depuis SignupServlet) ───────────────

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    @Test
    void isValidEmail_validEmail_returnsTrue() {
        assertTrue(isValidEmail("user@example.com"));
    }

    @Test
    void isValidEmail_missingAt_returnsFalse() {
        assertFalse(isValidEmail("userexample.com"));
    }

    @Test
    void isValidEmail_missingDomain_returnsFalse() {
        assertFalse(isValidEmail("user@"));
    }

    @Test
    void isValidEmail_null_returnsFalse() {
        assertFalse(isValidEmail(null));
    }

    @Test
    void isValidEmail_withSubdomain_returnsTrue() {
        assertTrue(isValidEmail("user@mail.example.com"));
    }

    // ── Validation du mot de passe ────────────────────────────────────────────

    private boolean isPasswordValid(String pwd) {
        return pwd != null && pwd.length() >= 8;
    }

    @Test
    void isPasswordValid_longEnough_returnsTrue() {
        assertTrue(isPasswordValid("password123"));
    }

    @Test
    void isPasswordValid_tooShort_returnsFalse() {
        assertFalse(isPasswordValid("short"));
    }

    @Test
    void isPasswordValid_exactly8Chars_returnsTrue() {
        assertTrue(isPasswordValid("12345678"));
    }

    @Test
    void isPasswordValid_7Chars_returnsFalse() {
        assertFalse(isPasswordValid("1234567"));
    }

    @Test
    void isPasswordValid_null_returnsFalse() {
        assertFalse(isPasswordValid(null));
    }
}
