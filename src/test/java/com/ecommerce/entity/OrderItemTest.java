package com.ecommerce.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour OrderItem.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * getSubtotal() est un calcul financier : price × quantity.
 * Une erreur ici se propage directement dans les totaux de commande et de
 * paiement. Tester cette méthode protège contre des régressions coûteuses.
 */
class OrderItemTest {

    private OrderItem buildItem(BigDecimal price, int qty) {
        OrderItem item = new OrderItem();
        item.setPrice(price);
        item.setQuantity(qty);
        return item;
    }

    // ── getSubtotal ───────────────────────────────────────────────────────────

    @Test
    void getSubtotal_normalValues_returnsCorrect() {
        OrderItem item = buildItem(new BigDecimal("25.00"), 3);
        assertEquals(0, new BigDecimal("75.00").compareTo(item.getSubtotal()));
    }

    @Test
    void getSubtotal_quantityOne_equalsPrice() {
        OrderItem item = buildItem(new BigDecimal("99.99"), 1);
        assertEquals(0, new BigDecimal("99.99").compareTo(item.getSubtotal()));
    }

    @Test
    void getSubtotal_nullPrice_returnsZero() {
        OrderItem item = new OrderItem();
        item.setQuantity(5);
        // price est null → doit retourner ZERO
        assertEquals(0, BigDecimal.ZERO.compareTo(item.getSubtotal()));
    }

    @Test
    void getSubtotal_nullQuantity_returnsZero() {
        OrderItem item = new OrderItem();
        item.setPrice(new BigDecimal("10.00"));
        // quantity est null → doit retourner ZERO
        assertEquals(0, BigDecimal.ZERO.compareTo(item.getSubtotal()));
    }

    @Test
    void getSubtotal_bothNull_returnsZero() {
        OrderItem item = new OrderItem();
        assertEquals(0, BigDecimal.ZERO.compareTo(item.getSubtotal()));
    }

    @Test
    void getSubtotal_withDecimalPrice_isAccurate() {
        OrderItem item = buildItem(new BigDecimal("12.50"), 4);
        assertEquals(0, new BigDecimal("50.00").compareTo(item.getSubtotal()));
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    @Test
    void setProductName_updatesValue() {
        OrderItem item = new OrderItem();
        item.setProductName("Keyboard");
        assertEquals("Keyboard", item.getProductName());
    }

    @Test
    void setImageUrl_updatesValue() {
        OrderItem item = new OrderItem();
        item.setImageUrl("http://example.com/img.png");
        assertEquals("http://example.com/img.png", item.getImageUrl());
    }

    @Test
    void setOrderId_updatesValue() {
        OrderItem item = new OrderItem();
        item.setOrderId(42L);
        assertEquals(42L, item.getOrderId());
    }
}
