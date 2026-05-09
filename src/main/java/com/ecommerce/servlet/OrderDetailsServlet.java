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
import java.util.stream.Collectors;

@WebServlet("/api/orders/details")
public class OrderDetailsServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        Object userIdAttr = req.getAttribute("userId");
        if (userIdAttr == null) {
            sendError(res, 401, "Authentication required.");
            return;
        }
        Long userId = Long.valueOf(userIdAttr.toString());

        String orderIdParam = req.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.isBlank()) {
            sendError(res, 400, "Order ID is missing.");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdParam);

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                sendError(res, 404, "Order not found.");
                return;
            }
            if (!order.getUserId().equals(userId)) {
                sendError(res, 403, "Access denied.");
                return;
            }

            List<OrderItem> items = orderService.getOrderItems(orderId);

            // ← Include productName in every item returned to the frontend
            List<Map<String, Object>> itemsPayload = items.stream().map(item -> {
                Map<String, Object> m = new HashMap<>();
                m.put("productId",   item.getProductId());
                m.put("productName", item.getProductName() != null
                        ? item.getProductName()
                        : "Product #" + item.getProductId());  // fallback for old rows
                m.put("quantity",    item.getQuantity());
                m.put("price",       item.getPrice());
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("order", Map.of(
                    "id",              order.getId(),
                    "status",          order.getStatus(),
                    "totalAmount",     order.getTotalAmount(),
                    "shippingAddress", order.getShippingAddress(),
                    "createdAt",       order.getCreatedAt().toString()
            ));
            result.put("items", itemsPayload);

            sendJson(res, 200, result);

        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid order ID format.");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Error fetching order details: " + e.getMessage());
        }
    }
}
