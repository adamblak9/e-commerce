package com.ecommerce.servlet;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.service.PaymentService;
import com.ecommerce.service.OrderService;
import com.ecommerce.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/payment")
public class PaymentServlet extends BaseServlet {

    private final PaymentService paymentService = new PaymentService();
    private final OrderService   orderService   = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        // 1. JWT Authentication (from cookie or Authorization header)
        String token = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) if ("jwt".equals(c.getName())) token = c.getValue();
        }
        // Also accept Bearer token from header (used by payment.html)
        if (token == null) {
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token == null) {
            sendJson(resp, 401, Map.of("success", false, "message", "Login required"));
            return;
        }

        try {
            DecodedJWT jwt    = JwtUtil.verify(token);
            Long userId       = jwt.getClaim("userId").asLong();

            // 2. Extract parameters
            String method     = req.getParameter("paymentMethod");
            Long orderId      = Long.parseLong(req.getParameter("orderId"));
            BigDecimal amount = new BigDecimal(req.getParameter("amount"));

            // 3. Process payment
            Payment payment;
            if ("CARD".equalsIgnoreCase(method)) {
                payment = paymentService.processCardPayment(
                        userId, orderId, amount,
                        req.getParameter("cardNumber"),
                        req.getParameter("cardHolder"),
                        req.getParameter("expiry"),
                        req.getParameter("cvv"));
            } else if ("PAYPAL".equalsIgnoreCase(method)) {
                payment = paymentService.processPaypalPayment(
                        userId, orderId, amount,
                        req.getParameter("paypalEmail"));
            } else {
                payment = paymentService.processCashOnDelivery(userId, orderId, amount);
            }

            // 4. Update order status to CONFIRMED
            orderService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED);

            // 5. ← DECREMENT STOCK for every item in this order
            orderService.decrementStockForOrder(orderId);

            // 6. Success response
            Map<String, Object> res = new HashMap<>();
            res.put("success",       true);
            res.put("transactionId", payment.getTransactionId());
            res.put("message",       "Payment processed and stock updated");
            sendJson(resp, 200, res);

        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, 500, Map.of("success", false, "message", e.getMessage()));
        }
    }
}
