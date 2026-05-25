package com.ecommerce.repository;

import com.ecommerce.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

public class ReviewRepository {

    private final EntityManager em;

    public ReviewRepository(EntityManager em) {
        this.em = em;
    }

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    public void save(Review review) {
        em.persist(review);
    }

    public Review update(Review review) {
        return em.merge(review);
    }

    public void delete(Long id) {
        Review r = em.find(Review.class, id);
        if (r != null) em.remove(r);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(em.find(Review.class, id));
    }

    /** All reviews for a given product, newest first. */
    public List<Review> findByProductId(Long productId) {
        return em.createQuery(
                "SELECT r FROM Review r WHERE r.productId = :pid ORDER BY r.createdAt DESC",
                Review.class)
                .setParameter("pid", productId)
                .getResultList();
    }

    /** All reviews written by a given user. */
    public List<Review> findByUserId(Long userId) {
        return em.createQuery(
                "SELECT r FROM Review r WHERE r.userId = :uid ORDER BY r.createdAt DESC",
                Review.class)
                .setParameter("uid", userId)
                .getResultList();
    }

    /** Check whether a user has already reviewed this product. */
    public Optional<Review> findByProductAndUser(Long productId, Long userId) {
        try {
            Review r = em.createQuery(
                    "SELECT r FROM Review r WHERE r.productId = :pid AND r.userId = :uid",
                    Review.class)
                    .setParameter("pid", productId)
                    .setParameter("uid", userId)
                    .getSingleResult();
            return Optional.of(r);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // -------------------------------------------------------------------------
    // Aggregates — only considers reviews that have a rating (rating IS NOT NULL)
    // -------------------------------------------------------------------------

    /** Average rating for a product (returns 0.0 if no rated reviews exist). */
    public double averageRatingByProduct(Long productId) {
        Double avg = em.createQuery(
                "SELECT AVG(CAST(r.rating AS double)) FROM Review r " +
                "WHERE r.productId = :pid AND r.rating IS NOT NULL",
                Double.class)
                .setParameter("pid", productId)
                .getSingleResult();
        return avg != null ? avg : 0.0;
    }

    /** Total number of reviews (includes comment-only reviews). */
    public long countByProductId(Long productId) {
        return em.createQuery(
                "SELECT COUNT(r) FROM Review r WHERE r.productId = :pid",
                Long.class)
                .setParameter("pid", productId)
                .getSingleResult();
    }

    /**
     * Number of reviews that carry a star rating (rating IS NOT NULL).
     * Used for the "X ratings" counter shown on product cards in the store.
     */
    public long countByRatingForProduct(Long productId) {
        return em.createQuery(
                "SELECT COUNT(r) FROM Review r WHERE r.productId = :pid AND r.rating IS NOT NULL",
                Long.class)
                .setParameter("pid", productId)
                .getSingleResult();
    }

    /**
     * Rating distribution for a product (only from reviews that have a rating).
     * Returns a list of Object[]{rating, count} pairs.
     */
    public List<Object[]> ratingDistribution(Long productId) {
        return em.createQuery(
                "SELECT r.rating, COUNT(r) FROM Review r " +
                "WHERE r.productId = :pid AND r.rating IS NOT NULL " +
                "GROUP BY r.rating ORDER BY r.rating",
                Object[].class)
                .setParameter("pid", productId)
                .getResultList();
    }
}
