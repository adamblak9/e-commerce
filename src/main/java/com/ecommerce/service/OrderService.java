package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderService {

    private final OrderRepository orderRepository = new OrderRepository();

    /**
     * Creates a new Order and its items.
     * cartItems must contain: productId, quantity, price, and name (product name).
     */
    public Order createOrder(Long userId, List<Map<String, Object>> cartItems, BigDecimal total, String address) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setShippingAddress(address);
        order.setStatus(Order.OrderStatus.PENDING);

        List<OrderItem> items = new ArrayList<>();
        for (Map<String, Object> itemMap : cartItems) {
            OrderItem item = new OrderItem();

            Long productId    = Long.valueOf(itemMap.get("productId").toString());
            Integer quantity  = Integer.valueOf(itemMap.get("quantity").toString());
            BigDecimal price  = new BigDecimal(itemMap.get("price").toString());

            // ← Save the product name at purchase time
            String productName = itemMap.containsKey("name")
                    ? itemMap.get("name").toString()
                    : "Product #" + productId;

            item.setProductId(productId);
            item.setProductName(productName);
            item.setQuantity(quantity);
            item.setPrice(price);

            items.add(item);
        }

        return orderRepository.save(order, items);
    }

    public Order getOrderById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Order.class, id);
        } finally {
            em.close();
        }
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT i FROM OrderItem i WHERE i.orderId = :oid", OrderItem.class)
                    .setParameter("oid", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Order order = em.find(Order.class, orderId);
            if (order != null) order.setStatus(status);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Decrements stock for each item in an order.
     * Called by PaymentServlet right after a successful payment.
     *
     * Uses a raw JPQL UPDATE on the products table.
     * Assumes your Product entity has a field `stockQuantity` (or `stock`).
     */
    public void decrementStockForOrder(Long orderId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            List<OrderItem> items = em.createQuery(
                    "SELECT i FROM OrderItem i WHERE i.orderId = :oid", OrderItem.class)
                    .setParameter("oid", orderId)
                    .getResultList();

            for (OrderItem item : items) {
                // Decrement stock — will not go below 0
                int updated = em.createQuery(
                        "UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty " +
                        "WHERE p.id = :pid AND p.stockQuantity >= :qty")
                        .setParameter("qty", item.getQuantity())
                        .setParameter("pid", item.getProductId())
                        .executeUpdate();

                if (updated == 0) {
                    // Product not found or insufficient stock — log but don't block payment
                    System.err.println("[OrderService] Stock not decremented for productId="
                            + item.getProductId() + " (not found or insufficient stock)");
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
