package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class OrderRepository {

    private EntityManager entityManager;

    // ADD THIS CONSTRUCTOR
    public OrderRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // KEEP EMPTY CONSTRUCTOR TOO
    public OrderRepository() {
    }

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

    public List<Order> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT o FROM Order o ORDER BY o.createdAt DESC", Order.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public long countAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(o) FROM Order o", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countByStatus(Order.OrderStatus status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(o) FROM Order o WHERE o.status = :status", Long.class)
                    .setParameter("status", status).getSingleResult();
        } finally {
            em.close();
        }
    }

    public java.math.BigDecimal sumRevenue() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            java.math.BigDecimal result = em.createQuery(
                            "SELECT SUM(o.totalAmount) FROM Order o WHERE o.status <> :cancelled",
                            java.math.BigDecimal.class)
                    .setParameter("cancelled", Order.OrderStatus.CANCELLED)
                    .getSingleResult();

            return result != null ? result : java.math.BigDecimal.ZERO;

        } finally {
            em.close();
        }
    }
}