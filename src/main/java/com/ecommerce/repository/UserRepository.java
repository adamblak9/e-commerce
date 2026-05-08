package com.ecommerce.repository;

import com.ecommerce.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;

public class UserRepository {

    private final EntityManager em;

    public UserRepository(EntityManager em) {
        this.em = em;
    }

    public void save(User user) {
        em.persist(user);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public Optional<User> findByEmail(String email) {
        try {
            User user = em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean existsByEmail(String email) {
        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.email = :email AND u.id <> :id", Long.class)
                .setParameter("email", email)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByUsername(String username) {
        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByUsernameAndIdNot(String username, Long id) {
        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :username AND u.id <> :id", Long.class)
                .setParameter("username", username)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    public User update(User user) {
        return em.merge(user);
    }
}