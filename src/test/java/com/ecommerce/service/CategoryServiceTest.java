package com.ecommerce.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour CategoryService — logique pure sans base de données.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * CategoryService contient une méthode toSlug() et isBlank() utilisées à chaque
 * création/modification de catégorie. Un slug incorrect (URL invalide) casserait
 * le référencement et le filtrage des produits par catégorie.
 *
 * Stratégie : on accède aux méthodes privées via Reflection pour les tester
 * directement sans avoir besoin d'une base de données.
 */
class CategoryServiceTest {

    private final CategoryService service = new CategoryService();

    // ── toSlug (via réflexion) ────────────────────────────────────────────────

    private String invokeToSlug(String input) throws Exception {
        Method m = CategoryService.class.getDeclaredMethod("toSlug", String.class);
        m.setAccessible(true);
        return (String) m.invoke(service, input);
    }

    @Test
    void toSlug_normalName_lowercaseHyphenated() throws Exception {
        assertEquals("electronics", invokeToSlug("Electronics"));
    }

    @Test
    void toSlug_nameWithSpaces_replacedByHyphens() throws Exception {
        assertEquals("home-appliances", invokeToSlug("Home Appliances"));
    }

    @Test
    void toSlug_nameWithSpecialChars_removedCorrectly() throws Exception {
        String slug = invokeToSlug("Books & Magazines!");
        // Caractères non alphanumériques sauf tirets doivent être supprimés
        assertFalse(slug.contains("&"));
        assertFalse(slug.contains("!"));
    }

    @Test
    void toSlug_multipleSpaces_singleHyphen() throws Exception {
        String slug = invokeToSlug("Sports   Outdoor");
        assertFalse(slug.contains("  "));
        assertTrue(slug.contains("-"));
    }

    @Test
    void toSlug_trailingSpaces_trimmed() throws Exception {
        String slug = invokeToSlug("  Phones  ");
        assertFalse(slug.startsWith("-"));
        assertFalse(slug.endsWith("-"));
    }

    @Test
    void toSlug_alreadyLowercase_unchanged() throws Exception {
        assertEquals("clothing", invokeToSlug("clothing"));
    }

    // ── isBlank (via réflexion) ───────────────────────────────────────────────

    private boolean invokeIsBlank(String input) throws Exception {
        Method m = CategoryService.class.getDeclaredMethod("isBlank", String.class);
        m.setAccessible(true);
        return (boolean) m.invoke(service, input);
    }

    @Test
    void isBlank_null_returnsTrue() throws Exception {
        assertTrue(invokeIsBlank(null));
    }

    @Test
    void isBlank_emptyString_returnsTrue() throws Exception {
        assertTrue(invokeIsBlank(""));
    }

    @Test
    void isBlank_whitespaceOnly_returnsTrue() throws Exception {
        assertTrue(invokeIsBlank("   "));
    }

    @Test
    void isBlank_validString_returnsFalse() throws Exception {
        assertFalse(invokeIsBlank("Electronics"));
    }
}
