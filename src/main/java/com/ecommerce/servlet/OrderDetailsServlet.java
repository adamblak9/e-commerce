package com.ecommerce.servlet;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.service.OrderService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/orders/details")
public class OrderDetailsServlet extends BaseServlet {
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String orderIdParam = req.getParameter("orderId");
        if (orderIdParam == null) {
            sendError(res, 400, "Order ID is missing");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdParam);
            // You'll need to add a 'getOrderById' and 'getItemsByOrderId' to your OrderService
            Order order = orderService.getOrderById(orderId);
            List<OrderItem> items = orderService.getOrderItems(orderId);

            Map<String, Object> result = new HashMap<>();
            result.put("order", order);
            result.put("items", items);

            sendJson(res, 200, result);
        } catch (Exception e) {
            sendError(res, 500, "Error fetching order details");
        }
    }
}