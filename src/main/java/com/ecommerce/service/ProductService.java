package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
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

            return buildResponse(product);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Get All
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getAllProducts() {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);
        try {
            return repo.findAll().stream()
                    .map(this::buildResponse)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Get by ID
    // -------------------------------------------------------------------------

    public Map<String, Object> getProductById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);
        try {
            Product product = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            return buildResponse(product);
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Get by Category
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getByCategory(String category) {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);
        try {
            return repo.findByCategory(category).stream()
                    .map(this::buildResponse)
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
                    .map(this::buildResponse)
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
                    .map(this::buildResponse)
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

            // Only the seller who owns it or an ADMIN can update
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

    public void deleteProduct(Long id, Long requesterId, String requesterRole) {
        EntityManager em = JPAUtil.getEntityManager();
        ProductRepository repo = new ProductRepository(em);

        try {
            em.getTransaction().begin();

            Product product = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            // Only the seller who owns it or an ADMIN can delete
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
    // Helper
    // -------------------------------------------------------------------------

    private Map<String, Object> buildResponse(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",            p.getId());
        map.put("name",          p.getName());
        map.put("description",   p.getDescription());
        map.put("price",         p.getPrice());
        map.put("stockQuantity", p.getStockQuantity());
        map.put("category",      p.getCategory());
        map.put("imageUrl",      p.getImageUrl());
        map.put("sellerId",      p.getSellerId());
        map.put("status",        p.getStatus().name());
        map.put("available",     p.isAvailable());
        map.put("createdAt",     p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
        map.put("updatedAt",     p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null);
        return map;
    }
}
