package com.ecommerce.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité User.
 *
 * Pourquoi ce fichier ?
 * ─────────────────────
 * User contient une logique métier pure (getFullName, isActive) qui ne nécessite
 * aucune base de données. Ces méthodes doivent être testées en isolation pour
 * garantir qu'un changement accidentel ne casse pas le comportement attendu.
 */
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("jdoe", "john@example.com", "hashed_pw", "John", "Doe");
    }

    // ── getFullName ───────────────────────────────────────────────────────────

    @Test
    void getFullName_returnsFirstNameAndLastName() {
        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void getFullName_withDifferentNames_returnsCorrectly() {
        user.setFirstName("Alice");
        user.setLastName("Smith");
        assertEquals("Alice Smith", user.getFullName());
    }

    // ── isActive ──────────────────────────────────────────────────────────────

    @Test
    void isActive_defaultStatus_isTrue() {
        // Le statut par défaut est ACTIVE
        assertTrue(user.isActive());
    }

    @Test
    void isActive_whenBanned_returnsFalse() {
        user.setStatus(User.Status.BANNED);
        assertFalse(user.isActive());
    }

    @Test
    void isActive_whenInactive_returnsFalse() {
        user.setStatus(User.Status.INACTIVE);
        assertFalse(user.isActive());
    }

    @Test
    void isActive_whenExplicitlyActive_returnsTrue() {
        user.setStatus(User.Status.ACTIVE);
        assertTrue(user.isActive());
    }

    // ── Role default ──────────────────────────────────────────────────────────

    @Test
    void defaultRole_isCustomer() {
        assertEquals(User.Role.CUSTOMER, user.getRole());
    }

    @Test
    void setRole_changeToSeller() {
        user.setRole(User.Role.SELLER);
        assertEquals(User.Role.SELLER, user.getRole());
    }

    // ── equals / hashCode ─────────────────────────────────────────────────────

    @Test
    void equals_sameEmail_returnsTrue() {
        User other = new User("other", "john@example.com", "pw", "John", "X");
        assertEquals(user, other);
    }

    @Test
    void equals_differentEmail_returnsFalse() {
        User other = new User("other", "other@example.com", "pw", "Other", "User");
        assertNotEquals(user, other);
    }

    @Test
    void hashCode_sameEmail_sameHash() {
        User other = new User("other", "john@example.com", "pw", "John", "X");
        assertEquals(user.hashCode(), other.hashCode());
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    @Test
    void setUsername_updatesValue() {
        user.setUsername("newname");
        assertEquals("newname", user.getUsername());
    }

    @Test
    void setEmail_updatesValue() {
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    void setPhoneNumber_updatesValue() {
        user.setPhoneNumber("+212600000000");
        assertEquals("+212600000000", user.getPhoneNumber());
    }

    @Test
    void toString_containsUsernameAndEmail() {
        String s = user.toString();
        assertTrue(s.contains("jdoe"));
        assertTrue(s.contains("john@example.com"));
    }
}
