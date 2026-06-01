package com.ecommerce.service;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JPAUtil;
import com.ecommerce.util.JwtUtil;
import com.ecommerce.util.PasswordUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    // -------------------------------------------------------------------------
    // Signup good
    // -------------------------------------------------------------------------

    public Map<String, Object> signup(String username, String email,
                                      String password, String firstName,
                                      String lastName, String phoneNumber,
                                      String role) {

        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);

        try {
            em.getTransaction().begin();

            if (repo.existsByEmail(email)) {
                throw new IllegalArgumentException("Email is already in use");
            }
            if (repo.existsByUsername(username)) {
                throw new IllegalArgumentException("Username is already taken");
            }

            User user = new User(
                    username, email,
                    PasswordUtil.hash(password),
                    firstName, lastName
            );
            user.setPhoneNumber(phoneNumber);

            // Set role — only CUSTOMER or SELLER allowed via self-registration
            try {
                user.setRole(User.Role.valueOf(role));
            } catch (Exception e) {
                user.setRole(User.Role.CUSTOMER); // fallback
            }

            repo.save(user);
            em.getTransaction().commit();

            String token = JwtUtil.generate(user.getId(), user.getEmail(), user.getRole().name());
            return buildResponse(token, user);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    public Map<String, Object> login(String email, String password) {

        EntityManager em = JPAUtil.getEntityManager();
        UserRepository repo = new UserRepository(em);

        try {
            em.getTransaction().begin();

            User user = repo.findByEmail(email)
                    .orElseThrow(() -> new SecurityException("Invalid email or password"));

            if (!PasswordUtil.verify(password, user.getPasswordHash())) {
                throw new SecurityException("Invalid email or password");
            }

            if (!user.isActive()) {
                throw new SecurityException("Account is inactive or banned");
            }

            user.setLastLoginAt(LocalDateTime.now());
            repo.update(user);
            em.getTransaction().commit();

            String token = JwtUtil.generate(user.getId(), user.getEmail(), user.getRole().name());
            return buildResponse(token, user);

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

    private Map<String, Object> buildResponse(String token, User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id",          user.getId());
        userMap.put("username",    user.getUsername());
        userMap.put("email",       user.getEmail());
        userMap.put("firstName",   user.getFirstName());
        userMap.put("lastName",    user.getLastName());
        userMap.put("fullName",    user.getFullName());
        userMap.put("role",        user.getRole().name());
        userMap.put("status",      user.getStatus().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token",     token);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", JwtUtil.getExpirationSeconds());
        response.put("user",      userMap);
        return response;
    }
}