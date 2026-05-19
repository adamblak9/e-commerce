package com.ecommerce.service;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
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
            ProductRepository productRepo = new ProductRepository(em);

            // Validate product exists and has enough stock
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            int available = product.getStockQuantity();
            if (available <= 0) {
                throw new IllegalStateException("Product is out of stock");
            }

            Cart cart = repo.getOrCreate(userId);

            // If item already exists → check total would not exceed stock
            final int[] finalQty = {quantity};
            repo.findItem(cart.getId(), productId).ifPresentOrElse(
                    existing -> {
                        int newTotal = existing.getQuantity() + quantity;
                        if (newTotal > available) {
                            newTotal = available; // cap at stock
                        }
                        finalQty[0] = newTotal - existing.getQuantity();
                        existing.setQuantity(newTotal);
                    },
                    () -> {
                        int addQty = Math.min(quantity, available);
                        finalQty[0] = addQty;
                        CartItem item = new CartItem(productId, productName, price, addQty, imageUrl, userId);
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

    // ── Update quantity with stock cap ────────────────
    public Map<String, Object> addItemChecked(Long userId, Long productId, String productName,
                                              BigDecimal price, int quantity, String imageUrl) {
        return addItem(userId, productId, productName, price, quantity, imageUrl);
    }

    // ── Update quantity ───────────────────────────────
    public Map<String, Object> updateQuantity(Long userId, Long productId, int newQty) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            CartRepository repo = new CartRepository(em);
            ProductRepository productRepo = new ProductRepository(em);
            Cart cart = repo.getOrCreate(userId);

            repo.findItem(cart.getId(), productId).ifPresent(item -> {
                if (newQty <= 0) {
                    cart.removeItem(item);
                } else {
                    // Cap at available stock
                    int available = productRepo.findById(productId)
                            .map(p -> p.getStockQuantity())
                            .orElse(newQty);
                    item.setQuantity(Math.min(newQty, available));
                }
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
                        () -> cart.addItem(new CartItem(pid, name, price, qty, img, userId))
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
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository productRepo = new ProductRepository(em);
        try {
            List<Map<String, Object>> items = cart.getItems().stream().map(i -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id",          i.getId());
                m.put("productId",   i.getProductId());
                m.put("productName", i.getProductName());
                m.put("price",       i.getPrice());
                m.put("quantity",    i.getQuantity());
                m.put("imageUrl",    i.getImageUrl());
                m.put("subtotal",    i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
                // Include stock so frontend can cap the + button
                int stock = productRepo.findById(i.getProductId())
                        .map(p -> p.getStockQuantity())
                        .orElse(i.getQuantity());
                m.put("stockQuantity", stock);
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
        } finally {
            em.close();
        }
    }
}