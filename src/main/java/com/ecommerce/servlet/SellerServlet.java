package com.ecommerce.servlet;

import com.ecommerce.filter.RoleGuard;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * GET /api/seller/stats   → revenue + order counts for the logged-in seller
 * GET /api/seller/orders  → orders containing this seller's products
 */
@WebServlet("/api/seller/*")
public class SellerServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!RoleGuard.require(req, res, "SELLER", "ADMIN")) return;

        Long sellerId = RoleGuard.getUserId(req);
        String pathInfo = req.getPathInfo(); // "/stats" or "/orders"

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(res, 400, "Endpoint required: /stats or /orders");
            return;
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            if (pathInfo.equals("/stats")) {
                // Count of seller's products
                Long totalProducts = em.createQuery(
                        "SELECT COUNT(p) FROM Product p WHERE p.sellerId = :sid", Long.class)
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                Long activeProducts = em.createQuery(
                        "SELECT COUNT(p) FROM Product p WHERE p.sellerId = :sid AND p.status = 'ACTIVE'", Long.class)
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                // Revenue from order_items where product belongs to this seller (non-cancelled orders)
                BigDecimal revenue = em.createQuery(
                        "SELECT COALESCE(SUM(i.price * i.quantity), 0) FROM OrderItem i " +
                        "JOIN Order o ON o.id = i.orderId " +
                        "JOIN Product p ON p.id = i.productId " +
                        "WHERE p.sellerId = :sid AND o.status <> com.ecommerce.entity.Order.OrderStatus.CANCELLED",
                        BigDecimal.class)
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                // Total orders containing this seller's products
                Long totalOrders = em.createQuery(
                        "SELECT COUNT(DISTINCT o.id) FROM Order o JOIN OrderItem i ON i.orderId = o.id " +
                        "JOIN Product p ON p.id = i.productId WHERE p.sellerId = :sid", Long.class)
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                Long pendingOrders = em.createQuery(
                        "SELECT COUNT(DISTINCT o.id) FROM Order o JOIN OrderItem i ON i.orderId = o.id " +
                        "JOIN Product p ON p.id = i.productId WHERE p.sellerId = :sid AND o.status = 'PENDING'", Long.class)
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                Map<String, Object> stats = new LinkedHashMap<>();
                stats.put("totalProducts",  totalProducts);
                stats.put("activeProducts", activeProducts);
                stats.put("totalRevenue",   revenue != null ? revenue : BigDecimal.ZERO);
                stats.put("totalOrders",    totalOrders);
                stats.put("pendingOrders",  pendingOrders);
                sendJson(res, 200, stats);

            } else if (pathInfo.equals("/orders")) {
                // Orders that contain this seller's products
                @SuppressWarnings("unchecked")
                List<Object[]> rows = em.createQuery(
                        "SELECT DISTINCT o.id, o.status, o.totalAmount, o.createdAt, " +
                        "       u.firstName, u.lastName " +
                        "FROM Order o " +
                        "JOIN OrderItem i ON i.orderId = o.id " +
                        "JOIN Product p ON p.id = i.productId " +
                        "JOIN User u ON u.id = o.userId " +
                        "WHERE p.sellerId = :sid " +
                        "ORDER BY o.createdAt DESC")
                        .setParameter("sid", sellerId)
                        .getResultList();

                List<Map<String, Object>> result = new ArrayList<>();
                Set<Long> seen = new HashSet<>();
                for (Object[] row : rows) {
                    Long orderId = (Long) row[0];
                    if (seen.contains(orderId)) continue;
                    seen.add(orderId);

                    // Get items for this order that belong to this seller
                    @SuppressWarnings("unchecked")
                    List<Object[]> items = em.createQuery(
                            "SELECT i.productName, i.quantity, i.price FROM OrderItem i " +
                            "JOIN Product p ON p.id = i.productId " +
                            "WHERE i.orderId = :oid AND p.sellerId = :sid")
                            .setParameter("oid", orderId)
                            .setParameter("sid", sellerId)
                            .getResultList();

                    List<Map<String, Object>> itemList = new ArrayList<>();
                    BigDecimal sellerTotal = BigDecimal.ZERO;
                    for (Object[] item : items) {
                        String name = (String) item[0];
                        Integer qty = (Integer) item[1];
                        BigDecimal price = (BigDecimal) item[2];
                        BigDecimal sub = price.multiply(BigDecimal.valueOf(qty));
                        sellerTotal = sellerTotal.add(sub);
                        Map<String, Object> im = new LinkedHashMap<>();
                        im.put("productName", name != null ? name : "—");
                        im.put("quantity",    qty);
                        im.put("price",       price);
                        im.put("subtotal",    sub);
                        itemList.add(im);
                    }

                    String firstName = row[4] != null ? (String) row[4] : "";
                    String lastName  = row[5] != null ? (String) row[5] : "";
                    String customerName = (firstName + " " + lastName).trim();
                    if (customerName.isEmpty()) customerName = "Customer";

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id",           orderId);
                    map.put("status",       row[1].toString());
                    map.put("sellerTotal",  sellerTotal);
                    map.put("createdAt",    row[3] != null ? row[3].toString() : null);
                    map.put("customerName", customerName);
                    map.put("items",        itemList);
                    result.add(map);
                }

                sendJson(res, 200, result);

            } else {
                sendError(res, 404, "Unknown seller endpoint");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Internal server error: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}
