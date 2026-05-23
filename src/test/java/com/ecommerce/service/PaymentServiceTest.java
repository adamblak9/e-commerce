package com.ecommerce.service;

import com.ecommerce.entity.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PaymentService — validation des entrées.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * PaymentService valide les données de paiement (numéro de carte, CVV, email
 * PayPal) avant de traiter une transaction. Si ces validations sont absentes
 * ou incorrectes, des paiements invalides seraient acceptés, causant des
 * pertes financières ou des erreurs en production.
 *
 * On utilise Mockito pour isoler PaymentService de PaymentRepository et
 * OrderRepository, afin de tester uniquement la logique de validation.
 */
class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() throws Exception {
        paymentService = new PaymentService();

        // Injecter des mocks pour éviter les appels à la BDD
        var mockPaymentRepo = Mockito.mock(
                com.ecommerce.repository.PaymentRepository.class);
        var mockOrderRepo = Mockito.mock(
                com.ecommerce.repository.OrderRepository.class);

        // PaymentRepository sauvegarde et renvoie le payment passé
        when(mockPaymentRepo.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // OrderRepository.findById renvoie un Optional vide (on ne teste pas confirmOrder ici)
        when(mockOrderRepo.findById(anyLong()))
                .thenReturn(java.util.Optional.empty());

        injectField(paymentService, "paymentRepository", mockPaymentRepo);
        injectField(paymentService, "orderRepository",   mockOrderRepo);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field f = PaymentService.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ── processCardPayment — validations ──────────────────────────────────────

    @Test
    void processCardPayment_validCard_returnsPayment() {
        Payment p = paymentService.processCardPayment(
                1L, 10L, new BigDecimal("100.00"),
                "4111 1111 1111 1111", "Alice Dupont", "12/26", "123");
        assertNotNull(p);
        assertEquals(Payment.PaymentStatus.COMPLETED, p.getStatus());
        assertEquals(Payment.PaymentMethod.CARD, p.getPaymentMethod());
    }

    @Test
    void processCardPayment_storesOnlyLast4Digits() {
        Payment p = paymentService.processCardPayment(
                1L, 10L, new BigDecimal("50.00"),
                "4111111111111111", "Bob Martin", "01/27", "456");
        assertEquals("1111", p.getCardLast4());
    }

    @Test
    void processCardPayment_shortCardNumber_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processCardPayment(
                        1L, 10L, new BigDecimal("50.00"),
                        "1234", "Bob", "01/27", "456"));
    }

    @Test
    void processCardPayment_nullCardNumber_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processCardPayment(
                        1L, 10L, new BigDecimal("50.00"),
                        null, "Bob", "01/27", "456"));
    }

    @Test
    void processCardPayment_shortCvv_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processCardPayment(
                        1L, 10L, new BigDecimal("50.00"),
                        "4111111111111111", "Bob", "01/27", "12")); // CVV trop court
    }

    @Test
    void processCardPayment_nullCvv_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processCardPayment(
                        1L, 10L, new BigDecimal("50.00"),
                        "4111111111111111", "Bob", "01/27", null));
    }

    @Test
    void processCardPayment_hasTransactionId() {
        Payment p = paymentService.processCardPayment(
                1L, 10L, new BigDecimal("100.00"),
                "4111111111111111", "Alice", "12/26", "123");
        assertTrue(p.getTransactionId().startsWith("TXN-CARD-"));
    }

    // ── processPaypalPayment — validations ────────────────────────────────────

    @Test
    void processPaypalPayment_validEmail_returnsPayment() {
        Payment p = paymentService.processPaypalPayment(
                1L, 10L, new BigDecimal("200.00"), "user@paypal.com");
        assertNotNull(p);
        assertEquals(Payment.PaymentStatus.COMPLETED, p.getStatus());
        assertEquals(Payment.PaymentMethod.PAYPAL, p.getPaymentMethod());
    }

    @Test
    void processPaypalPayment_invalidEmail_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPaypalPayment(
                        1L, 10L, new BigDecimal("200.00"), "notanemail"));
    }

    @Test
    void processPaypalPayment_nullEmail_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPaypalPayment(
                        1L, 10L, new BigDecimal("200.00"), null));
    }

    @Test
    void processPaypalPayment_hasTransactionId() {
        Payment p = paymentService.processPaypalPayment(
                1L, 10L, new BigDecimal("100.00"), "test@paypal.com");
        assertTrue(p.getTransactionId().startsWith("TXN-PP-"));
    }

    // ── processCashOnDelivery ─────────────────────────────────────────────────

    @Test
    void processCashOnDelivery_returnsPaymentPending() {
        Payment p = paymentService.processCashOnDelivery(
                1L, 10L, new BigDecimal("150.00"));
        assertNotNull(p);
        // COD est PENDING, pas COMPLETED immédiatement
        assertEquals(Payment.PaymentStatus.PENDING, p.getStatus());
        assertEquals(Payment.PaymentMethod.CASH_ON_DELIVERY, p.getPaymentMethod());
    }

    @Test
    void processCashOnDelivery_hasTransactionId() {
        Payment p = paymentService.processCashOnDelivery(
                1L, 10L, new BigDecimal("100.00"));
        assertTrue(p.getTransactionId().startsWith("TXN-COD-"));
    }
}
