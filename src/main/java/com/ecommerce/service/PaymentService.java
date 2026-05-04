package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Payment.PaymentMethod;
import com.ecommerce.entity.Payment.PaymentStatus;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentService {

    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final OrderRepository orderRepository = new OrderRepository();

    // ─── Carte Bancaire ──────────────────────────────────────────────────────────
    public Payment processCardPayment(Long userId, Long orderId, BigDecimal amount,
                                      String cardNumber, String cardHolder,
                                      String expiry, String cvv) {
        // Validate card (basic)
        if (cardNumber == null || cardNumber.replaceAll("\\s", "").length() < 13) {
            throw new IllegalArgumentException("Numéro de carte invalide");
        }
        if (cvv == null || cvv.length() < 3) {
            throw new IllegalArgumentException("CVV invalide");
        }

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setCardHolderName(cardHolder);
        // Store only last 4 digits
        String digits = cardNumber.replaceAll("\\s", "");
        payment.setCardLast4(digits.substring(digits.length() - 4));
        payment.setTransactionId("TXN-CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus(PaymentStatus.COMPLETED);  // Simulate successful charge

        Payment saved = paymentRepository.save(payment);
        confirmOrder(orderId);
        return saved;
    }

    // ─── PayPal ──────────────────────────────────────────────────────────────────
    public Payment processPaypalPayment(Long userId, Long orderId, BigDecimal amount,
                                         String paypalEmail) {
        if (paypalEmail == null || !paypalEmail.contains("@")) {
            throw new IllegalArgumentException("Email PayPal invalide");
        }

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.PAYPAL);
        payment.setPaypalEmail(paypalEmail);
        payment.setTransactionId("TXN-PP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus(PaymentStatus.COMPLETED);  // Simulate PayPal redirect/success

        Payment saved = paymentRepository.save(payment);
        confirmOrder(orderId);
        return saved;
    }

    // ─── Paiement à la livraison ──────────────────────────────────────────────────
    public Payment processCashOnDelivery(Long userId, Long orderId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        payment.setTransactionId("TXN-COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus(PaymentStatus.PENDING);  // Will be collected on delivery

        Payment saved = paymentRepository.save(payment);
        confirmOrder(orderId);
        return saved;
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────
    private void confirmOrder(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
        });
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }
}
