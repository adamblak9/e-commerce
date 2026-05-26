package com.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a customer review for a product.
 * Either rating or comment (or both) must be provided — neither is mandatory on its own.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uq_reviews_product_user",
                          columnNames = {"product_id", "user_id"})
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Rating from 1 (poor) to 5 (excellent).
     * Nullable — a user may leave a comment without selecting a star rating.
     */
    @Column(name = "rating", nullable = true)
    private Integer rating;

    /** Optional free-text comment. */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Review() {}

    public Review(Long productId, Long userId, int rating, String comment) {
        this.productId = productId;
        this.userId    = userId;
        this.rating    = rating;
        this.comment   = comment;
    }

    // -------------------------------------------------------------------------
    // Validation helper
    // -------------------------------------------------------------------------

    /** If a rating is provided, it must be between 1 and 5. */
    public static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public Long    getId()                    { return id; }

    public Long    getProductId()             { return productId; }
    public void    setProductId(Long v)       { this.productId = v; }

    public Long    getUserId()                { return userId; }
    public void    setUserId(Long v)          { this.userId = v; }

    public Integer getRating()                { return rating; }
    public void    setRating(Integer v)       { this.rating = v; }
    public void    setRating(int v)           { this.rating = Integer.valueOf(v); }

    public String  getComment()               { return comment; }
    public void    setComment(String v)       { this.comment = v; }

    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }

    // -------------------------------------------------------------------------
    // equals / hashCode / toString
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review)) return false;
        return Objects.equals(id, ((Review) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Review{id=" + id + ", productId=" + productId +
               ", userId=" + userId + ", rating=" + rating + "}";
    }
}
