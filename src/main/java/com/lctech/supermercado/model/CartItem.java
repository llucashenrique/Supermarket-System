package com.lctech.supermercado.model;

import jakarta.persistence.*;
import javafx.beans.property.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private double quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Transient properties for JavaFX integration
    @Transient
    private final DoubleProperty quantityProperty = new SimpleDoubleProperty();

    @Transient
    private final ObjectProperty<BigDecimal> totalPriceProperty = new SimpleObjectProperty<>();

    // Construtor vazio para JPA
    public CartItem() {}

    // Construtor para adicionar ao carrinho
    public CartItem(Order order, Product product, double quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        updateJavaFxProperties();
    }

    public CartItem(Order order, Product product, double quantity, double totalPrice) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = BigDecimal.valueOf(totalPrice);
        updateJavaFxProperties();
    }

    @PostLoad
    private void updateJavaFxProperties() {
        this.quantityProperty.set(this.quantity);
        this.totalPriceProperty.set(this.totalPrice);
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        updateJavaFxProperties();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
        updateJavaFxProperties();
    }

    // JavaFX Properties
    public DoubleProperty quantityProperty() {
        return quantityProperty;
    }

    public ObjectProperty<BigDecimal> totalPriceProperty() {
        return totalPriceProperty;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", order=" + (order != null ? order.getId() : "N/A") +
                ", product=" + (product != null ? product.getName() : "N/A") +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
