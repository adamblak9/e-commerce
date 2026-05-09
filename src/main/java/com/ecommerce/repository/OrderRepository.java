package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class OrderRepository {

    // Save order with items
    public Order save(Order order, List<OrderItem> items) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.persist(order);

            for (OrderItem item : items) {
                item.setOrderId(order.getId());
                em.persist(item);
            }

            tx.commit();
            return order;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    // Save or update order only
    public Order save(Order order) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            if (order.getId() == null) {
                em.persist(order);
            } else {
                order = em.merge(order);
            }

            tx.commit();
            return order;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    // Find order by ID
    public Optional<Order> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            Order order = em.find(Order.class, id);
            return Optional.ofNullable(order);

        } finally {
            em.close();
        }
    }
}