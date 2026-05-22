package com.ecommerce.service;

import com.ecommerce.entity.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JPAUtil;
import com.ecommerce.util.PasswordUtil;
import jakarta.persistence.EntityManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminService {

    // -------------------------------------------------------------------------
    // Dashboard stats
    // -------------------------------------------------------------------------

    public Map<String, Object> getDashboardStats() {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository    userRepo    = new UserRepository(em);
        ProductRepository productRepo = new ProductRepository(em);
        OrderRepository   orderRepo   = new OrderRepository(em);

        try {
            Map<String, Object> stats = new HashMap<>();

            // User stats
            stats.put("totalUsers",     userRepo.countAll());
            stats.put("totalCustomers", userRepo.countByRole(User.Role.CUSTOMER));
            stats.put("totalSellers",   userRepo.countByRole(User.Role.SELLER));
            stats.put("totalAdmins",    userRepo.countByRole(User.Role.ADMIN));
            stats.put("bannedUsers",    userRepo.countByStatus(User.Status.BANNED));
            stats.put("inactiveUsers",  userRepo.countByStatus(User.Status.INACTIVE));

            // Product stats
            stats.put("totalProducts",  productRepo.countAll());
            stats.put("activeProducts", productRepo.countByStatus(com.ecommerce.entity.Product.ProductStatus.ACTIVE));

            // Order stats
            stats.put("totalOrders",   orderRepo.countAll());
            stats.put("pendingOrders", orderRepo.countByStatus(com.ecommerce.entity.Order.OrderStatus.PENDING));
            stats.put("totalRevenue",  orderRepo.sumRevenue());

            // Recent users (last 5)
            List<Map<String, Object>> recentUsers = userRepo.findAll().stream()
                    .limit(5)
                    .map(this::userToMap)
                    .collect(Collectors.toList());
            stats.put("recentUsers", recentUsers);

            return stats;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // User management
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getAllUsers() {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            return repo.findAll().stream().map(this::userToMap).collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    public Map<String, Object> getUserById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            User user = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return userToMap(user);
        } finally {
            em.close();
        }
    }

    public Map<String, Object> updateUserRole(Long adminId, Long targetUserId, String role) {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            em.getTransaction().begin();

            if (adminId.equals(targetUserId))
                throw new IllegalArgumentException("You cannot change your own role");

            User user = repo.findById(targetUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            User.Role newRole;
            try {
                newRole = User.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role. Must be CUSTOMER, SELLER, or ADMIN");
            }

            user.setRole(newRole);
            repo.update(user);
            em.getTransaction().commit();

            return userToMap(user);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Map<String, Object> updateUserStatus(Long adminId, Long targetUserId, String status) {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            em.getTransaction().begin();

            if (adminId.equals(targetUserId))
                throw new IllegalArgumentException("You cannot change your own status");

            User user = repo.findById(targetUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            User.Status newStatus;
            try {
                newStatus = User.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status. Must be ACTIVE, INACTIVE, or BANNED");
            }

            user.setStatus(newStatus);
            repo.update(user);
            em.getTransaction().commit();

            return userToMap(user);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Map<String, Object> resetUserPassword(Long adminId, Long targetUserId, String newPassword) {
        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            em.getTransaction().begin();

            User user = repo.findById(targetUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (isBlank(newPassword) || newPassword.length() < 8)
                throw new IllegalArgumentException("Password must be at least 8 characters");

            user.setPasswordHash(PasswordUtil.hash(newPassword));
            repo.update(user);
            em.getTransaction().commit();

            return Map.of("message", "Password reset successfully", "userId", targetUserId);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteUser(Long adminId, Long targetUserId) {
        if (adminId.equals(targetUserId))
            throw new IllegalArgumentException("You cannot delete your own account");

        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);
        try {
            em.getTransaction().begin();
            User user = repo.findById(targetUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            // Soft delete — set inactive
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
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> userToMap(User user) {
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
