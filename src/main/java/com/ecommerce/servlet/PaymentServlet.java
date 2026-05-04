package com.ecommerce.servlet;

import com.ecommerce.entity.Payment;
import com.ecommerce.service.PaymentService;
import com.ecommerce.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.ServletException;
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
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        req.getRequestDispatcher("/payment.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // ─────────────────────────────────────────────
        // 🔐 GET TOKEN FROM COOKIE
        // ─────────────────────────────────────────────
        String token = null;

        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("jwt".equals(c.getName())) {
                    token = c.getValue();
                }
            }
        }

        if (token == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, error("Non authentifié (cookie manquant)"));
            return;
        }

        // ─────────────────────────────────────────────
        // 🔐 VERIFY JWT
        // ─────────────────────────────────────────────
        Long userId;
        try {
            DecodedJWT jwt = JwtUtil.verify(token);
            userId = jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, error("Token invalide ou expiré"));
            return;
        }

        // ─────────────────────────────────────────────
        // 💳 PAYMENT LOGIC (UNCHANGED)
        // ─────────────────────────────────────────────
        try {
            String method = req.getParameter("paymentMethod");
            String orderIdStr = req.getParameter("orderId");
            String amountStr = req.getParameter("amount");

            if (method == null || orderIdStr == null || amountStr == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(resp, error("Paramètres manquants"));
                return;
            }

            Long orderId = Long.parseLong(orderIdStr);
            BigDecimal amount = new BigDecimal(amountStr);

            Payment payment;

            switch (method.toUpperCase()) {

                case "CARD":
                    payment = paymentService.processCardPayment(
                            userId,
                            orderId,
                            amount,
                            req.getParameter("cardNumber"),
                            req.getParameter("cardHolder"),
                            req.getParameter("expiry"),
                            req.getParameter("cvv")
                    );
                    break;

                case "PAYPAL":
                    payment = paymentService.processPaypalPayment(
                            userId,
                            orderId,
                            amount,
                            req.getParameter("paypalEmail")
                    );
                    break;

                case "CASH_ON_DELIVERY":
                    payment = paymentService.processCashOnDelivery(
                            userId,
                            orderId,
                            amount
                    );
                    break;

                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeJson(resp, error("Méthode de paiement inconnue"));
                    return;
            }

            writeJson(resp, success(payment));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(resp, error("Erreur serveur: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private Map<String, Object> success(Payment payment) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Paiement effectué avec succès");
        res.put("transactionId", payment.getTransactionId());
        res.put("orderId", payment.getOrderId());
        res.put("amount", payment.getAmount());
        res.put("method", payment.getPaymentMethod().name());
        res.put("status", payment.getStatus().name());
        return res;
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", msg);
        return res;
    }

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        mapper.writeValue(resp.getWriter(), obj);
    }
}