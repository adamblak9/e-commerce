package com.ecommerce.servlet;

import com.ecommerce.entity.Payment;
import com.ecommerce.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/payment/status")
public class  PaymentStatusServlet extends BaseServlet {

    private final PaymentService paymentService = new PaymentService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String orderIdStr = req.getParameter("orderId");
        if (orderIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), "{\"error\":\"orderId requis\"}");
            return;
        }

        Payment payment = paymentService.getPaymentByOrderId(Long.parseLong(orderIdStr));
        if (payment == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(resp.getWriter(), "{\"error\":\"Paiement introuvable\"}");
            return;
        }

        mapper.writeValue(resp.getWriter(), payment);
    }
}
