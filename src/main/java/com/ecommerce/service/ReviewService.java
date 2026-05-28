package com.ecommerce.service;

import com.ecommerce.entity.Review;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReviewService {

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Adds a review for a product.
     * A user can only review each product once.
     *
     * Business rule: at least one of rating or comment must be non-null.
     * Rating is optional (1-5 when provided). Comment is optional.
     */
    public Map<String, Object> addReview(Long productId, Long userId,
                                          Integer rating, String comment) {

        if (rating != null) Review.validateRating(rating);

        // Ensure at least one field is provided
        boolean hasComment = comment != null && !comment.trim().isEmpty();
        if (rating == null && !hasComment) {
            throw new IllegalArgumentException(
                "Please provide at least a rating or a comment.");
        }

        EntityManager em = JPAUtil.getEntityManager();
        ReviewRepository repo = new ReviewRepository(em);

        try {
            em.getTransaction().begin();

            // Prevent duplicate reviews (one per user per product)
            if (repo.findByProductAndUser(productId, userId).isPresent()) {
                throw new IllegalStateException("You have already reviewed this product");
            }

            Review review = new Review();
            review.setProductId(productId);
            review.setUserId(userId);
            review.setRating(rating);
            review.setComment(comment);
            repo.save(review);
            em.getTransaction().commit();

            return buildResponse(review);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Read – list for a product
    // -------------------------------------------------------------------------

    /**
     * Returns aggregated rating statistics for a product (public endpoint).
     * Individual comments are NOT included here — a user's own comment is
     * returned via the product-detail endpoint in the "myReview" field.
     * Average rating is calculated only from reviews that have a rating.
     */
    public Map<String, Object> getProductReviews(Long productId) {
        EntityManager em = JPAUtil.getEntityManager();
        ReviewRepository repo = new ReviewRepository(em);

        try {
            UserRepository userRepo = new UserRepository(em);

            // Public list: include rating + reviewer name, but omit the comment text
            List<Map<String, Object>> reviews = repo.findByProductId(productId).stream()
                    .map(r -> {
                        Map<String, Object> m = new java.util.HashMap<>();
                        m.put("id",        r.getId());
                        m.put("productId", r.getProductId());
                        m.put("userId",    r.getUserId());
                        // Resolve reviewer's display name
                        String name = userRepo.findById(r.getUserId())
                                .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                                .orElse("User #" + r.getUserId());
                        m.put("userName",  name);
                        m.put("rating",    r.getRating());
                        m.put("comment",   r.getComment());
                        // comment intentionally omitted — visible only to its author via myReview
                        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
                        return m;
                    })
                    .collect(Collectors.toList());

            double average = repo.averageRatingByProduct(productId);
            long total     = repo.countByProductId(productId);
            long rated     = repo.countByRatingForProduct(productId);

            // Build distribution map {1: n, 2: n, …, 5: n}
            Map<Integer, Long> distribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
            for (Object[] row : repo.ratingDistribution(productId)) {
                distribution.put((Integer) row[0], (Long) row[1]);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("reviews",       reviews);
            result.put("totalReviews",  total);
            result.put("ratingCount",   rated);
            result.put("averageRating", Math.round(average * 10.0) / 10.0);
            result.put("distribution",  distribution);
            return result;

        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Read – single review
    // -------------------------------------------------------------------------

    public Map<String, Object> getReviewById(Long reviewId) {
        EntityManager em = JPAUtil.getEntityManager();
        ReviewRepository repo = new ReviewRepository(em);
        try {
            Review review = repo.findById(reviewId)
                    .orElseThrow(() -> new IllegalArgumentException("Review not found"));
            return buildResponse(review);
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Read – reviews by user
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getReviewsByUser(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        ReviewRepository repo = new ReviewRepository(em);
        try {
            return repo.findByUserId(userId).stream()
                    .map(this::buildResponse)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing review.
     * Only the original author (or an ADMIN) may edit.
     * Passing null for rating or comment leaves that field unchanged.
     */
    public Map<String, Object> updateReview(Long reviewId, Long requesterId,
                                             String requesterRole,
                                             Integer rating, String comment) {

        if (rating != null) Review.validateRating(rating);

        EntityManager em = JPAUtil.getEntityManager();
        ReviewRepository repo = new ReviewRepository(em);

        try {
            em.getTransaction().begin();

            Review review = repo.findById(reviewId)
                    .orElseThrow(() -> new IllegalArgumentException("Review not found"));

            if (!"ADMIN".equals(requesterRole) && !review.getUserId().equals(requesterId)) {
                throw new SecurityException("You are not authorized to edit this review");
            }

            if (rating  != null) review.setRating(rating);
            if (comment != null) review.setComment(comment.trim().isEmpty() ? null : comment.trim());

            // After update, ensure at least one field remains
            if (review.getRating() == null && (review.getComment() == null
                    || review.getComment().isBlank())) {
                throw new IllegalArgumentException(
                    "A review must have at least a rating or a comment.");
            }

            Review updated = repo.update(review);
            em.getTransaction().commit();

            return buildResponse(updated);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    public void deleteReview(Long reviewId, Long requesterId, String requesterRole) {
        EntityManager em = JPAUtil.getEntityManager();
        ReviewRepository repo = new ReviewRepository(em);

        try {
            em.getTransaction().begin();

            Review review = repo.findById(reviewId)
                    .orElseThrow(() -> new IllegalArgumentException("Review not found"));

            if (!"ADMIN".equals(requesterRole) && !review.getUserId().equals(requesterId)) {
                throw new SecurityException("You are not authorized to delete this review");
            }

            repo.delete(reviewId);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Map<String, Object> buildResponse(Review r) {
        EntityManager em2 = JPAUtil.getEntityManager();
        try {
            String name = new UserRepository(em2).findById(r.getUserId())
                    .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                    .orElse("User #" + r.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("id",        r.getId());
            map.put("productId", r.getProductId());
            map.put("userId",    r.getUserId());
            map.put("userName",  name);
            map.put("rating",    r.getRating());
            map.put("comment",   r.getComment());
            map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            map.put("updatedAt", r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null);
            return map;
        } finally {
            em2.close();
        }
    }
}
