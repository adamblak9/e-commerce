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

@WebServlet("/api/seller/*")
public class SellerServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        if (!RoleGuard.require(req, res, "SELLER", "ADMIN")) return;

        Long sellerId = RoleGuard.getUserId(req);
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(res, 400, "Endpoint required");
            return;
        }

        EntityManager em = JPAUtil.getEntityManager();

        try {

            /*
             =======================
             ======== STATS ========
             =======================
            */
            if (pathInfo.equals("/stats")) {

                Long totalProducts = em.createQuery(
                                "SELECT COUNT(p) FROM Product p WHERE p.sellerId=:sid",
                                Long.class)
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                Long activeProducts = em.createQuery(
                                "SELECT COUNT(p) FROM Product p " +
                                        "WHERE p.sellerId=:sid AND p.status='ACTIVE'",
                                Long.class)
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                Object revenueResult = em.createNativeQuery(
                                "SELECT COALESCE(SUM(oi.price*oi.quantity),0) " +
                                        "FROM order_items oi " +
                                        "JOIN orders o ON o.id=oi.order_id " +
                                        "JOIN products p ON p.id=oi.product_id " +
                                        "WHERE p.seller_id=:sid " +
                                        "AND o.status<>'CANCELLED'")
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                BigDecimal revenue =
                        new BigDecimal(revenueResult.toString());

                Object totalOrdersResult = em.createNativeQuery(
                                "SELECT COUNT(DISTINCT o.id) " +
                                        "FROM orders o " +
                                        "JOIN order_items oi ON oi.order_id=o.id " +
                                        "JOIN products p ON p.id=oi.product_id " +
                                        "WHERE p.seller_id=:sid")
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                Long totalOrders =
                        Long.parseLong(totalOrdersResult.toString());

                Object pendingResult = em.createNativeQuery(
                                "SELECT COUNT(DISTINCT o.id) " +
                                        "FROM orders o " +
                                        "JOIN order_items oi ON oi.order_id=o.id " +
                                        "JOIN products p ON p.id=oi.product_id " +
                                        "WHERE p.seller_id=:sid " +
                                        "AND o.status='PENDING'")
                        .setParameter("sid", sellerId)
                        .getSingleResult();

                Long pendingOrders =
                        Long.parseLong(pendingResult.toString());

                Map<String,Object> stats = new LinkedHashMap<>();
                stats.put("totalProducts", totalProducts);
                stats.put("activeProducts", activeProducts);
                stats.put("totalRevenue", revenue);
                stats.put("totalOrders", totalOrders);
                stats.put("pendingOrders", pendingOrders);

                sendJson(res,200,stats);
            }

            /*
             =======================
             ======== ORDERS =======
             =======================
            */
            else if (pathInfo.equals("/orders")) {

                List<Object[]> rows = em.createNativeQuery(
                                "SELECT DISTINCT " +
                                        "o.id, o.status, o.created_at, " +
                                        "u.first_name, u.last_name " +
                                        "FROM orders o " +
                                        "JOIN users u ON u.id=o.user_id " +
                                        "JOIN order_items oi ON oi.order_id=o.id " +
                                        "JOIN products p ON p.id=oi.product_id " +
                                        "WHERE p.seller_id=:sid " +
                                        "ORDER BY o.created_at DESC")
                        .setParameter("sid", sellerId)
                        .getResultList();

                List<Map<String,Object>> result =
                        new ArrayList<>();

                for(Object[] row : rows){

                    Long orderId =
                            ((Number)row[0]).longValue();

                    List<Object[]> items =
                            em.createNativeQuery(
                                            "SELECT oi.product_name, oi.quantity, oi.price " +
                                                    "FROM order_items oi " +
                                                    "JOIN products p ON p.id = oi.product_id " +
                                                    "WHERE oi.order_id = :oid " +
                                                      "AND p.seller_id = :sid")
                                    .setParameter("oid", orderId)
                                    .setParameter("sid", sellerId)
                                    .getResultList();

                    BigDecimal sellerTotal =
                            BigDecimal.ZERO;

                    List<Map<String,Object>> itemList =
                            new ArrayList<>();

                    for(Object[] item : items){

                        String name =
                                item[0].toString();

                        Integer qty =
                                ((Number)item[1]).intValue();

                        BigDecimal price =
                                new BigDecimal(item[2].toString());

                        BigDecimal subtotal =
                                price.multiply(
                                        BigDecimal.valueOf(qty));

                        sellerTotal =
                                sellerTotal.add(subtotal);

                        Map<String,Object> m =
                                new LinkedHashMap<>();

                        m.put("productName", name);
                        m.put("quantity", qty);
                        m.put("price", price);
                        m.put("subtotal", subtotal);

                        itemList.add(m);
                    }

                    String customerName =
                            (row[3]+" "+row[4]).trim();

                    Map<String,Object> order =
                            new LinkedHashMap<>();

                    order.put("id", orderId);
                    order.put("status", row[1].toString());
                    order.put("createdAt", row[2].toString());
                    order.put("customerName", customerName);
                    order.put("sellerTotal", sellerTotal);
                    order.put("items", itemList);

                    result.add(order);
                }

                sendJson(res,200,result);
            }

            else {
                sendError(res,404,"Unknown endpoint");
            }

        } catch(Exception e){
            e.printStackTrace();
            sendError(res,500,e.getMessage());
        } finally {
            em.close();
        }
    }
}