package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

public class ProductRepository {

    private final EntityManager em;

    public ProductRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Product product) {
        em.persist(product);
    }

    public Optional<Product> findById(Long id) {
        Product product = em.find(Product.class, id);
        return Optional.ofNullable(product);
    }

    public List<Product> findAll() {
        return em.createQuery("SELECT p FROM Product p ORDER BY p.createdAt DESC", Product.class)
                .getResultList();
    }

    public List<Product> findByCategory(String category) {
        return em.createQuery(
                "SELECT p FROM Product p WHERE p.category = :category ORDER BY p.createdAt DESC",
                Product.class)
                .setParameter("category", category)
                .getResultList();
    }

    public List<Product> findBySellerId(Long sellerId) {
        return em.createQuery(
                "SELECT p FROM Product p WHERE p.sellerId = :sellerId ORDER BY p.createdAt DESC",
                Product.class)
                .setParameter("sellerId", sellerId)
                .getResultList();
    }

    public List<Product> findByStatus(Product.ProductStatus status) {
        return em.createQuery(
                "SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdAt DESC",
                Product.class)
                .setParameter("status", status)
                .getResultList();
    }

    public List<Product> search(String keyword) {
        String kw = "%" + keyword.toLowerCase() + "%";
        return em.createQuery(
                "SELECT p FROM Product p WHERE LOWER(p.name) LIKE :kw OR LOWER(p.description) LIKE :kw ORDER BY p.createdAt DESC",
                Product.class)
                .setParameter("kw", kw)
                .getResultList();
    }

    public Product update(Product product) {
        return em.merge(product);
    }

    public void delete(Long id) {
        Product product = em.find(Product.class, id);
        if (product != null) em.remove(product);
    }

    public boolean existsById(Long id) {
        Long count = em.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    public long countAll() {
        return em.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
    }

    public long countByStatus(Product.ProductStatus status) {
        return em.createQuery("SELECT COUNT(p) FROM Product p WHERE p.status = :status", Long.class)
                .setParameter("status", status).getSingleResult();
    }
}
