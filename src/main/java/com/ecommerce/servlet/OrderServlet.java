package com.ecommerce.servlet;

import com.ecommerce.servlet.MetricsServlet;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.filter.RoleGuard;
import com.ecommerce.service.OrderService;
import com.ecommerce.util.JPAUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * POST /api/orders              → create order (any authenticated user)
 * GET  /api/orders/all          → list all orders with customer name (ADMIN only)
 * PUT  /api/orders/{id}/status  → update order status (ADMIN only)
 */
@WebServlet("/api/orders/*")
public class OrderServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Only admin can list all orders
        if (!RoleGuard.require(req, res, "ADMIN")) return;

        String pathInfo = req.getPathInfo(); // null or "/all"

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/all")) {
            EntityManager em = JPAUtil.getEntityManager();
            try {
                List<Order> orders = em.createQuery(
                                "SELECT o FROM Order o ORDER BY o.createdAt DESC", Order.class)
                        .getResultList();

                List<Map<String, Object>> result = new ArrayList<>();
                for (Order o : orders) {
                    // Look up customer name
                    String customerName = "User #" + o.getUserId();
                    try {
                        Object[] nameRow = (Object[]) em.createQuery(
                                        "SELECT u.firstName, u.lastName FROM User u WHERE u.id = :uid")
                                .setParameter("uid", o.getUserId())
                                .getSingleResult();
                        if (nameRow != null) {
                            customerName = (nameRow[0] != null ? nameRow[0] : "") + " " +
                                    (nameRow[1] != null ? nameRow[1] : "");
                            customerName = customerName.trim();
                            if (customerName.isEmpty()) customerName = "User #" + o.getUserId();
                        }
                    } catch (Exception ignored) {}

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id",              o.getId());
                    map.put("userId",          o.getUserId());
                    map.put("customerName",    customerName);
                    map.put("totalAmount",     o.getTotalAmount());
                    map.put("status",          o.getStatus().name());
                    map.put("shippingAddress", o.getShippingAddress());
                    map.put("createdAt",       o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
                    result.add(map);
                }

                sendJson(res, 200, result);
            } finally {
                em.close();
            }
            return;
        }

        sendError(res, 404, "Unknown endpoint");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();
        // Only POST /api/orders (no path segment) creates an order
        if (pathInfo != null && !pathInfo.equals("/")) {
            sendError(res, 404, "Not found");
            return;
        }

        Object userIdAttr = req.getAttribute("userId");
        if (userIdAttr == null) {
            sendError(res, 401, "Authentication required. Please log in.");
            return;
        }

        Long userId = Long.valueOf(userIdAttr.toString());

        try {
            Map<String, Object> body = mapper.readValue(req.getReader(),
                    new TypeReference<Map<String, Object>>() {});

            if (body == null || !body.containsKey("items") || !body.containsKey("total")) {
                sendError(res, 400, "Invalid order data: items and total are required.");
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) body.get("items");
            String address = (String) body.get("address");
            BigDecimal totalAmount = new BigDecimal(body.get("total").toString());

            if (cartItems.isEmpty()) {
                sendError(res, 400, "Cannot create an empty order.");
                return;
            }

            Order order = orderService.createOrder(userId, cartItems, totalAmount, address);
            MetricsServlet.ordersCreatedTotal.incrementAndGet();
            sendJson(res, 201, Map.of(
                    "success", true,
                    "orderId", order.getId(),
                    "message", "Order created successfully"
            ));

        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid price format in order.");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Server error while processing order: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!RoleGuard.require(req, res, "ADMIN")) return;

        String pathInfo = req.getPathInfo(); // e.g. "/5/status"
        if (pathInfo == null) { sendError(res, 400, "Missing order ID"); return; }

        String[] parts = pathInfo.split("/"); // ["", "5", "status"]
        if (parts.length < 3 || !"status".equals(parts[2])) {
            sendError(res, 400, "Expected: /api/orders/{id}/status");
            return;
        }

        try {
            Long orderId = Long.parseLong(parts[1]);
            Map<String, String> body = readBody(req);
            String statusStr = body.get("status");
            if (statusStr == null) { sendError(res, 400, "status is required"); return; }

            Order.OrderStatus status;
            try {
                status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                sendError(res, 400, "Invalid status: " + statusStr);
                return;
            }

            orderService.updateOrderStatus(orderId, status);
            sendJson(res, 200, Map.of("message", "Order status updated", "orderId", orderId, "status", status.name()));

        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid order ID");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Error updating order: " + e.getMessage());
        }
    }
}