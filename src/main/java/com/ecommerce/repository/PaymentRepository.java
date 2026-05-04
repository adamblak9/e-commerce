package com.ecommerce.repository;

import com.ecommerce.entity.Payment;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class PaymentRepository {

    public Payment save(Payment payment) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (payment.getId() == null) {
                em.persist(payment);
            } else {
                payment = em.merge(payment);
            }
            em.getTransaction().commit();
            return payment;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Payment> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Payment.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<Payment> findByOrderId(Long orderId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.orderId = :orderId", Payment.class);
            query.setParameter("orderId", orderId);
            List<Payment> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public List<Payment> findByUserId(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.userId = :userId ORDER BY p.createdAt DESC", Payment.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
