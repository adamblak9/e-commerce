package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductService {

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    public Map<String, Object> createProduct(String name, String description,
                                              BigDecimal price, int stockQuantity,
                                              String category, String imageUrl,
                                              Long sellerId) {

        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);

        try {
            em.getTransaction().begin();

            Product product = new Product(name, description, price, stockQuantity, category, sellerId);
            product.setImageUrl(imageUrl);

            repo.save(product);
            em.getTransaction().commit();

            return buildResponse(em, product, null);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Get All (public listing — includes star summary, no comments)
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getAllProducts() {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);
        try {
            return repo.findAll().stream()
                    .map(p -> buildResponse(em, p, null))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Get by ID
    //   requesterId == null  → public view (no comments returned)
    //   requesterId != null  → authenticated; caller's own review/comment included
    // -------------------------------------------------------------------------

    public Map<String, Object> getProductById(Long id, Long requesterId) {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);
        try {
            Product product = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            return buildResponse(em, product, requesterId);
        } finally {
            em.close();
        }
    }

    /** Backwards-compatible overload used internally (no auth context). */
    public Map<String, Object> getProductById(Long id) {
        return getProductById(id, null);
    }

    // -------------------------------------------------------------------------
    // Get by Category
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getByCategory(String category) {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);
        try {
            return repo.findByCategory(category).stream()
                    .map(p -> buildResponse(em, p, null))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Get by Seller
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getBySeller(Long sellerId) {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);
        try {
            return repo.findBySellerId(sellerId).stream()
                    .map(p -> buildResponse(em, p, null))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> search(String keyword) {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);
        try {
            return repo.search(keyword).stream()
                    .map(p -> buildResponse(em, p, null))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    public Map<String, Object> updateProduct(Long id, String name, String description,
                                              BigDecimal price, int stockQuantity,
                                              String category, String imageUrl,
                                              String status, Long requesterId,
                                              String requesterRole) {

        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);

        try {
            em.getTransaction().begin();

            Product product = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            if (!"ADMIN".equals(requesterRole) && !product.getSellerId().equals(requesterId)) {
                throw new SecurityException("You are not authorized to update this product");
            }

            if (name        != null) product.setName(name);
            if (description != null) product.setDescription(description);
            if (price       != null) product.setPrice(price);
            if (stockQuantity >= 0)  product.setStockQuantity(stockQuantity);
            if (category    != null) product.setCategory(category);
            if (imageUrl    != null) product.setImageUrl(imageUrl);
            if (status      != null) {
                try {
                    product.setStatus(Product.ProductStatus.valueOf(status));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid status value: " + status);
                }
            }

            Product updated = repo.update(product);
            em.getTransaction().commit();

            return buildResponse(em, updated, requesterId);

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

    public void deleteProduct(Long id, Long requesterId, String requesterRole) {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);

        try {
            em.getTransaction().begin();

            Product product = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            if (!"ADMIN".equals(requesterRole) && !product.getSellerId().equals(requesterId)) {
                throw new SecurityException("You are not authorized to delete this product");
            }

            repo.delete(id);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Helper — builds the product map.
    //
    //  Always included (for store cards):
    //    averageRating  — rounded to 1 decimal, 0.0 if no ratings yet
    //    ratingCount    — number of reviews that carry a star rating
    //
    //  Included only on product-detail calls by the review author:
    //    myReview       — { id, rating, comment, createdAt, updatedAt }
    //                     present only when requesterId != null AND the
    //                     requester has previously reviewed this product.
    // -------------------------------------------------------------------------

    private Map<String, Object> buildResponse(EntityManager em, Product p, Long requesterId) {
        ReviewRepository reviewRepo = new ReviewRepository(em);
        UserRepository   userRepo   = new UserRepository(em);

        double avg   = reviewRepo.averageRatingByProduct(p.getId());
        long   count = reviewRepo.countByRatingForProduct(p.getId());

        // Resolve seller name
        String sellerName = userRepo.findById(p.getSellerId())
                .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                .orElse("Seller #" + p.getSellerId());

        Map<String, Object> map = new HashMap<>();
        map.put("id",            p.getId());
        map.put("name",          p.getName());
        map.put("description",   p.getDescription());
        map.put("price",         p.getPrice());
        map.put("stockQuantity", p.getStockQuantity());
        map.put("category",      p.getCategory());
        map.put("imageUrl",      p.getImageUrl());
        map.put("sellerId",      p.getSellerId());
        map.put("sellerName",    sellerName);
        map.put("status",        p.getStatus().name());
        map.put("available",     p.isAvailable());
        map.put("createdAt",     p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
        map.put("updatedAt",     p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null);

        // Star summary — always present
        map.put("averageRating", Math.round(avg * 10.0) / 10.0);
        map.put("ratingCount",   count);

        // Own review (comment + rating) — only for the authenticated author
        if (requesterId != null) {
            reviewRepo.findByProductAndUser(p.getId(), requesterId).ifPresent(r -> {
                Map<String, Object> myReview = new HashMap<>();
                myReview.put("id",        r.getId());
                myReview.put("rating",    r.getRating());
                myReview.put("comment",   r.getComment());
                myReview.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
                myReview.put("updatedAt", r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null);
                map.put("myReview", myReview);
            });
        }

        return map;
    }
}
