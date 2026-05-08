package com.ecommerce.service;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JPAUtil;
import com.ecommerce.util.PasswordUtil;
import jakarta.persistence.EntityManager;

import java.util.HashMap;
import java.util.Map;

public class ProfileService {

    // -------------------------------------------------------------------------
    // Get profile
    // -------------------------------------------------------------------------

    public Map<String, Object> getProfile(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            User user = repo.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return toMap(user);
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Update personal info (name, username, email, phone)
    // -------------------------------------------------------------------------

    public Map<String, Object> updateInfo(Long userId,
                                          String firstName, String lastName,
                                          String username, String email,
                                          String phoneNumber) {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            em.getTransaction().begin();

            User user = repo.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (isBlank(firstName) || isBlank(lastName) || isBlank(username) || isBlank(email)) {
                throw new IllegalArgumentException("firstName, lastName, username and email are required");
            }

            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new IllegalArgumentException("Email is not valid");
            }

            if (repo.existsByEmailAndIdNot(email, userId)) {
                throw new IllegalArgumentException("Email is already in use by another account");
            }

            if (repo.existsByUsernameAndIdNot(username, userId)) {
                throw new IllegalArgumentException("Username is already taken");
            }

            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setUsername(username.trim());
            user.setEmail(email.trim());
            user.setPhoneNumber(isBlank(phoneNumber) ? null : phoneNumber.trim());

            repo.update(user);
            em.getTransaction().commit();

            return toMap(user);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Change password
    // -------------------------------------------------------------------------

    public void changePassword(Long userId, String currentPassword,
                               String newPassword, String confirmPassword) {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            em.getTransaction().begin();

            User user = repo.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!PasswordUtil.verify(currentPassword, user.getPasswordHash())) {
                throw new SecurityException("Current password is incorrect");
            }

            if (isBlank(newPassword) || newPassword.length() < 8) {
                throw new IllegalArgumentException("New password must be at least 8 characters");
            }

            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("New passwords do not match");
            }

            user.setPasswordHash(PasswordUtil.hash(newPassword));
            repo.update(user);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Delete (deactivate) account
    // -------------------------------------------------------------------------

    public void deleteAccount(Long userId, String password) {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            em.getTransaction().begin();

            User user = repo.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!PasswordUtil.verify(password, user.getPasswordHash())) {
                throw new SecurityException("Password is incorrect");
            }

            user.setStatus(User.Status.INACTIVE);
            repo.update(user);
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

    private Map<String, Object> toMap(User user) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",          user.getId());
        m.put("username",    user.getUsername());
        m.put("email",       user.getEmail());
        m.put("firstName",   user.getFirstName());
        m.put("lastName",    user.getLastName());
        m.put("fullName",    user.getFullName());
        m.put("phoneNumber", user.getPhoneNumber());
        m.put("role",        user.getRole().name());
        m.put("status",      user.getStatus().name());
        m.put("createdAt",   user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        m.put("lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
        return m;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
