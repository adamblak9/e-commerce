package com.ecommerce.service;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartService {

    // ── Get cart ──────────────────────────────────────
    public Map<String, Object> getCart(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            CartRepository repo = new CartRepository(em);
            Cart cart = repo.getOrCreate(userId);
            em.getTransaction().begin();
            em.getTransaction().commit();
            return buildResponse(cart);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    // ── Add or update item ────────────────────────────
    public Map<String, Object> addItem(Long userId, Long productId, String productName,
                                       BigDecimal price, int quantity, String imageUrl) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            CartRepository repo = new CartRepository(em);
            Cart cart = repo.getOrCreate(userId);

            // If item already exists → just increase quantity
            repo.findItem(cart.getId(), productId).ifPresentOrElse(
                    existing -> existing.setQuantity(existing.getQuantity() + quantity),
                    () -> {
                        CartItem item = new CartItem(productId, productName, price, quantity, imageUrl);
                        cart.addItem(item);
                    }
            );

            repo.save(cart);
            em.getTransaction().commit();
            return buildResponse(cart);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    // ── Update quantity ───────────────────────────────
    public Map<String, Object> updateQuantity(Long userId, Long productId, int newQty) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            CartRepository repo = new CartRepository(em);
            Cart cart = repo.getOrCreate(userId);

            repo.findItem(cart.getId(), productId).ifPresent(item -> {
                if (newQty <= 0) cart.removeItem(item);
                else item.setQuantity(newQty);
            });

            repo.save(cart);
            em.getTransaction().commit();
            return buildResponse(cart);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    // ── Remove item ───────────────────────────────────
    public Map<String, Object> removeItem(Long userId, Long productId) {
        return updateQuantity(userId, productId, 0);
    }

    // ── Clear cart ────────────────────────────────────
    public Map<String, Object> clearCart(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            CartRepository repo = new CartRepository(em);
            Cart cart = repo.getOrCreate(userId);
            cart.getItems().clear();
            repo.save(cart);
            em.getTransaction().commit();
            return buildResponse(cart);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    // ── Merge guest cart into user cart on login ──────
    public Map<String, Object> mergeGuestCart(Long userId, List<Map<String, Object>> guestItems) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            CartRepository repo = new CartRepository(em);
            Cart cart = repo.getOrCreate(userId);

            for (Map<String, Object> g : guestItems) {
                Long      pid  = Long.valueOf(g.get("id").toString());
                String    name = g.get("name").toString();
                BigDecimal price = new BigDecimal(g.get("price").toString());
                int       qty  = Integer.parseInt(g.get("qty").toString());
                String    img  = g.getOrDefault("imageUrl", "").toString();

                repo.findItem(cart.getId(), pid).ifPresentOrElse(
                        ex -> ex.setQuantity(ex.getQuantity() + qty),
                        () -> cart.addItem(new CartItem(pid, name, price, qty, img))
                );
            }

            repo.save(cart);
            em.getTransaction().commit();
            return buildResponse(cart);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    // ── Build JSON response ───────────────────────────
    private Map<String, Object> buildResponse(Cart cart) {
        List<Map<String, Object>> items = cart.getItems().stream().map(i -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",          i.getId());
            m.put("productId",   i.getProductId());
            m.put("productName", i.getProductName());
            m.put("price",       i.getPrice());
            m.put("quantity",    i.getQuantity());
            m.put("imageUrl",    i.getImageUrl());
            m.put("subtotal",    i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
            return m;
        }).collect(Collectors.toList());

        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> res = new HashMap<>();
        res.put("cartId", cart.getId());
        res.put("userId", cart.getUserId());
        res.put("items",  items);
        res.put("total",  total);
        res.put("count",  items.stream().mapToInt(i -> (int) i.get("quantity")).sum());
        return res;
    }
}