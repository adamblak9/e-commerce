package com.ecommerce.service;

import com.ecommerce.entity.Category;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CategoryService {

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    public Map<String, Object> createCategory(String name, String description, String icon) {
        EntityManager em = JPAUtil.getEntityManager();
        CategoryRepository repo = new CategoryRepository(em);
        try {
            em.getTransaction().begin();

            if (isBlank(name)) throw new IllegalArgumentException("Category name is required");

            if (repo.existsByName(name.trim()))
                throw new IllegalArgumentException("A category with that name already exists");

            String slug = toSlug(name);

            Category category = new Category(name.trim(), description, slug, icon);
            repo.save(category);
            em.getTransaction().commit();

            return toMap(category);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // List all
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getAllCategories() {
        EntityManager em = JPAUtil.getEntityManager();
        CategoryRepository repo = new CategoryRepository(em);
        try {
            return repo.findAll().stream().map(this::toMap).collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    public List<Map<String, Object>> getActiveCategories() {
        EntityManager em = JPAUtil.getEntityManager();
        CategoryRepository repo = new CategoryRepository(em);
        try {
            return repo.findAllActive().stream().map(this::toMap).collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Get by ID
    // -------------------------------------------------------------------------

    public Map<String, Object> getCategoryById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        CategoryRepository repo = new CategoryRepository(em);
        try {
            Category category = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            return toMap(category);
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    public Map<String, Object> updateCategory(Long id, String name, String description,
                                               String icon, Boolean active) {
        EntityManager em = JPAUtil.getEntityManager();
        CategoryRepository repo = new CategoryRepository(em);
        try {
            em.getTransaction().begin();

            Category category = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));

            if (!isBlank(name)) {
                if (repo.existsByNameAndIdNot(name.trim(), id))
                    throw new IllegalArgumentException("A category with that name already exists");
                category.setName(name.trim());
                category.setSlug(toSlug(name));
            }

            if (description != null) category.setDescription(description);
            if (icon        != null) category.setIcon(icon);
            if (active      != null) category.setActive(active);

            repo.update(category);
            em.getTransaction().commit();

            return toMap(category);
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

    public void deleteCategory(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        CategoryRepository repo = new CategoryRepository(em);
        try {
            em.getTransaction().begin();
            Category category = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            repo.delete(category);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> toMap(Category c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",          c.getId());
        m.put("name",        c.getName());
        m.put("description", c.getDescription());
        m.put("slug",        c.getSlug());
        m.put("icon",        c.getIcon());
        m.put("active",      c.isActive());
        m.put("createdAt",   c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
        m.put("updatedAt",   c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null);
        return m;
    }

    private String toSlug(String name) {
        return name.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
