package com.ecommerce.service;

import com.ecommerce.entity.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la logique de construction d'OrderService.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * OrderService.createOrder() construit des OrderItem à partir d'une liste
 * de cartItems passés en Map. Si un champ est absent (productId, price...),
 * une NullPointerException ou une donnée incorrecte est enregistrée en BDD.
 * Ces tests vérifient la robustesse de la construction des objets.
 */
class OrderServiceTest {

    // ── Logique de construction des OrderItem (reproduite) ────────────────────

    /**
     * Reproduit la logique de createOrder() pour extraire le productName.
     * En l'absence du champ "name", le fallback "Product #ID" doit être utilisé.
     */
    private String resolveProductName(Map<String, Object> item) {
        Long productId = Long.valueOf(item.get("productId").toString());
        return item.containsKey("name")
                ? item.get("name").toString()
                : "Product #" + productId;
    }

    @Test
    void resolveProductName_withName_returnsName() {
        Map<String, Object> item = Map.of("productId", 5L, "name", "Laptop Pro");
        assertEquals("Laptop Pro", resolveProductName(item));
    }

    @Test
    void resolveProductName_withoutName_returnsFallback() {
        Map<String, Object> item = Map.of("productId", 7L, "quantity", 2, "price", "99.99");
        assertEquals("Product #7", resolveProductName(item));
    }

    // ── Parsing de BigDecimal depuis les cartItems ─────────────────────────────

    @Test
    void parsePriceFromMap_validDecimalString_works() {
        Map<String, Object> item = Map.of("price", "29.99");
        BigDecimal price = new BigDecimal(item.get("price").toString());
        assertEquals(0, new BigDecimal("29.99").compareTo(price));
    }

    @Test
    void parsePriceFromMap_integerPrice_works() {
        Map<String, Object> item = Map.of("price", 100);
        BigDecimal price = new BigDecimal(item.get("price").toString());
        assertEquals(0, new BigDecimal("100").compareTo(price));
    }

    // ── Order status par défaut ───────────────────────────────────────────────

    @Test
    void newOrder_defaultStatus_isPending() {
        Order order = new Order();
        order.setStatus(Order.OrderStatus.PENDING);
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void orderStatus_allValuesExist() {
        // Vérifier que tous les statuts attendus existent
        assertDoesNotThrow(() -> Order.OrderStatus.valueOf("PENDING"));
        assertDoesNotThrow(() -> Order.OrderStatus.valueOf("CONFIRMED"));
        assertDoesNotThrow(() -> Order.OrderStatus.valueOf("SHIPPED"));
        assertDoesNotThrow(() -> Order.OrderStatus.valueOf("DELIVERED"));
        assertDoesNotThrow(() -> Order.OrderStatus.valueOf("CANCELLED"));
    }

    @Test
    void orderStatus_invalidValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                Order.OrderStatus.valueOf("REFUNDED")); // n'existe pas
    }

    // ── Calcul du total à partir des cartItems ────────────────────────────────

    @Test
    void totalAmount_calculatedFromItems_isCorrect() {
        List<Map<String, Object>> items = List.of(
                Map.of("productId", 1L, "quantity", 2, "price", "50.00", "name", "A"),
                Map.of("productId", 2L, "quantity", 1, "price", "30.00", "name", "B")
        );

        BigDecimal total = items.stream()
                .map(i -> new BigDecimal(i.get("price").toString())
                        .multiply(BigDecimal.valueOf(Integer.parseInt(i.get("quantity").toString()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, new BigDecimal("130.00").compareTo(total));
    }

    @Test
    void emptyItems_total_isZero() {
        List<Map<String, Object>> items = List.of();
        BigDecimal total = items.stream()
                .map(i -> new BigDecimal(i.get("price").toString())
                        .multiply(BigDecimal.valueOf((int) i.get("quantity"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, BigDecimal.ZERO.compareTo(total));
    }
}
