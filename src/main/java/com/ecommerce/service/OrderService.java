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
     * Creates a new Order and its associated items in the database.
     * Used by OrderServlet when the user clicks "Checkout" in cart.html.
     */
    public Order createOrder(Long userId, List<Map<String, Object>> cartItems, BigDecimal total, String address) {
        // 1. Prepare the Order entity
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setShippingAddress(address);
        order.setStatus(Order.OrderStatus.PENDING); // Initial status

        // 2. Prepare the list of OrderItem entities
        List<OrderItem> items = new ArrayList<>();
        for (Map<String, Object> itemMap : cartItems) {
            OrderItem item = new OrderItem();

            // Extract IDs and values safely from the Map
            Long productId = Long.valueOf(itemMap.get("productId").toString());
            Integer quantity = Integer.valueOf(itemMap.get("quantity").toString());
            BigDecimal price = new BigDecimal(itemMap.get("price").toString());

            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setPrice(price);

            items.add(item);
        }

        // 3. Save everything to BDD via the Repository
        return orderRepository.save(order, items);
    }

    /**
     * Fetches an order by its ID.
     * Used by OrderDetailsServlet to show info on the Payment page.
     */
    public Order getOrderById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Order.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Fetches all items belonging to a specific order.
     * Used by OrderDetailsServlet to show the product list on the Payment page.
     */
    public List<OrderItem> getOrderItems(Long orderId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT i FROM OrderItem i WHERE i.orderId = :oid", OrderItem.class)
                    .setParameter("oid", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Updates the status of an order (e.g., from PENDING to CONFIRMED).
     * Used by PaymentServlet after a successful transaction.
     */
    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Order order = em.find(Order.class, orderId);
            if (order != null) {
                order.setStatus(status);
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