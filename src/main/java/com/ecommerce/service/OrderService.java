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

    public Order createOrder(Long userId, List<Map<String, Object>> cartItems, BigDecimal total, String address) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setShippingAddress(address);
        order.setStatus(Order.OrderStatus.PENDING); // Initial status

        List<OrderItem> items = new ArrayList<>();
        for (Map<String, Object> itemMap : cartItems) {
            OrderItem item = new OrderItem();

            Long productId = Long.valueOf(itemMap.get("productId").toString());
            Integer quantity = Integer.valueOf(itemMap.get("quantity").toString());
            BigDecimal price = new BigDecimal(itemMap.get("price").toString());

            item.setProductId(productId);
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
            return em.createQuery("SELECT i FROM OrderItem i WHERE i.orderId = :oid", OrderItem.class)
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