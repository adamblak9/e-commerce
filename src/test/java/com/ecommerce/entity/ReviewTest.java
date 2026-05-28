package com.ecommerce.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    @Test
    void constructor_setsAllFields() {
        Review r = new Review(1L, 2L, 5, "Great product!");
        assertEquals(1L, r.getProductId());
        assertEquals(2L, r.getUserId());
        assertEquals(5,  r.getRating());
        assertEquals("Great product!", r.getComment());
    }

    @Test
    void validateRating_acceptsBoundaryValues() {
        assertDoesNotThrow(() -> Review.validateRating(1));
        assertDoesNotThrow(() -> Review.validateRating(5));
    }

    @Test
    void validateRating_rejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> Review.validateRating(0));
    }

    @Test
    void validateRating_rejectsSix() {
        assertThrows(IllegalArgumentException.class, () -> Review.validateRating(6));
    }

    @Test
    void validateRating_rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Review.validateRating(-1));
    }

    @Test
    void setters_work() {
        Review r = new Review();
        r.setProductId(10L);
        r.setUserId(20L);
        r.setRating(3);
        r.setComment("Okay");

        assertEquals(10L,   r.getProductId());
        assertEquals(20L,   r.getUserId());
        assertEquals(3,     r.getRating());
        assertEquals("Okay", r.getComment());
    }

    @Test
    void equals_basedOnId() {
        Review a = new Review(1L, 2L, 4, "Good");
        Review b = new Review(1L, 3L, 5, "Excellent");
        // Both have null IDs → equal (same object class, same null id)
        assertEquals(a, b);
    }
}
