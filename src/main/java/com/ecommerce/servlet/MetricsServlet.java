package com.ecommerce.servlet;

import jakarta.servlet.annotation.WebServlet;
import com.ecommerce.servlet.MetricsServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ShopEase Prometheus Metrics Endpoint
 * Exposes real business + JVM metrics at GET /metrics
 *
 * Prometheus scrapes this every 5s (configured in prometheus.yml)
 * Grafana reads from Prometheus to build dashboards.
 */
@WebServlet("/metrics")
public class MetricsServlet extends HttpServlet {

    // ── In-memory counters (static = shared across all requests) ──────────────
    public static final AtomicLong httpRequestsTotal      = new AtomicLong(0);
    public static final AtomicLong ordersCreatedTotal     = new AtomicLong(0);
    public static final AtomicLong loginSuccessTotal      = new AtomicLong(0);
    public static final AtomicLong loginFailureTotal      = new AtomicLong(0);
    public static final AtomicLong productsCreatedTotal   = new AtomicLong(0);
    public static final AtomicLong paymentsTotal          = new AtomicLong(0);
    public static final AtomicLong cartAddedTotal         = new AtomicLong(0);
    public static final AtomicLong registrationsTotal     = new AtomicLong(0);

    // ── App start time for uptime calculation ─────────────────────────────────
    private static final long START_TIME_SECONDS = Instant.now().getEpochSecond();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/plain; version=0.0.4; charset=utf-8");
        resp.setStatus(200);

        PrintWriter out = resp.getWriter();

        // ── 1. HTTP Requests Counter ──────────────────────────────────────────
        writeMetric(out, "shopease_http_requests_total",
                "counter", "Total HTTP requests received",
                httpRequestsTotal.get());

        // ── 2. Order Metrics ──────────────────────────────────────────────────
        writeMetric(out, "shopease_orders_created_total",
                "counter", "Total orders placed",
                ordersCreatedTotal.get());

        // ── 3. Auth Metrics ───────────────────────────────────────────────────
        writeMetric(out, "shopease_login_success_total",
                "counter", "Successful logins",
                loginSuccessTotal.get());

        writeMetric(out, "shopease_login_failure_total",
                "counter", "Failed login attempts",
                loginFailureTotal.get());

        // ── 4. Product & Catalog ──────────────────────────────────────────────
        writeMetric(out, "shopease_products_created_total",
                "counter", "Total products listed by sellers",
                productsCreatedTotal.get());

        // ── 5. Payments ───────────────────────────────────────────────────────
        writeMetric(out, "shopease_payments_total",
                "counter", "Total payment transactions",
                paymentsTotal.get());

        // ── 6. Cart Activity ──────────────────────────────────────────────────
        writeMetric(out, "shopease_cart_add_total",
                "counter", "Items added to cart",
                cartAddedTotal.get());

        // ── 7. User Registrations ─────────────────────────────────────────────
        writeMetric(out, "shopease_registrations_total",
                "counter", "New user registrations",
                registrationsTotal.get());

        // ── 8. App Uptime (Gauge) ─────────────────────────────────────────────
        long uptimeSeconds = Instant.now().getEpochSecond() - START_TIME_SECONDS;
        writeMetric(out, "shopease_uptime_seconds",
                "gauge", "Application uptime in seconds",
                uptimeSeconds);

        // ── 9. JVM Memory (Gauge) ─────────────────────────────────────────────
        Runtime rt = Runtime.getRuntime();
        long usedMemory = rt.totalMemory() - rt.freeMemory();

        writeMetric(out, "shopease_jvm_memory_used_bytes",
                "gauge", "JVM used heap memory in bytes",
                usedMemory);

        writeMetric(out, "shopease_jvm_memory_max_bytes",
                "gauge", "JVM max heap memory in bytes",
                rt.maxMemory());

        // ── 10. DB Live Counts (Gauges from database) ─────────────────────────
        EntityManager em = null;
        try {
            em = JPAUtil.getEntityManager();

            long totalUsers = (Long) em.createQuery("SELECT COUNT(u) FROM User u").getSingleResult();
            long totalOrders = (Long) em.createQuery("SELECT COUNT(o) FROM Order o").getSingleResult();
            long totalProducts = (Long) em.createQuery("SELECT COUNT(p) FROM Product p").getSingleResult();

            // Revenue: sum of all completed order totals
            Object revenueObj = em.createQuery(
                "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'"
            ).getSingleResult();
            double totalRevenue = revenueObj instanceof BigDecimal bd ? bd.doubleValue() : 0.0;

            // Pending orders count
            long pendingOrders = (Long) em.createQuery(
                "SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'"
            ).getSingleResult();

            writeMetric(out, "shopease_db_users_total",
                    "gauge", "Total registered users in database", totalUsers);

            writeMetric(out, "shopease_db_orders_total",
                    "gauge", "Total orders in database", totalOrders);

            writeMetric(out, "shopease_db_products_total",
                    "gauge", "Total products in database", totalProducts);

            writeMetric(out, "shopease_db_revenue_total",
                    "gauge", "Total revenue from delivered orders", totalRevenue);

            writeMetric(out, "shopease_db_pending_orders",
                    "gauge", "Number of pending orders", pendingOrders);

        } catch (Exception e) {
            // DB unavailable — emit 0 so Prometheus doesn't drop the scrape
            writeMetric(out, "shopease_db_users_total",    "gauge", "Total users", 0);
            writeMetric(out, "shopease_db_orders_total",   "gauge", "Total orders", 0);
            writeMetric(out, "shopease_db_products_total", "gauge", "Total products", 0);
            writeMetric(out, "shopease_db_revenue_total",  "gauge", "Total revenue", 0);
            writeMetric(out, "shopease_db_pending_orders", "gauge", "Pending orders", 0);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }

        // ── 11. App status (1 = up) ───────────────────────────────────────────
        writeMetric(out, "shopease_up", "gauge", "ShopEase app is running (1=up)", 1);

        out.flush();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void writeMetric(PrintWriter out, String name, String type,
                              String help, long value) {
        out.printf("# HELP %s %s%n", name, help);
        out.printf("# TYPE %s %s%n", name, type);
        out.printf("%s %d%n%n", name, value);
    }

    private void writeMetric(PrintWriter out, String name, String type,
                              String help, double value) {
        out.printf("# HELP %s %s%n", name, help);
        out.printf("# TYPE %s %s%n", name, type);
        out.printf("%s %.2f%n%n", name, value);
    }
}
