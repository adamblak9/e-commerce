package com.ecommerce.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité Product.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * Product.isAvailable() combine deux conditions (statut ACTIVE + stock > 0).
 * Un bug dans cette méthode ferait apparaître comme disponibles des produits
 * en rupture de stock ou désactivés — un problème critique pour l'e-commerce.
 */
class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Laptop", "Good laptop", new BigDecimal("999.99"), 10, "Electronics", 1L);
    }

    // ── isAvailable ───────────────────────────────────────────────────────────

    @Test
    void isAvailable_activeWithStock_returnsTrue() {
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setStockQuantity(5);
        assertTrue(product.isAvailable());
    }

    @Test
    void isAvailable_activeButNoStock_returnsFalse() {
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setStockQuantity(0);
        assertFalse(product.isAvailable());
    }

    @Test
    void isAvailable_inactiveWithStock_returnsFalse() {
        product.setStatus(Product.ProductStatus.INACTIVE);
        product.setStockQuantity(10);
        assertFalse(product.isAvailable());
    }

    @Test
    void isAvailable_outOfStock_returnsFalse() {
        product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        product.setStockQuantity(5);
        assertFalse(product.isAvailable());
    }

    @Test
    void isAvailable_negativeStock_returnsFalse() {
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setStockQuantity(-1);
        assertFalse(product.isAvailable());
    }

    // ── Default status ────────────────────────────────────────────────────────

    @Test
    void defaultStatus_isActive() {
        // Le statut par défaut est ACTIVE
        assertEquals(Product.ProductStatus.ACTIVE, product.getStatus());
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    @Test
    void setPrice_updatesCorrectly() {
        BigDecimal newPrice = new BigDecimal("1299.00");
        product.setPrice(newPrice);
        assertEquals(0, newPrice.compareTo(product.getPrice()));
    }

    @Test
    void setStockQuantity_updatesCorrectly() {
        product.setStockQuantity(50);
        assertEquals(50, product.getStockQuantity());
    }

    @Test
    void setCategory_updatesCorrectly() {
        product.setCategory("Phones");
        assertEquals("Phones", product.getCategory());
    }

    // ── equals / hashCode ─────────────────────────────────────────────────────

    @Test
    void equals_sameObject_returnsTrue() {
        assertEquals(product, product);
    }

    @Test
    void equals_differentType_returnsFalse() {
        assertNotEquals(product, "some string");
    }

    @Test
    void toString_containsNameAndPrice() {
        String s = product.toString();
        assertTrue(s.contains("Laptop"));
        assertTrue(s.contains("999.99"));
    }
}
