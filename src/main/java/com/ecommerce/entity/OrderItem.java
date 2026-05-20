package com.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Added: image url for display in orders page
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Getters and Setters
    public Long    getId()          { return id; }
    public void    setId(Long id)   { this.id = id; }

    public Long    getOrderId()     { return orderId; }
    public void    setOrderId(Long orderId) { this.orderId = orderId; }

    public Long    getProductId()   { return productId; }
    public void    setProductId(Long productId) { this.productId = productId; }

    public String  getProductName() { return productName; }
    public void    setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity()    { return quantity; }
    public void    setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice()    { return price; }
    public void    setPrice(BigDecimal price) { this.price = price; }

    public String  getImageUrl()    { return imageUrl; }
    public void    setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getSubtotal() {
        if (price == null || quantity == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}