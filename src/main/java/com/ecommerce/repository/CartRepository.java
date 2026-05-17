package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;

public class CartRepository {

    private final EntityManager em;

    public CartRepository(EntityManager em) { this.em = em; }

    /** Get cart by userId — returns empty if user has no cart yet */
    public Optional<Cart> findByUserId(Long userId) {
        try {
            Cart cart = em.createQuery(
                            "SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :uid", Cart.class)
                    .setParameter("uid", userId)
                    .getSingleResult();
            return Optional.of(cart);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /** Get or create a cart for the user */
    public Cart getOrCreate(Long userId) {
        return findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart(userId);
            em.persist(cart);
            return cart;
        });
    }

    public Cart save(Cart cart) {
        if (cart.getId() == null) {
            em.persist(cart); return cart;
        }
        return em.merge(cart);
    }

    public void removeItem(CartItem item) {
        CartItem managed = em.contains(item) ? item : em.merge(item);
        em.remove(managed);
    }

    public Optional<CartItem> findItem(Long cartId, Long productId) {
        try {
            CartItem item = em.createQuery(
                            "SELECT i FROM CartItem i WHERE i.cart.id = :cid AND i.productId = :pid", CartItem.class)
                    .setParameter("cid", cartId)
                    .setParameter("pid", productId)
                    .getSingleResult();
            return Optional.of(item);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}