package com.ecommerce.servlet;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.filter.RoleGuard;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GET /api/customer/orders      → all orders for logged-in customer
 * GET /api/customer/orders/{id} → single order detail
 */
@WebServlet("/api/customer/orders/*")
public class CustomerOrderServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        Long userId = RoleGuard.getUserId(req);
        if (userId == null) { sendError(res, 401, "Login required"); return; }

        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // ── All orders ────────────────────────
                List<Order> orders = em.createQuery(
                                "SELECT o FROM Order o WHERE o.userId = :uid ORDER BY o.createdAt DESC",
                                Order.class)
                        .setParameter("uid", userId)
                        .getResultList();

                // Load items for each order
                List<Map<String, Object>> result = new ArrayList<>();
                for (Order o : orders) {
                    List<OrderItem> items = em.createQuery(
                                    "SELECT i FROM OrderItem i WHERE i.orderId = :oid", OrderItem.class)
                            .setParameter("oid", o.getId())
                            .getResultList();
                    result.add(toMap(o, items));
                }

                sendJson(res, 200, result);

            } else {
                // ── Single order ──────────────────────
                Long orderId = Long.parseLong(pathInfo.replace("/", ""));
                Order order  = em.find(Order.class, orderId);

                if (order == null || !order.getUserId().equals(userId)) {
                    sendError(res, 404, "Order not found"); return;
                }

                List<OrderItem> items = em.createQuery(
                                "SELECT i FROM OrderItem i WHERE i.orderId = :oid", OrderItem.class)
                        .setParameter("oid", orderId)
                        .getResultList();

                sendJson(res, 200, toMap(order, items));
            }

        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid order ID");
        } catch (Exception e) {
            sendError(res, 500, "Could not load orders: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    private Map<String, Object> toMap(Order o, List<OrderItem> items) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",              o.getId());
        map.put("status",          o.getStatus().name());
        map.put("totalAmount",     o.getTotalAmount());
        map.put("shippingAddress", o.getShippingAddress());
        map.put("createdAt",       o.getCreatedAt()   != null ? o.getCreatedAt().toString()   : null);
        map.put("updatedAt",       o.getUpdatedAt()   != null ? o.getUpdatedAt().toString()   : null);
        map.put("deliveredAt",     o.getDeliveredAt() != null ? o.getDeliveredAt().toString() : null);
        map.put("items", items.stream().map(this::itemToMap).collect(Collectors.toList()));
        return map;
    }

    private Map<String, Object> itemToMap(OrderItem i) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("productId",   i.getProductId());
        map.put("productName", i.getProductName());
        map.put("price",       i.getPrice());
        map.put("quantity",    i.getQuantity());
        map.put("subtotal",    i.getSubtotal());
        map.put("imageUrl",    i.getImageUrl());
        return map;
    }
}
