package com.ecommerce.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour RoleGuard.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * RoleGuard est appelé au début de chaque endpoint protégé pour vérifier
 * que l'utilisateur a le bon rôle. Si require() retourne true pour un mauvais
 * rôle, ou false pour un rôle autorisé, toute la sécurité de l'API s'effondre.
 * Ces tests constituent une dernière ligne de défense contre les régressions.
 */
class RoleGuardTest {

    private HttpServletRequest  req;
    private HttpServletResponse res;
    private StringWriter        responseBody;

    @BeforeEach
    void setUp() throws IOException {
        req = mock(HttpServletRequest.class);
        res = mock(HttpServletResponse.class);

        responseBody = new StringWriter();
        when(res.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    // ── require — rôle autorisé ───────────────────────────────────────────────

    @Test
    void require_userHasExactRole_returnsTrue() throws IOException {
        when(req.getAttribute("role")).thenReturn("ADMIN");
        assertTrue(RoleGuard.require(req, res, "ADMIN"));
    }

    @Test
    void require_userHasOneOfMultipleAllowedRoles_returnsTrue() throws IOException {
        when(req.getAttribute("role")).thenReturn("SELLER");
        assertTrue(RoleGuard.require(req, res, "ADMIN", "SELLER"));
    }

    @Test
    void require_customerAccessingCustomerRoute_returnsTrue() throws IOException {
        when(req.getAttribute("role")).thenReturn("CUSTOMER");
        assertTrue(RoleGuard.require(req, res, "CUSTOMER", "ADMIN"));
    }

    // ── require — rôle refusé ─────────────────────────────────────────────────

    @Test
    void require_userHasWrongRole_returnsFalse() throws IOException {
        when(req.getAttribute("role")).thenReturn("CUSTOMER");
        assertFalse(RoleGuard.require(req, res, "ADMIN"));
    }

    @Test
    void require_wrongRole_sets403Status() throws IOException {
        when(req.getAttribute("role")).thenReturn("CUSTOMER");
        RoleGuard.require(req, res, "ADMIN");
        verify(res).setStatus(403);
    }

    @Test
    void require_wrongRole_writesErrorJson() throws IOException {
        when(req.getAttribute("role")).thenReturn("SELLER");
        RoleGuard.require(req, res, "ADMIN");
        String body = responseBody.toString();
        assertTrue(body.contains("Forbidden") || body.contains("ADMIN"),
                "La réponse doit mentionner l'accès refusé et le rôle requis");
    }

    @Test
    void require_nullRole_returnsFalse() throws IOException {
        when(req.getAttribute("role")).thenReturn(null);
        assertFalse(RoleGuard.require(req, res, "ADMIN"));
    }

    // ── require — insensibilité à la casse ───────────────────────────────────

    @Test
    void require_caseInsensitive_matchesLowercase() throws IOException {
        when(req.getAttribute("role")).thenReturn("admin");
        // RoleGuard utilise equalsIgnoreCase → "admin" doit matcher "ADMIN"
        assertTrue(RoleGuard.require(req, res, "ADMIN"));
    }

    @Test
    void require_caseInsensitive_matchesMixedCase() throws IOException {
        when(req.getAttribute("role")).thenReturn("Seller");
        assertTrue(RoleGuard.require(req, res, "SELLER"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @Test
    void getUserId_returnsAttributeAsLong() {
        when(req.getAttribute("userId")).thenReturn(42L);
        assertEquals(42L, RoleGuard.getUserId(req));
    }

    @Test
    void getUserId_nullAttribute_returnsNull() {
        when(req.getAttribute("userId")).thenReturn(null);
        assertNull(RoleGuard.getUserId(req));
    }

    @Test
    void getRole_returnsRoleAttribute() {
        when(req.getAttribute("role")).thenReturn("SELLER");
        assertEquals("SELLER", RoleGuard.getRole(req));
    }

    @Test
    void getEmail_returnsEmailAttribute() {
        when(req.getAttribute("email")).thenReturn("seller@example.com");
        assertEquals("seller@example.com", RoleGuard.getEmail(req));
    }
}
