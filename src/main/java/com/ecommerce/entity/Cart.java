package com.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One cart per user — unique constraint ensures this
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // ── Constructors ──────────────────────────────────
    public Cart() {}
    public Cart(Long userId) { this.userId = userId; }

    // ── Helpers ───────────────────────────────────────
    public void addItem(CartItem item) {
        item.setCart(this);
        this.items.add(item);
    }

    public void removeItem(CartItem item) {
        this.items.remove(item);
        item.setCart(null);
    }

    // ── Getters & Setters ─────────────────────────────
    public Long            getId()        { return id; }
    public Long            getUserId()    { return userId; }
    public void            setUserId(Long u) { this.userId = u; }
    public List<CartItem>  getItems()     { return items; }
    public LocalDateTime   getUpdatedAt() { return updatedAt; }
}