package com.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
        // A product can only appear once per cart
        @UniqueConstraint(name = "uq_cart_product", columnNames = {"cart_id", "product_id"})
})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @PrePersist
    protected void onCreate() {
        if (this.addedAt == null) this.addedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────
    public CartItem() {
    }

    public CartItem(Long productId, String productName, BigDecimal price, int quantity, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public CartItem(Long productId, String productName, BigDecimal price, int quantity, String imageUrl, Long userId) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    // ── Getters & Setters ─────────────────────────────
    public Long getId() {
        return id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart c) {
        this.cart = c;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal p) {
        this.price = p;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int q) {
        this.quantity = q;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long u) {
        this.userId = u;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}