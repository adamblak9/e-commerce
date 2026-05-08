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
    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        // 1. JWT Authentication
        String token = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) if ("jwt".equals(c.getName())) token = c.getValue();
        }

        if (token == null) {
            resp.setStatus(401);
            sendJson(resp, 401, Map.of("success", false, "message", "Login required"));
            return;
        }

        try {
            DecodedJWT jwt = JwtUtil.verify(token);
            Long userId = jwt.getClaim("userId").asLong();

            // 2. Extract Parameters
            String method = req.getParameter("paymentMethod");
            Long orderId = Long.parseLong(req.getParameter("orderId"));
            BigDecimal amount = new BigDecimal(req.getParameter("amount"));

            // 3. Process Payment based on method
            Payment payment;
            if ("CARD".equalsIgnoreCase(method)) {
                payment = paymentService.processCardPayment(userId, orderId, amount,
                        req.getParameter("cardNumber"), req.getParameter("cardHolder"),
                        req.getParameter("expiry"), req.getParameter("cvv"));
            } else if ("PAYPAL".equalsIgnoreCase(method)) {
                payment = paymentService.processPaypalPayment(userId, orderId, amount, req.getParameter("paypalEmail"));
            } else {
                payment = paymentService.processCashOnDelivery(userId, orderId, amount);
            }

            // 4. IMPORTANT: Update the Order Status in BDD
            orderService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED);

            // 5. Success response
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("transactionId", payment.getTransactionId());
            res.put("message", "Payment processed and Order confirmed");
            sendJson(resp, 200, res);

        } catch (Exception e) {
            resp.setStatus(500);
            sendJson(resp, 500, Map.of("success", false, "message", e.getMessage()));
        }
    }
}