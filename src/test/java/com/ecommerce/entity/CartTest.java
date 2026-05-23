package com.ecommerce.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour Cart.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * Cart.addItem() et removeItem() maintiennent la relation bidirectionnelle
 * entre Cart et CartItem. Un bug ici causerait des paniers incohérents :
 * items orphelins ou item.cart == null après suppression.
 */
class CartTest {

    private Cart cart;
    private CartItem item1;
    private CartItem item2;

    @BeforeEach
    void setUp() {
        cart = new Cart(1L);
        item1 = new CartItem(101L, "Laptop", new BigDecimal("999.99"), 1, "img1.png", 1L);
        item2 = new CartItem(102L, "Mouse",  new BigDecimal("29.99"),  2, "img2.png", 1L);
    }

    // ── addItem ───────────────────────────────────────────────────────────────

    @Test
    void addItem_addsToList() {
        cart.addItem(item1);
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void addItem_setsCartOnItem() {
        cart.addItem(item1);
        assertSame(cart, item1.getCart());
    }

    @Test
    void addItem_multipleItems_allPresent() {
        cart.addItem(item1);
        cart.addItem(item2);
        assertEquals(2, cart.getItems().size());
    }

    // ── removeItem ────────────────────────────────────────────────────────────

    @Test
    void removeItem_removesFromList() {
        cart.addItem(item1);
        cart.addItem(item2);
        cart.removeItem(item1);
        assertEquals(1, cart.getItems().size());
        assertFalse(cart.getItems().contains(item1));
    }

    @Test
    void removeItem_setsCartNullOnItem() {
        cart.addItem(item1);
        cart.removeItem(item1);
        assertNull(item1.getCart());
    }

    @Test
    void removeItem_nonExistentItem_doesNotThrow() {
        // Retirer un item jamais ajouté ne doit pas lever d'exception
        assertDoesNotThrow(() -> cart.removeItem(item1));
    }

    // ── userId ────────────────────────────────────────────────────────────────

    @Test
    void getUserId_returnsConstructorValue() {
        assertEquals(1L, cart.getUserId());
    }

    @Test
    void setUserId_updatesValue() {
        cart.setUserId(99L);
        assertEquals(99L, cart.getUserId());
    }

    // ── Empty cart ────────────────────────────────────────────────────────────

    @Test
    void newCart_hasNoItems() {
        assertTrue(cart.getItems().isEmpty());
    }
}
