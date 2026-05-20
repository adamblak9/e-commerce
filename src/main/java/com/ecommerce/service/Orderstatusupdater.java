package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background job — runs every 60 seconds.
 * SHIPPED orders older than 1 hour → automatically marked DELIVERED.
 */
@WebListener
public class Orderstatusupdater implements ServletContextListener {

    private static final Logger log = Logger.getLogger(Orderstatusupdater.class.getName());
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateDeliveredOrders, 60, 60, TimeUnit.SECONDS);
        log.info("OrderStatusUpdater started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) scheduler.shutdownNow();
    }

    private void updateDeliveredOrders() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Find SHIPPED orders placed more than 3 minutes  ago
            List<Order> orders = em.createQuery(
                            "SELECT o FROM Order o WHERE o.status = :status AND o.createdAt <= :cutoff",
                            Order.class)
                    .setParameter("status", Order.OrderStatus.CONFIRMED)
                    .setParameter("cutoff", java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(3))
                    .getResultList();

            for (Order o : orders) {
                o.setStatus(Order.OrderStatus.DELIVERED);
                o.setDeliveredAt(LocalDateTime.now());
                log.info("Order #" + o.getId() + " auto-marked as DELIVERED.");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.warning("OrderStatusUpdater error: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}