package com.ecommerce.servlet;

import com.ecommerce.entity.Payment;
import com.ecommerce.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
            throws ServletException, IOException {
        // Display the payment page
        req.getRequestDispatcher("/payment.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // ─── Auth via JWT (Bearer token in Authorization header) ───────────────
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, error("Non authentifié. Fournissez un token Bearer."));
            return;
        }
        Long userId;
        try {
            com.auth0.jwt.interfaces.DecodedJWT jwt =
                    com.ecommerce.util.JwtUtil.verify(authHeader.substring(7));
            userId = jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, error("Token invalide ou expiré."));
            return;
        }

        // ... rest of your existing doPost code (keep it exactly as-is, just remove the session block)
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
                    String cardNumber = req.getParameter("cardNumber");
                    String cardHolder = req.getParameter("cardHolder");
                    String expiry    = req.getParameter("expiry");
                    String cvv       = req.getParameter("cvv");
                    payment = paymentService.processCardPayment(
                            userId, orderId, amount, cardNumber, cardHolder, expiry, cvv);
                    break;

                case "PAYPAL":
                    String paypalEmail = req.getParameter("paypalEmail");
                    payment = paymentService.processPaypalPayment(
                            userId, orderId, amount, paypalEmail);
                    break;

                case "CASH_ON_DELIVERY":
                    payment = paymentService.processCashOnDelivery(userId, orderId, amount);
                    break;

                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeJson(resp, error("Méthode de paiement inconnue: " + method));
                    return;
            }

            writeJson(resp, success(payment));

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(resp, error("Erreur serveur: " + e.getMessage()));
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private Map<String, Object> success(Payment payment) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Paiement effectué avec succès !");
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
