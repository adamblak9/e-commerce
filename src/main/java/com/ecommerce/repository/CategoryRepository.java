package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

public class CategoryRepository {

    private final EntityManager em;

    public CategoryRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Category category) {
        em.persist(category);
    }

    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(em.find(Category.class, id));
    }

    public Optional<Category> findBySlug(String slug) {
        try {
            Category c = em.createQuery(
                            "SELECT c FROM Category c WHERE c.slug = :slug", Category.class)
                    .setParameter("slug", slug)
                    .getSingleResult();
            return Optional.of(c);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean existsByName(String name) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM Category c WHERE LOWER(c.name) = LOWER(:name)", Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByNameAndIdNot(String name, Long id) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.id <> :id", Long.class)
                .setParameter("name", name)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    public List<Category> findAll() {
        return em.createQuery("SELECT c FROM Category c ORDER BY c.name", Category.class)
                .getResultList();
    }

    public List<Category> findAllActive() {
        return em.createQuery("SELECT c FROM Category c WHERE c.active = true ORDER BY c.name", Category.class)
                .getResultList();
    }

    public void delete(Category category) {
        em.remove(em.contains(category) ? category : em.merge(category));
    }

    public Category update(Category category) {
        return em.merge(category);
    }
}
